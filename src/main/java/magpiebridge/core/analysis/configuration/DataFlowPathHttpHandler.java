package magpiebridge.core.analysis.configuration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.MagpieServer;

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

      } 
    } finally {
      if (outputStream != null) outputStream.close();
    }
  }
}
