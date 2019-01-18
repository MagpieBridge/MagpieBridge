package magpiebridge.core;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

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

}
