/*
 * @author Linghui Luo
 */
package magpiebridge.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Implementation of quick fix command.
 *
 * @author Linghui Luo
 */
public class FixCommand implements WorkspaceCommand {

  @Override
  public String getName() {
    return CodeActionCommand.fix.name();
  }

  @Override
  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client) {
    List<Object> args = params.getArguments();
    JsonPrimitive juri = (JsonPrimitive) args.get(0);
    JsonObject jrange = (JsonObject) args.get(1);
    JsonPrimitive jreplaceText = (JsonPrimitive) args.get(2);
    JsonObject jstart = jrange.get("start").getAsJsonObject();
    JsonObject jend = jrange.get("end").getAsJsonObject();
    Range range =
        new Range(
            new Position(jstart.get("line").getAsInt(), jstart.get("character").getAsInt()),
            new Position(jend.get("line").getAsInt(), jend.get("character").getAsInt()));
    String replaceText = jreplaceText.getAsString();
    TextEdit tEdit = new TextEdit(range, replaceText);
    Map<String, List<TextEdit>> changes = new HashMap<>();
    String uri = juri.getAsString();
    changes.put(uri, Collections.singletonList(tEdit));
    WorkspaceEdit edit = new WorkspaceEdit(changes);
    client.applyEdit(new ApplyWorkspaceEditParams(edit));
  }
}
