package magpiebridge.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.util.io.TemporaryFile;

public class MagpieTextDocumentService implements TextDocumentService {

	protected final MagpieServer server;

	public MagpieTextDocumentService(MagpieServer server) {
		this.server = server;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		System.err.println("client:\n"+params);
		TextDocumentItem doc = params.getTextDocument();
		String language = doc.getLanguageId();
		try {
			URI uri = new URI(doc.getUri());
			File file = File.createTempFile("temp", ".java");
			file.deleteOnExit();
			TemporaryFile.stringToFile(file, doc.getText());
			Module sourceFile = new SourceFileModule(file, uri.toString(), null);
			server.addSource(language, sourceFile, uri);
			server.doAnalysis(language);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> {
			Hover hover = new Hover();
			try {
				String uri = position.getTextDocument().getUri();
				URL url = new URI(uri).toURL();
				Position lookupPos = server.lookupPos(position.getPosition(), url);
				hover = server.findHover(lookupPos);
			} catch (MalformedURLException | URISyntaxException e) {
				e.printStackTrace();
			}
			return hover;
		});

	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return CompletableFuture.supplyAsync(() -> {
			List<CodeLens> codeLenses = new ArrayList<CodeLens>();
			String uri = params.getTextDocument().getUri();
			try {
				codeLenses = server.findCodeLenses(new URI(uri));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return codeLenses;
		});
	}

}
