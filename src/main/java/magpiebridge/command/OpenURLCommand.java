package magpiebridge.command;

import com.google.gson.JsonPrimitive;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import magpiebridge.core.MagpieClient;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import magpiebridge.util.URIUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Implementation of opening an URL in the client or the default browser.
 *
 * @author Julian Dolby
 * @author Linghui Luo
 */
public class OpenURLCommand implements WorkspaceCommand {

  @Override
  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client) {
    if (Desktop.isDesktopSupported()) {
      try {
        String uri;
        Object uriJson = params.getArguments().get(0);
        if (uriJson instanceof JsonPrimitive) {
          uri = ((JsonPrimitive) uriJson).getAsString();
        } else {
          uri = (String) uriJson;
        }
        showHTMLinClientOrBroswer(server, client, uri);
      } catch (IOException | URISyntaxException e) {
        MagpieServer.ExceptionLogger.log(e);
        e.printStackTrace();
      }
    }
  }

  /**
   * Show A HTML page with the given URI in the client, or in a browser if the client doesn't
   * support this.
   *
   * @param server The MagpieServer
   * @param client The IDE/Editor
   * @param uri The URI which should be opened
   * @throws IOException IO exception
   * @throws URISyntaxException URI exception
   */
  public static void showHTMLinClientOrBroswer(
      MagpieServer server, LanguageClient client, String uri)
      throws IOException, URISyntaxException {
    if (server.clientSupportShowHTML()) {
      if (client instanceof MagpieClient) {
        if (!uri.contains("://")) uri = uri.replace(":/", "://");
        String content =
            IOUtils.toString(new URI(URIUtils.checkURI(uri)), StandardCharsets.UTF_8.toString());
        ((MagpieClient) client).showHTML(content);
      }
    } else {
      if (Desktop.isDesktopSupported()) {
        // disable stdout from browser as it corrupts jsonrpc stdio communication
        PrintStream original = System.out;
        try {
          PrintStream devnull = new PrintStream(
                  new OutputStream() {
                    public void write(int b) {
                    }
                  });
          System.setErr(devnull);
          System.setOut(devnull);
          Desktop.getDesktop().browse(new URI(URIUtils.checkURI(uri)));
        } catch (Exception e) {
          e.printStackTrace();
        }
        // System.setOut(original);
      }
    }
  }
}
