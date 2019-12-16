/*
 * @author Linghui Luo
 */
package magpiebridge.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Implementation of reporting false positive command.
 *
 * @author Linghui Luo
 */
public class ReportFalsePositiveCommand implements WorkspaceCommand {

  @Override
  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client) {
    client.showMessage(new MessageParams(MessageType.Info, "False alarm was reported."));
    List<Object> args = params.getArguments();
    JsonPrimitive uri = (JsonPrimitive) args.get(0);
    JsonObject jdiag = (JsonObject) args.get(1);
    // just record start line number, code and message to identify diagnostic.
    if (jdiag.has("range") && jdiag.has("code") && jdiag.has("message")) {
      int lineNumber =
          jdiag
              .get("range")
              .getAsJsonObject()
              .get("start")
              .getAsJsonObject()
              .get("line")
              .getAsInt();
      String code = jdiag.get("code").getAsString();
      String message = jdiag.get("message").getAsString();
      Triple<Integer, String, String> diag = Triple.of(lineNumber, code, message);
      try {
        String decodedUri = URLDecoder.decode(uri.getAsString(), "UTF-8");
        server.recordFalsePositive(decodedUri, diag);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
  }
}
