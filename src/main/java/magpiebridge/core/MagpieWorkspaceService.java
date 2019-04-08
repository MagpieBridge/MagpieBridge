package magpiebridge.core;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
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
          return server.client.applyEdit(
              new ApplyWorkspaceEditParams(server.matchActions.get(0).getEdit()));
        });
  }
}
