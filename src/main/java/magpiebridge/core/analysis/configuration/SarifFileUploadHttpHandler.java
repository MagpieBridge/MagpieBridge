package magpiebridge.core.analysis.configuration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import magpiebridge.core.MagpieServer;
import org.apache.commons.io.IOUtils;

/**
 * The class handles HTTP requests of the SARIF page. 
 */
public class SarifFileUploadHttpHandler implements HttpHandler {

  private MagpieServer magpieServer;
  private String serverAddress;

  public SarifFileUploadHttpHandler(MagpieServer magpieServer, String serverAddress) {
    this.magpieServer = magpieServer;
    this.serverAddress = serverAddress;
  }

  public void handle(HttpExchange exchange) throws IOException {
    OutputStream outputStream = exchange.getResponseBody();
    try {
      if ("GET".equals(exchange.getRequestMethod().toUpperCase())) {
        String htmlPage = SarifFileUploadHtmlGenerator.generateHTML(null, this.serverAddress);

        exchange.sendResponseHeaders(200, htmlPage.length());
        outputStream.write(htmlPage.getBytes());
        outputStream.flush();
        outputStream.close();

      } else if ("POST".equals(exchange.getRequestMethod().toUpperCase())) {

        StringWriter writer = new StringWriter();
        IOUtils.copy(exchange.getRequestBody(), writer, StandardCharsets.UTF_8);
        String theString = getJsonFromString(writer.toString());
        JsonObject sarifJson = (JsonObject) new JsonParser().parse(theString);
        String finalResult = "";
        try {
          SARIFElement sarifElement = new SARIFElement(sarifJson);
          this.magpieServer.consume(sarifElement.getAnalysisResults(), "Sarif File Upload");
          finalResult = sarifElement.getAnalysisResults().toString();
        } catch (Exception e) {
          finalResult = e.toString();
        } finally {
          exchange.sendResponseHeaders(200, finalResult.length());
          outputStream.write(finalResult.getBytes());
          outputStream.flush();
          outputStream.close();
        }
      }
    } finally {
      if (outputStream != null) outputStream.close();
    }
  }
  /**
   * Keep only JSON part of the string
   * 
   * @param str
   * @return
   */
  public String getJsonFromString(String str) {
    String content = "";
    String temp = "";
    Stack<Character> stack = new Stack<Character>();
    for (char singleChar : str.toCharArray()) {
      if (stack.isEmpty() && singleChar == '{') {
        stack.push(singleChar);
        temp += singleChar;
      } else if (!stack.isEmpty()) {
        temp += singleChar;
        if (singleChar == '}' && stack.peek().equals('{')) {
          stack.pop();
          if (stack.isEmpty()) {
            content += temp;
            temp = "";
          }
        } else if (singleChar == '{' || singleChar == '}') {
          stack.push(singleChar);
        }
      } else if (temp.length() > 0 && stack.isEmpty()) {
        content += temp;
        temp = "";
      }
    }
    return content;
  }
}
