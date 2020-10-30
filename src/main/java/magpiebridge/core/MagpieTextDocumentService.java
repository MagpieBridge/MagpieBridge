/*
 * @author Linghui Luo
 */
package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import magpiebridge.file.SourceFileManager;
import magpiebridge.util.SourceCodePositionUtils;
import magpiebridge.util.URIUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
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

  protected Timer timer = new Timer();

  /** Flag to check if the file is the first opened one. */
  protected boolean isFirstOpenedFile;

  private TimerTask timerTask;
  private Runnable task;

  /**
   * Instantiates a new magpie text document service.
   *
   * @param server the server
   */
  public MagpieTextDocumentService(MagpieServer server) {
    this.server = server;
    this.isFirstOpenedFile = true;
  }

  @Override
  public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem doc = params.getTextDocument();
    String language = doc.getLanguageId();
    // add the opened file to file manager and do analysis
    SourceFileManager fileManager = server.getSourceFileManager(language);
    fileManager.didOpen(params);
    if (server.config.doAnalysisByOpen()) {
      if (isFirstOpenedFile) {
        server.doAnalysis(language, true);
        isFirstOpenedFile = false;
      } else {
        // don't need to rerun the analysis if no file is changed.
        server.doAnalysis(language, false);
      }
    }
  }

  /**
   * Set the rootPath for project service if it is not set yet.
   *
   * @param language the language
   */
  protected void setProjectRootPath(String language) {
    if (server.rootPath.isPresent()) {
      if (server.getProjectService(language).isPresent()) {
        server.getProjectService(language).get().setRootPath(server.rootPath.get());
      }
    }
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    // update the changed file in file manager
    String language = inferLanguage(params.getTextDocument().getUri());
    SourceFileManager fileManager = server.getSourceFileManager(language);
    fileManager.didChange(params);
    // TODO. it could be customized to clean all diagnostics.
    // server.cleanUp();
    if (server.config.doAnalysisByIdle()) {
      // cancel the task running the analysis
      restartTimer();
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {}

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    // update the saved file in file manager
    String language = inferLanguage(params.getTextDocument().getUri());
    SourceFileManager fileManager = server.getSourceFileManager(language);
    // run analysis depending on the configuration
    fileManager.didSave(params);
    if (server.config.doAnalysisByIdle()) {
      this.task =
          () -> {
            if (fileManager.allFilesSaved()) runAnalysis(params);
          };
      restartTimer();
    } else {
      if (server.config.doAnalysisBySave()) {
        runAnalysis(params);
      }
    }
  }

  private void runAnalysis(DidSaveTextDocumentParams params) {
    server.cleanUp();
    // re-analyze when file is saved.
    String language = inferLanguage(params.getTextDocument().getUri());
    server.doAnalysis(language, true);
  }

  private void restartTimer() {
    if (this.timerTask != null) timerTask.cancel();
    if (this.task != null) {
      this.timerTask =
          new TimerTask() {
            public void run() {
              task.run();
            }
          };
      timer.schedule(timerTask, server.config.timeOut());
    }
  }

  @Override
  public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
    return CompletableFuture.supplyAsync(
        () -> {
          Hover hover = new Hover();
          try {
            String uri = position.getTextDocument().getUri();
            String decodedUri = URLDecoder.decode(uri, "UTF-8");
            URL url = new URI(URIUtils.checkURI(decodedUri)).toURL();
            Position lookupPos = SourceCodePositionUtils.lookupPos(position.getPosition(), url);
            hover = server.findHover(lookupPos);
          } catch (MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
            MagpieServer.ExceptionLogger.log(e);
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
          try {
            String uri = params.getTextDocument().getUri();
            String decodedUri = URLDecoder.decode(uri, "UTF-8");
            codeLenses = server.findCodeLenses(new URI(URIUtils.checkURI(decodedUri)));
          } catch (URISyntaxException | UnsupportedEncodingException e) {
            MagpieServer.ExceptionLogger.log(e);
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
            String decodedUri = URLDecoder.decode(uri, "UTF-8");
            List<CodeAction> matchedActions =
                server.findCodeActions(new URI(URIUtils.checkURI(decodedUri)), params);
            for (CodeAction action : matchedActions) {
              actions.add(Either.forLeft(action.getCommand()));
            }
          } catch (URISyntaxException | UnsupportedEncodingException e) {
            MagpieServer.ExceptionLogger.log(e);
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
  protected String inferLanguage(String uri) {
    if (uri.endsWith(".java")) {
      return "java";
    } else if (uri.endsWith(".py")) {
      return "python";
    } else if (uri.endsWith(".js")) {
      return "javascript";
    } else if (uri.endsWith(".ts")) {
      return "typescript";
    } else if (uri.endsWith(".cpp") || uri.endsWith(".h")) {
      return "c++";
    } else if (uri.endsWith(".c")) {
      return "c";
    } else {
      MagpieServer.ExceptionLogger.log("Couldn't infer the language of the source code in " + uri);
      return "unknown";
    }
  }
}
