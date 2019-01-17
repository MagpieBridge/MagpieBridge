package magpiebridge.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * 
 * @author Julian Dolby and Linghui Luo
 *
 */
public class MagpieServer implements LanguageServer, LanguageClientAware {
	protected LanguageClient client;
	protected Map<String, Collection<ServerAnalysis>> languageAnalyses;
	
	private Socket connectionSocket;

	public void launchOnStdio() {
		launchOnStream(System.in, System.out);
	}

	public void launchOnSocketPort(String host, int port) {
		try {
			connectionSocket = new Socket(host, port);
			Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(this, connectionSocket.getInputStream(),
					connectionSocket.getOutputStream());

			connect(launcher.getRemoteProxy());
			launcher.startListening();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void launchOnWebSocketPort()
	{
		
	}
	
	public void launchOnStream(InputStream in, OutputStream out) {
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(this, in, out, true,
				new PrintWriter(System.err));
		connect(launcher.getRemoteProxy());
		launcher.startListening();
	}

	public void connect(LanguageClient client) {
		this.client = client;
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
		try {
			connectionSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TextDocumentService getTextDocumentService() {
		// TODO Auto-generated method stub
		return null;
	}

	public WorkspaceService getWorkspaceService() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addAnalysis(String language, ServerAnalysis analysis) {

	}

	public void doAnalysis(String language) {
		
	}

	protected void consume(Collection<AnalysisResult> results) {
	}

	protected Consumer<AnalysisResult> createDiagnosticConsumer() {
		return null;
	}

	protected Consumer<AnalysisResult> createHoverConsumer() {
		return null;
	}

	protected Consumer<AnalysisResult> createCodeLensConsumer() {
		return null;
	}

	public String getSourceCodePath() {
		return null;
	}
}
