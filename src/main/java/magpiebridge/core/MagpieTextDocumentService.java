package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import magpiebridge.file.SourceFileManager;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * Default {@link TextDocumentService} for {@link MagpieServer}.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class MagpieTextDocumentService implements TextDocumentService {

  /** The server. */
  protected final MagpieServer server;

  /**
   * Instantiates a new magpie text document service.
   *
   * @param server the server
   */
  public MagpieTextDocumentService(MagpieServer server) {
    this.server = server;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem doc = params.getTextDocument();
    String language = doc.getLanguageId();
    // set the rootPath for project service if it is not set yet.
    if (server.rootPath.isPresent()) {
      if (server.getProjectService(language).isPresent()) {
        server.getProjectService(language).get().setRootPath(server.rootPath.get());
      }
    }
    // add the opened file to file manager and do analysis
    SourceFileManager fileManager = server.getSourceFileManager(language);
    fileManager.didOpen(params);
    server.doAnalysis(language);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    // update the changed file in file manager and clean diagnostics.
    String language = inferLanguage(params.getTextDocument().getUri());
    SourceFileManager fileManager = server.getSourceFileManager(language);
    fileManager.didChange(params);
    // TODO. it should be customized to clean all or just this changed file
    server.cleanAllDiagnostics();
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {}

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    // re-analyze when file is saved.
    String language = inferLanguage(params.getTextDocument().getUri());
    server.doAnalysis(language);
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

  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          List<Either<Command, CodeAction>> actions = new ArrayList<>();
          try {
            String uri = params.getTextDocument().getUri();
            List<CodeAction> matchedActions =
                server.findCodeActions(new URI(uri), params.getContext().getDiagnostics());
            for (CodeAction action : matchedActions) {
              // FIXME. VSCode expects CodeAction, but Sublime expects Command. Now just send both
              actions.add(Either.forRight(action));
              // actions.add(Either.forLeft(action.getCommand()));
            }
          } catch (URISyntaxException e) {
            e.printStackTrace();
          }
          return actions;
        });
  }

  /**
   * Infer language of the file from the uri.
   *
   * @param uri the uri
   * @return the string
   */
  private String inferLanguage(String uri) {
    if (uri.endsWith(".java")) {
      return "java";
    } else if (uri.endsWith(".py")) {
      return "python";
    } else if (uri.endsWith(".js")) {
      return "javascript";
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
