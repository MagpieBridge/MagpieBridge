package magpiebridge.command;

import com.google.gson.JsonPrimitive;
import com.ibm.wala.util.io.FileUtil;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import magpiebridge.core.MagpieClient;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
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
        String url;
        Object uriJson = params.getArguments().get(0);
        if (uriJson instanceof JsonPrimitive) {
          url = ((JsonPrimitive) uriJson).getAsString();
        } else {
          url = (String) uriJson;
        }
        showHTMLinClientOrBroswer(server, client, url);
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    }
  }

  public static void showHTMLinClientOrBroswer(
      MagpieServer server, LanguageClient client, String url)
      throws IOException, URISyntaxException {
    if (server.clientSupportShowHTML()) {
      if (client instanceof MagpieClient) {
        MessageParams mp = new MessageParams();
        mp.setType(MessageType.Info);
        mp.setMessage(new String(FileUtil.readBytes(new URL(url).openStream())));
        ((MagpieClient) client).showHTML(mp);
      }
    } else {
      if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(url));
    }
  }
}
