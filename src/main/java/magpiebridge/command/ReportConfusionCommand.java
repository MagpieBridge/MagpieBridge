/*
 * @author Linghui Luo
 */
package magpiebridge.command;

import magpiebridge.core.MagpieServer;
import magpiebridge.core.WorkspaceCommand;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Implementation of reporting confusion command.
 *
 * @author Linghui Luo
 */
public class ReportConfusionCommand implements WorkspaceCommand {

  @Override
  public String getName() {
    return CodeActionCommand.reportConfusion.name();
  }

  @Override
  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client) {
    client.showMessage(new MessageParams(MessageType.Info, "Thank you for your feedback!"));
  }
}
