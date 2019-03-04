package magpiebridge.core;

import com.google.common.io.Files;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.io.TemporaryFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * The Class MagpieServer.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class MagpieServer implements LanguageServer, LanguageClientAware {

  /** The client. */
  protected LanguageClient client;

  /** The text document service. */
  protected TextDocumentService textDocumentService;

  /** The workspace service. */
  protected WorkspaceService workspaceService;

  /** The language analyses. */
  protected Map<String, Collection<ServerAnalysis>> languageAnalyses;

  /** The language source files. */
  protected Map<String, Map<Module, URI>> languageSourceFiles;

  /** The language project services. */
  protected Map<String, IProjectService> languageProjectServices;

  /** The diagnostics. */
  protected Map<URL, List<Diagnostic>> diagnostics;

  /** The hovers. */
  protected Map<URL, NavigableMap<Position, Hover>> hovers;

  /** The code lenses. */
  protected Map<URL, List<CodeLens>> codeLenses;

  /** The root path. */
  protected Optional<Path> rootPath;

  /** The server client uri. */
  private Map<String, String> serverClientUri;

  /** The connection socket. */
  private Socket connectionSocket;

  /** The logger. */
  public Logger logger;

  /**
   * Instantiates a new magpie server using default {@link MagpieTextDocumentService} and {@link MagpieWorkspaceService}.
   */
  public MagpieServer() {
    this.textDocumentService = new MagpieTextDocumentService(this);
    this.workspaceService = new MagpieWorkspaceService(this);
    languageAnalyses = new HashMap<String, Collection<ServerAnalysis>>();
    languageSourceFiles = new HashMap<String, Map<Module, URI>>();
    languageProjectServices = new HashMap<String, IProjectService>();
    diagnostics = new HashMap<>();
    hovers = new HashMap<>();
    codeLenses = new HashMap<>();
    serverClientUri = new HashMap<>();
    logger = new Logger();
  }

  /**
   * Instantiates a new magpie server using given {@link TextDocumentService} and {@link WorkspaceService}.
   *
   * @param textDocumentService
   *          the text document service
   * @param workspaceService
   *          the workspace service
   */
  public MagpieServer(TextDocumentService textDocumentService, WorkspaceService workspaceService) {
    this();
    this.textDocumentService = textDocumentService;
    this.workspaceService = workspaceService;
  }

  /**
   * Launch on stdio.
   */
  public void launchOnStdio() {
    launchOnStream(logStream(System.in, "magpie.in"), logStream(System.out, "magpie.out"));
  }

  /**
   * Launch on stream.
   *
   * @param in
   *          the in
   * @param out
   *          the out
   */
  public void launchOnStream(InputStream in, OutputStream out) {
    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(this, logStream(in, "magpie.in"),
        logStream(out, "magpie.out"), true, new PrintWriter(System.err));
    connect(launcher.getRemoteProxy());
    launcher.startListening();
  }

  /**
   * Launch on socket port.
   *
   * @param host
   *          the host
   * @param port
   *          the port
   */
  public void launchOnSocketPort(String host, int port) {
    try {
      connectionSocket = new Socket(host, port);
      Launcher<LanguageClient> launcher
          = LSPLauncher.createServerLauncher(this, logStream(connectionSocket.getInputStream(), "magpie.in"),
              logStream(connectionSocket.getOutputStream(), "magpie.out"));
      connect(launcher.getRemoteProxy());
      launcher.startListening();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Launch on web socket port.
   */
  public void launchOnWebSocketPort() {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageClientAware#connect(org.eclipse.lsp4j.services.LanguageClient)
   */
  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageServer#initialize(org.eclipse.lsp4j.InitializeParams)
   */
  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    logger.logClientMsg(params.toString());
    System.err.println("client:\n" + params);
    if (params.getRootUri() != null) {
      this.rootPath = Optional.ofNullable(Paths.get(URI.create(params.getRootUri())));
    } else {
      this.rootPath = Optional.empty();
    }
    final ServerCapabilities caps = new ServerCapabilities();
    caps.setHoverProvider(true);
    caps.setTextDocumentSync(TextDocumentSyncKind.Full);
    CodeLensOptions cl = new CodeLensOptions();
    cl.setResolveProvider(true);
    caps.setCodeLensProvider(cl);
    caps.setDocumentSymbolProvider(true);
    caps.setDefinitionProvider(true);
    caps.setReferencesProvider(true);
    ExecuteCommandOptions exec = new ExecuteCommandOptions();
    exec.setCommands(new LinkedList<String>());
    caps.setExecuteCommandProvider(exec);
    caps.setCodeActionProvider(false);
    InitializeResult v = new InitializeResult(caps);
    System.err.println("server:\n" + caps);
    logger.logServerMsg(v.toString());
    return CompletableFuture.completedFuture(v);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.InitializedParams)
   */
  @Override
  public void initialized(InitializedParams params) {
    logger.logClientMsg(params.toString());
    System.err.println("client:\n" + params);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageServer#shutdown()
   */
  @Override
  public CompletableFuture<Object> shutdown() {
    return CompletableFuture.completedFuture(new Object());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageServer#exit()
   */
  @Override
  public void exit() {
    try {
      connectionSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds the source code.
   *
   * @param language
   *          the language
   * @param text
   *          the text
   * @param clientUri
   *          the client uri
   * @return true, if successful
   */
  public boolean addSource(String language, String text, String clientUri) {
    try {
      File file = File.createTempFile("temp", ".java");
      file.deleteOnExit();
      TemporaryFile.stringToFile(file, text);
      Module sourceFile = new SourceFileModule(file, clientUri.toString(), null);
      String serverUri = Paths.get(file.toURI()).toUri().toString();
      serverClientUri.put(serverUri, clientUri);
      if (!languageSourceFiles.containsKey(language)) {
        languageSourceFiles.put(language, new HashMap<Module, URI>());
      }
      if (!languageSourceFiles.get(language).containsKey(sourceFile)) {
        languageSourceFiles.get(language).put(sourceFile, new URI(clientUri));
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Add project service for different languages. This should be specified by the user of MagpieServer.<br>
   * An example for using MagpieServer for java projects.
   * 
   * <pre>
   * {
   *   &#64;code
   *   MagpieServer server = new MagpieServer();
   *   String language = "java";
   *   IProjectService javaProjectService = new JavaProjectService();
   *   server.addProjectService(language, javaProjectService);
   * }
   * </pre>
   *
   * @param language
   *          the language
   * @param projectService
   *          the project service
   */
  public void addProjectService(String language, IProjectService projectService) {
    if (!this.languageProjectServices.containsKey(language)) {
      this.languageProjectServices.put(language, projectService);
    }
  }

  /**
   * Adds the analysis for different languages running on the server. This should be specified by the user of
   * MagpieServer.<br>
   * An example for adding a user-defined analysis.
   * 
   * <pre>
   * {@code
   * MagpieServer server = new MagpieServer();
   * String language = "java";
   * ServerAnalysis myAnalysis = new MyAnalysis();
   * server.addAnalysis(language, myAnalysis);
   * }
   * 
   * <pre>
   *
   * @param language
   *          the language
   * @param analysis
   *          the analysis
   */
  public void addAnalysis(String language, ServerAnalysis analysis) {
    if (!languageAnalyses.containsKey(language)) {
      languageAnalyses.put(language, new HashSet<ServerAnalysis>());
    }
    languageAnalyses.get(language).add(analysis);
  }

  /**
   * Do analysis.
   *
   * @param language
   *          the language
   */
  public void doAnalysis(String language) {
    Map<Module, URI> sourceFiles = this.languageSourceFiles.get(language);
    if (!languageAnalyses.containsKey(language)) {
      languageAnalyses.put(language, Collections.emptyList());
    }
    for (ServerAnalysis analysis : languageAnalyses.get(language)) {
      analysis.analyze(sourceFiles.keySet(), this);
    }
  }

  /**
   * Consume the analysis results.
   *
   * @param results
   *          the results
   * @param source
   *          the source
   */
  public void consume(Collection<AnalysisResult> results, String source) {
    for (AnalysisResult result : results) {
      URL url = result.position().getURL();
      List<Diagnostic> diagList = null;
      if (this.diagnostics.containsKey(url)) {
        diagList = diagnostics.get(url);
      } else {
        diagList = new ArrayList<>();
        this.diagnostics.put(url, diagList);
      }
      switch (result.kind()) {
        case Diagnostic:
          createDiagnosticConsumer(diagList, source).accept(result);
          break;
        case Hover:
          createHoverConsumer().accept(result);
          break;
        case CodeLens:
          createCodeLensConsumer().accept(result);
          break;
        default:
          break;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageServer#getTextDocumentService()
   */
  @Override
  public TextDocumentService getTextDocumentService() {
    return this.textDocumentService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.lsp4j.services.LanguageServer#getWorkspaceService()
   */
  @Override
  public WorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  /**
   * Gets the project service.
   *
   * @param language
   *          the language
   * @return the project service
   */
  public Optional<IProjectService> getProjectService(String language) {
    return Optional.ofNullable(languageProjectServices.get(language));
  }

  /**
   * Creates the diagnostic consumer.
   *
   * @param diagList
   *          the diag list
   * @param source
   *          the source
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createDiagnosticConsumer(List<Diagnostic> diagList, String source) {
    Consumer<AnalysisResult> consumer = result -> {
      Diagnostic d = new Diagnostic();
      d.setMessage(result.toString(false));
      d.setRange(getLocationFrom(result.position()).getRange());
      d.setSource(source);
      List<DiagnosticRelatedInformation> relatedList = new ArrayList<>();
      for (Pair<Position, String> related : result.related()) {
        DiagnosticRelatedInformation di = new DiagnosticRelatedInformation();
        di.setLocation(getLocationFrom(related.fst));
        di.setMessage(related.snd);
        relatedList.add(di);
      }
      d.setRelatedInformation(relatedList);
      d.setSeverity(result.severity());
      if (!diagList.contains(d)) {
        diagList.add(d);
      }
      PublishDiagnosticsParams pdp = new PublishDiagnosticsParams();
      pdp.setDiagnostics(diagList);
      String serverUri = result.position().getURL().toString();
      if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
        // take care of uri in windows
        if (!serverUri.startsWith("file:///")) {
          serverUri = serverUri.replace("file://", "file:///");
        }
      }
      String clientUri = null;
      if (serverClientUri.containsKey(serverUri)) {
        // the file was at least opened once in the editor
        clientUri = serverClientUri.get(serverUri);
      } else {
        // the file was not opened, but whole project was analyzed
        try {
          File file = new File(new URI(serverUri));
          if (file.exists()) {
            clientUri = serverUri;
          }
        }catch (URISyntaxException e)
        {
          e.printStackTrace();
        }
      }
      if (clientUri != null) {
        pdp.setUri(clientUri);
        client.publishDiagnostics(pdp);
        logger.logServerMsg(pdp.toString());
        System.err.println("server:\n" + pdp);
      }
    };
    return consumer;
  }

  /**
   * Creates the hover consumer.
   *
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createHoverConsumer() {
    Consumer<AnalysisResult> consumer = result -> {
      Hover hover = new Hover();
      List<Either<String, MarkedString>> contents = new ArrayList<>();
      Either<String, MarkedString> content = Either.forLeft(result.toString(true));
      contents.add(content);
      hover.setContents(contents);
      hover.setRange(getLocationFrom(result.position()).getRange());

    };
    return consumer;
  }

  /**
   * Creates the code lens consumer.
   *
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createCodeLensConsumer() {
    Consumer<AnalysisResult> consumer = result -> {
      CodeLens codeLens = new CodeLens();

      codeLens.setRange(getLocationFrom(result.position()).getRange());

    };
    return consumer;
  }

  /**
   * Gets the location from given position.
   *
   * @param pos
   *          the pos
   * @return the location from
   */
  protected Location getLocationFrom(Position pos) {
    Location codeLocation = new Location();
    try {
      codeLocation.setUri(pos.getURL().toURI().toString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    Range codeRange = new Range();
    if (pos.getFirstCol() < 0) {
      codeRange.setStart(getPositionFrom(pos.getFirstLine(), 0));// imprecise
    } else {
      codeRange.setStart(getPositionFrom(pos.getFirstLine(), pos.getFirstCol()));
    }
    if (pos.getLastLine() < 0) {
      codeRange.setEnd(getPositionFrom(pos.getFirstLine() + 1, 0));// imprecise
    } else {
      codeRange.setEnd(getPositionFrom(pos.getLastLine(), pos.getLastCol()));
    }
    codeLocation.setRange(codeRange);
    return codeLocation;
  }

  /**
   * Gets the position from given line and column numbers.
   *
   * @param line
   *          the line
   * @param column
   *          the column
   * @return the position from
   */
  protected org.eclipse.lsp4j.Position getPositionFrom(int line, int column) {
    org.eclipse.lsp4j.Position codeStart = new org.eclipse.lsp4j.Position();
    codeStart.setLine(line - 1);
    codeStart.setCharacter(column);
    return codeStart;
  }

  /**
   * Lookup pos.
   *
   * @param pos
   *          the pos
   * @param url
   *          the url
   * @return the position
   */
  protected Position lookupPos(org.eclipse.lsp4j.Position pos, URL url) {
    return new AbstractSourcePosition() {

      @Override
      public int getFirstLine() {
        // LSP is 0-based, but parsers mostly 1-based
        return pos.getLine() + 1;
      }

      @Override
      public int getLastLine() {
        // LSP is 0-based, but parsers mostly 1-based
        return pos.getLine() + 1;
      }

      @Override
      public int getFirstCol() {
        return pos.getCharacter();
      }

      @Override
      public int getLastCol() {
        return pos.getCharacter();
      }

      @Override
      public int getFirstOffset() {
        return -1;
      }

      @Override
      public int getLastOffset() {
        return -1;
      }

      @Override
      public URL getURL() {
        return url;
      }

      @Override
      public Reader getReader() throws IOException {
        return new InputStreamReader(url.openConnection().getInputStream());
      }

      @Override
      public String toString() {
        return url + ":" + getFirstLine() + "," + getFirstCol();
      }
    };
  }

  /**
   * Find hover.
   *
   * @param lookupPos
   *          the lookup pos
   * @return the hover
   */
  public Hover findHover(Position lookupPos) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Find code lenses.
   *
   * @param uri
   *          the uri
   * @return the list
   */
  public List<CodeLens> findCodeLenses(URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Log stream.
   *
   * @param is
   *          the is
   * @param logFileName
   *          the log file name
   * @return the input stream
   */
  static InputStream logStream(InputStream is, String logFileName) {
    File log;
    try {
      log = File.createTempFile(logFileName, ".txt");
      return new TeeInputStream(is, new FileOutputStream(log));
    } catch (IOException e) {
      return is;
    }
  }

  /**
   * Log stream.
   *
   * @param os
   *          the os
   * @param logFileName
   *          the log file name
   * @return the output stream
   */
  static OutputStream logStream(OutputStream os, String logFileName) {
    File log;
    try {
      log = File.createTempFile(logFileName, ".txt");
      return new TeeOutputStream(os, new FileOutputStream(log));
    } catch (IOException e) {
      return os;
    }
  }

}
