/*
 * @author Linghui Luo
 */
package magpiebridge.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import magpiebridge.command.CodeActionCommand;
import magpiebridge.command.FixCommand;
import magpiebridge.command.ReportConfusionCommand;
import magpiebridge.command.ReportFalsePositiveCommand;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
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

  protected void addDefaultCommands() {
    this.commands.put(CodeActionCommand.fix.name(), new FixCommand());
    this.commands.put(CodeActionCommand.reportFP.name(), new ReportFalsePositiveCommand());
    this.commands.put(CodeActionCommand.reportConfusion.name(), new ReportConfusionCommand());
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    // TODO Auto-generated method stub
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    // TODO Auto-generated method stub
  }

  public void addCommand(WorkspaceCommand command) {
    this.commands.put(command.getName(), command);
  }

  @Override
  public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          String command = params.getCommand();
          if (this.commands.containsKey(command)) {
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
