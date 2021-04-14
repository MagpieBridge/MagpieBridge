package magpiebridge.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageClient;

public class SuppressWarningCommand implements WorkspaceCommand {

  @Override
  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client) {
    List<Object> args = params.getArguments();
    JsonPrimitive uri = (JsonPrimitive) args.get(0);
    JsonObject jdiag = (JsonObject) args.get(1);
    try {
      String decodedUri = URLDecoder.decode(uri.getAsString(), "UTF-8");
      server.getSuppressWarningHandler().recordSuppression(decodedUri, jdiag);
    } catch (UnsupportedEncodingException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
  }
}
