package magpiebridge.core.analysis.configuration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerConfiguration;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * The class handles HTTP request from the configuration page. This is used when the server is
 * configured to show the configuration page after initialization, namely {@link
 * ServerConfiguration#showConfigurationPage()} returns true.
 *
 * @author Linghui Luo
 */
public class MagpieHttpHandler implements HttpHandler {

  private MagpieServer magpieServer;
  private String serverAddress;

  public MagpieHttpHandler(MagpieServer magpieServer, String serverAddress) {
    this.magpieServer = magpieServer;
    this.serverAddress = serverAddress;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    URI uri = exchange.getRequestURI();
    OutputStream outputStream = exchange.getResponseBody();
    if ("GET".equals(exchange.getRequestMethod().toUpperCase())) {
      String htmlPage =
          HtmlGenerator.generateHTML(
              magpieServer.getAnalysisConfiguration(),
              magpieServer.getConfigurationActions(),
              this.serverAddress);
      List<NameValuePair> params = URLEncodedUtils.parse(uri, Charset.forName("UTF-8"));
      if (!params.isEmpty() && params.size() == 2) {
        magpieServer.performConfiguredAction(params.get(0), params.get(1));
        exchange.getResponseHeaders().add("Location", "/config");
        exchange.sendResponseHeaders(307, -1);
      } else {
        exchange.sendResponseHeaders(200, htmlPage.length());
        outputStream.write(htmlPage.getBytes());
        outputStream.flush();
        outputStream.close();
      }

    } else if ("POST".equals(exchange.getRequestMethod().toUpperCase())) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
      String body = URLDecoder.decode(reader.lines().collect(Collectors.joining()), "UTF-8");
      reader.close();
      Map<String, String> requestOptions = new HashMap<>();
      String[] options = body.split("&");
      for (String option : options) {
        String[] pairs = option.split("=");
        if (pairs.length > 1) {
          String key = pairs[0];
          String value = pairs[1];
          requestOptions.put(key, value);
        }
      }
      List<ConfigurationOption> newOptions = magpieServer.setConfigurationOptions(requestOptions);
      String htmlPage =
          HtmlGenerator.generateHTML(
              newOptions, magpieServer.getConfigurationActions(), this.serverAddress);
      exchange.sendResponseHeaders(200, htmlPage.length());
      outputStream.write(htmlPage.getBytes());
      outputStream.flush();
      outputStream.close();
    }
  }
}
