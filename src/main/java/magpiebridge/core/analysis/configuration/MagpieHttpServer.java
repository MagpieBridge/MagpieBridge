package magpiebridge.core.analysis.configuration;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import magpiebridge.core.MagpieServer;

/** @author Linghui Luo */
public class MagpieHttpServer {

  public static URI createAndStartLocalHttpServer(MagpieServer magpieServer) {
    try {
      InetAddress ipAddress = InetAddress.getLocalHost();
      InetSocketAddress socket = new InetSocketAddress(ipAddress.getHostName(), 0);
      HttpServer server = HttpServer.create(socket, 0);
      HttpContext context = server.createContext("/config");
      context.setHandler(new MagpieHttpHandler(magpieServer));
      server.start();
      return new URI("http", server.getAddress().toString() + "/config", null);
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }
}
