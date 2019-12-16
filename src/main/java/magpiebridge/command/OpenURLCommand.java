package magpiebridge.command;

import com.google.gson.JsonPrimitive;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Implementation of opening an URL in browser.
 *
 * @author Julian Dolby
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
        Desktop.getDesktop().browse(new URI(url));
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    }
  }
}
