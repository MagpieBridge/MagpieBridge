package magpiebridge.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Default {@link WorkspaceService} for {@link MagpieServer}.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class MagpieWorkspaceService implements WorkspaceService {
  protected final MagpieServer server;

  public MagpieWorkspaceService(MagpieServer server) {
    this.server = server;
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    // TODO Auto-generated method stub
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    // TODO Auto-generated method stub
  }

  @Override
  public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          String command = params.getCommand();
          if (command.equals("reportFP")) {
            server.client.showMessage(
                new MessageParams(MessageType.Info, "False alarm was reported."));
            List<Object> args = params.getArguments();
            JsonPrimitive uri = (JsonPrimitive) args.get(0);
            JsonObject jdiag = (JsonObject) args.get(1);
            // just record start line number, code and message to identify diagnostic.
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
            return null;
          } else if (command.equals("reportConfusion")) {
            server.client.showMessage(
                new MessageParams(MessageType.Info, "Thank you for your feedback!"));
            return null;
          } else
            return server.client.applyEdit(
                new ApplyWorkspaceEditParams(server.actionForDiags.get(0).getEdit()));
        });
  }
}
