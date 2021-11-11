package magpiebridge.core.analysis.configuration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.FlowAnalysisResult;
import magpiebridge.core.FlowCodePosition;
import magpiebridge.core.Kind;
import magpiebridge.core.MagpieServer;
import magpiebridge.util.JsonFormatHandler;
import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.DiagnosticSeverity;

/** The class handles HTTP requests of the flow graph page. */
public class DataFlowPathHttpHandler implements HttpHandler {

  private MagpieServer magpieServer;
  private String serverAddress;
  private AnalysisResult result;

  public DataFlowPathHttpHandler(
      MagpieServer magpieServer, String serverAddress, AnalysisResult result) {
    this.magpieServer = magpieServer;
    this.serverAddress = serverAddress;
    this.result = result;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    OutputStream outputStream = exchange.getResponseBody();
    try {
      if ("GET".equals(exchange.getRequestMethod().toUpperCase())) {
        String htmlPage = DataFlowPathHtmlGenerator.generateHTML(this.result, this.serverAddress);

        exchange.sendResponseHeaders(200, htmlPage.length());
        outputStream.write(htmlPage.getBytes());
        outputStream.flush();
        outputStream.close();

      } else if ("POST".equals(exchange.getRequestMethod().toUpperCase())) {
        StringWriter writer = new StringWriter();
        IOUtils.copy(exchange.getRequestBody(), writer, StandardCharsets.UTF_8);
        String theString = JsonFormatHandler.getJsonFromString(writer.toString());
        JsonObject data = (JsonObject) new JsonParser().parse(theString);
        String finalResult = "";
        try {
          Position errorPosition = locationToPosition(data);
          List<AnalysisResult> flowResults = new ArrayList<AnalysisResult>();
          String code =
              JsonFormatHandler.notNullAndHas(data, "code") ? data.get("code").getAsString() : null;
          AnalysisResult flowResult =
              new FlowAnalysisResult(
                  Kind.Diagnostic,
                  errorPosition,
                  "Affected line",
                  null,
                  DiagnosticSeverity.Error,
                  null,
                  code);
          flowResults.add(flowResult);
          this.magpieServer.consume(flowResults, "Show in the editor.");
          finalResult = errorPosition.toString() + " " + result.position().toString();
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

  private Position locationToPosition(JsonObject data) throws MalformedURLException {

    int firstLine =
        JsonFormatHandler.notNullAndHas(data, "firstLine") ? data.get("firstLine").getAsInt() : -1;
    int lastLine =
        JsonFormatHandler.notNullAndHas(data, "lastLine") ? data.get("lastLine").getAsInt() : -1;
    int firstCol =
        JsonFormatHandler.notNullAndHas(data, "firstCol") ? data.get("firstCol").getAsInt() : -1;
    int lastCol =
        JsonFormatHandler.notNullAndHas(data, "lastCol") ? data.get("lastCol").getAsInt() : -1;
    URL url =
        JsonFormatHandler.notNullAndHas(data, "url")
            ? new URL(data.get("url").getAsString())
            : null;
    FlowCodePosition position =
        new FlowCodePosition(firstLine, firstCol, lastLine, lastCol, url, null);
    return position;
  }
}
