package magpiebridge.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import magpiebridge.command.CodeActionCommand;
import magpiebridge.command.FixCommand;
import magpiebridge.command.OpenURLCommand;
import magpiebridge.command.ReportConfusionCommand;
import magpiebridge.command.ReportFalsePositiveCommand;
import magpiebridge.command.SuppressWarningCommand;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Default {@link WorkspaceService} for {@link MagpieServer}.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class MagpieWorkspaceService implements WorkspaceService {
  protected final MagpieServer server;
  protected Map<String, WorkspaceCommand> commands;

  public MagpieWorkspaceService(MagpieServer server) {
    this.server = server;
    this.commands = new HashMap<String, WorkspaceCommand>();
    addDefaultCommands();
  }

  /** Add default commands, see {@link CodeActionCommand}. */
  protected void addDefaultCommands() {
    this.commands.put(CodeActionCommand.fix.name(), new FixCommand());
    this.commands.put(CodeActionCommand.reportFP.name(), new ReportFalsePositiveCommand());
    this.commands.put(CodeActionCommand.reportConfusion.name(), new ReportConfusionCommand());
    this.commands.put(CodeActionCommand.openURL.name(), new OpenURLCommand());
    this.commands.put(CodeActionCommand.suppressWarning.name(), new SuppressWarningCommand());
  }

  /**
   * Add a code action and its corresponding command.
   *
   * @param commandName the name of the command.
   * @param command the implementation of the command.
   */
  protected void addCommand(String commandName, WorkspaceCommand command) {
    if (!this.commands.containsKey(commandName)) {
      this.commands.put(commandName, command);
    }
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
          if (command.equals(CodeActionCommand.fix.name())) {
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
            server.client.applyEdit(new ApplyWorkspaceEditParams(edit));
          } else if (command.equals(CodeActionCommand.reportFP.name())) {
            server.forwardMessageToClient(
                new MessageParams(MessageType.Info, "False alarm was reported."));
            List<Object> args = params.getArguments();
            JsonPrimitive uri = (JsonPrimitive) args.get(0);
            JsonObject jdiag = (JsonObject) args.get(1);
            try {
              String decodedUri = URLDecoder.decode(uri.getAsString(), "UTF-8");
              server.getFalsePositiveHandler().recordFalsePositive(decodedUri, jdiag);
            } catch (UnsupportedEncodingException e) {
              MagpieServer.ExceptionLogger.log(e);
              e.printStackTrace();
            }
          } else if (command.equals(CodeActionCommand.reportConfusion.name())) {
            server.forwardMessageToClient(
                new MessageParams(MessageType.Info, "Thank you for your feedback!"));
          } else if (command.equals(CodeActionCommand.suppressWarning.name())) {
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
          } else if (this.commands.containsKey(command)) {
            WorkspaceCommand cmd = this.commands.get(command);
            cmd.execute(params, server, server.client);
          }

          return null;
        });
  }

  public List<String> getCommandNames() {
    List<String> ret = new ArrayList<>();
    for (String name : this.commands.keySet()) {
      ret.add(name);
    }
    return ret;
  }
}
