/*
 * @author Linghui Luo
 */
package magpiebridge.core;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Define a code action command.
 *
 * @author Linghui Luo
 */
public interface WorkspaceCommand {

  public String getName();

  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client);
}
