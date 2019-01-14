package magpie.magpiebridge;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class MagpieServer  implements LanguageServer, LanguageClientAware
{
    public static void main( String[] args )
    {
   
    }

	public void connect(LanguageClient client) {
		// TODO Auto-generated method stub
		
	}

	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public CompletableFuture<Object> shutdown() {
		// TODO Auto-generated method stub
		return null;
	}

	public void exit() {
		// TODO Auto-generated method stub
		
	}

	public TextDocumentService getTextDocumentService() {
		// TODO Auto-generated method stub
		return null;
	}

	public WorkspaceService getWorkspaceService() {
		// TODO Auto-generated method stub
		return null;
	}
}
