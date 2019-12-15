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
@FunctionalInterface
public interface WorkspaceCommand {

  public void execute(ExecuteCommandParams params, MagpieServer server, LanguageClient client);
}
