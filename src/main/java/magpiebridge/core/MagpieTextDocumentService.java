package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
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

/**
 * Default {@link TextDocumentService} for {@link MagpieServer}.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class MagpieTextDocumentService implements TextDocumentService {

  protected final MagpieServer server;

  public MagpieTextDocumentService(MagpieServer server) {
    this.server = server;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    server.logger.logClientMsg(params.toString());
    System.err.println("client didOpen:\n" + params);
    TextDocumentItem doc = params.getTextDocument();
    String language = doc.getLanguageId();
    if (server.rootPath.isPresent()) {
      if (server.getProjectService(language).isPresent())
        server.getProjectService(language).get().setRootPath(server.rootPath.get());
    }
    server.addSource(language, doc.getText(), doc.getUri());
    server.doAnalysis(language);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    server.logger.logClientMsg(params.toString());
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
    return CompletableFuture.supplyAsync(
        () -> {
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
    return CompletableFuture.supplyAsync(
        () -> {
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
