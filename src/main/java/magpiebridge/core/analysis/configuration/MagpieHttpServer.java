package magpiebridge.core.analysis.configuration;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerConfiguration;

/**
 * The class creates a HTTP Server running in the same process as the {@link MagpieServer} to handle
 * from requests (user interactions) from the configuration page. This is used when the server is
 * configured to show the configuration page after initialization, namely {@link
 * ServerConfiguration#showConfigurationPage()} returns true.
 *
 * @author Linghui Luo
 */
public class MagpieHttpServer {

  public static String createAndStartLocalHttpServer(MagpieServer magpieServer) {
    try {
      InetSocketAddress socket = new InetSocketAddress("localhost", 0);
      HttpServer server = HttpServer.create(socket, 0);
      HttpContext context = server.createContext("/config");
      context.setHandler(new MagpieHttpHandler(magpieServer, server.getAddress().toString()));
      server.start();
      return new URI("http", server.getAddress().toString() + "/config", null).toURL().toString();
    } catch (IOException | URISyntaxException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
    return null;
  }
  /**
   * Creates a HTTP Server to view flow graph
   *
   * @param magpieServer server instance
   * @param result static analysis result element
   * @return newly created server URL
   */
  public static String createAndStartDataFlowHttpServer(
      MagpieServer magpieServer, AnalysisResult result) {
    try {
      String routeName = "/flow";
      InetSocketAddress socket = new InetSocketAddress("localhost", 0);
      HttpServer server = HttpServer.create(socket, 0);
      HttpContext context = server.createContext(routeName);
      context.setHandler(
          new DataFlowPathHttpHandler(magpieServer, server.getAddress().toString(), result));
      server.start();
      return new URI("http", server.getAddress().toString() + routeName, null).toURL().toString();
    } catch (IOException | URISyntaxException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
    return null;
  }
  /**
   * Creates a HTTP Server for SARIF file upload
   *
   * @param magpieServer
   * @return
   */
  public static String createAndStartSarifFileUploadHttpServer(MagpieServer magpieServer) {
    try {
      String routeName = "/sarif-file";
      InetSocketAddress socket = new InetSocketAddress("localhost", 0);
      HttpServer server = HttpServer.create(socket, 0);
      HttpContext context = server.createContext(routeName);
      context.setHandler(
          new SarifFileUploadHttpHandler(magpieServer, server.getAddress().toString()));
      server.start();
      return new URI("http", server.getAddress().toString() + routeName, null).toURL().toString();
    } catch (IOException | URISyntaxException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
    return null;
  }
}
