/*
 * @author Linghui Luo
 */
package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;
import com.ibm.wala.cast.tree.impl.LineNumberPosition;
import com.ibm.wala.cast.util.SourceBuffer;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Pair;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import magpiebridge.command.CodeActionCommand;
import magpiebridge.file.SourceFileManager;
import magpiebridge.util.MagpieMessageLogger;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.launch.LSPLauncher.Builder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * The Class MagpieServer.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class MagpieServer implements AnalysisConsumer, LanguageServer, LanguageClientAware {

  private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

  private static ServerSocket serverSocket;

  /** The server configuration. */
  protected final ServerConfiguration config;

  /** The client. */
  protected LanguageClient client;

  /** The text document service. */
  protected TextDocumentService textDocumentService;

  /** The workspace service. */
  protected MagpieWorkspaceService workspaceService;

  /** The language analyzes. language mapped to a set of analyzes. */
  protected Map<String, Collection<Either<ServerAnalysis, ToolAnalysis>>> languageAnalyzes;

  /**
   * The language source file managers. language mapped to its corresponding source file manager.
   */
  protected Map<String, SourceFileManager> languageSourceFileManagers;

  /** The language project services. language mapped to its project service. */
  protected Map<String, IProjectService> languageProjectServices;

  /** The diagnostics. */
  protected Map<URL, List<Diagnostic>> diagnostics;

  /** The hovers. */
  protected Map<URL, NavigableMap<Position, Hover>> hovers;

  /** The code lenses. */
  protected Map<URL, List<CodeLens>> codeLenses;

  /** The code actions. */
  protected Map<URL, Map<Range, List<CodeAction>>> codeActions;

  /** The false positives reported by users. */
  protected Map<String, Set<Triple<Integer, String, String>>> falsePositives;

  /** The root path. */
  protected Optional<Path> rootPath;

  /** Map server-side URI to client-side URI. */
  protected Map<String, String> serverClientUri;

  /** The logger. */
  protected MagpieMessageLogger logger;

  /** Client config */
  private ClientCapabilities clientConfig;

  final Map<String, Consumer<Command>> commands = HashMapFactory.make();

  /**
   * Instantiates a new MagpieServer using default {@link MagpieTextDocumentService} and {@link
   * MagpieWorkspaceService} with given {@link ServerConfiguration}.
   *
   * @param config the config
   */
  public MagpieServer(ServerConfiguration config) {
    this.config = config;
    this.textDocumentService = new MagpieTextDocumentService(this);
    this.workspaceService = new MagpieWorkspaceService(this);
    this.languageAnalyzes = new HashMap<>();
    this.languageSourceFileManagers = new HashMap<String, SourceFileManager>();
    this.languageProjectServices = new HashMap<String, IProjectService>();
    this.diagnostics = new HashMap<>();
    this.hovers = new HashMap<>();
    this.codeLenses = new HashMap<>();
    this.codeActions = new HashMap<>();
    this.serverClientUri = new HashMap<>();
    this.falsePositives = new HashMap<>();
    logger = config.getMagpieMessageLogger();
  }

  public LanguageClient getClient() {
    return client;
  }

  /**
   * Sets customized text document service.
   *
   * @param textDocumentService the new text document service
   */
  public void setTextDocumentService(TextDocumentService textDocumentService) {
    this.textDocumentService = textDocumentService;
  }

  /**
   * Sets customized workspace service.
   *
   * @param workspaceService the new workspace service
   */
  public void setWorkspaceService(MagpieWorkspaceService workspaceService) {
    this.workspaceService = workspaceService;
  }

  /** Launch on stdio. */
  public void launchOnStdio() {
    launchOnStream(System.in, System.out);
  }

  /**
   * Launch on stream.
   *
   * @param in the in
   * @param out the out
   */
  public void launchOnStream(InputStream in, OutputStream out) {
    Launcher<LanguageClient> launcher;
    launcher = LSPLauncher.createServerLauncher(this, in, out, THREAD_POOL, logger.getWrapper());
    connect(launcher.getRemoteProxy());
    launcher.startListening();
  }

  /**
   * Launch on socket port.
   *
   * @param port the port
   * @deprecated The server will crash after the first client disconnects. To support more clients
   *     use {@link #launchOnSocketPort(int, Supplier)} instead.
   */
  public void launchOnSocketPort(int port) {
    try {
      serverSocket = new ServerSocket(port);
      Socket connectionSocket = serverSocket.accept();
      Launcher<LanguageClient> launcher =
          new Builder<LanguageClient>()
              .setLocalService(this)
              .setRemoteInterface(LanguageClient.class)
              .setInput(connectionSocket.getInputStream())
              .setOutput(connectionSocket.getOutputStream())
              .setExecutorService(Executors.newCachedThreadPool())
              .traceMessages(config.traceWriter())
              .wrapMessages(logger.getWrapper())
              .create();
      connect(launcher.getRemoteProxy());
      launcher.startListening();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Launch on socket port. Will create a new {@link MagpieServer} instance for each new connecting
   * client using the given supplier. Example:
   *
   * <pre>
   * <code>
   *  Supplier<MagpieServer> supplier = ()->{
   *    MagpieServer server = new MagpieServer(new ServerConfiguration());
   *    String language = "java";
   *    IProjectService javaProjectService = new JavaProjectService();
   *    server.addProjectService(language, javaProjectService);
   *    return server.
   *  }
   * </code>
   * </pre>
   *
   * @param port the port
   */
  public static void launchOnSocketPort(int port, Supplier<MagpieServer> createServer) {
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
      serverSocket = new ServerSocket(port);
      while (!serverSocket.isClosed()) {
        MagpieServer server = createServer.get();
        Socket connectionSocket = serverSocket.accept();
        Launcher<LanguageClient> launcher =
            new Builder<LanguageClient>()
                .setLocalService(server)
                .setRemoteInterface(LanguageClient.class)
                .setInput(connectionSocket.getInputStream())
                .setOutput(connectionSocket.getOutputStream())
                .setExecutorService(THREAD_POOL)
                .wrapMessages(server.logger.getWrapper())
                .traceMessages(server.config.traceWriter())
                .create();
        server.connect(launcher.getRemoteProxy());
        new Thread(
                () -> {
                  try {
                    // wait for future to return, signaling connection was closed
                    launcher.startListening().get();
                  } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                  }
                },
                connectionSocket.getRemoteSocketAddress() + " connected")
            .start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.eclipse.lsp4j.services.LanguageClientAware#connect(org.eclipse.lsp4j.
   * services.LanguageClient)
   */
  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.lsp4j.services.LanguageServer#initialize(org.eclipse.lsp4j.
   * InitializeParams)
   */
  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    if (params.getRootUri() != null) {
      this.rootPath = Optional.ofNullable(Paths.get(URI.create(params.getRootUri())));
    } else {
      this.rootPath = Optional.empty();
    }
    clientConfig = params.getCapabilities();

    final ServerCapabilities caps = new ServerCapabilities();
    caps.setTypeDefinitionProvider(false);
    caps.setImplementationProvider(false);
    caps.setWorkspaceSymbolProvider(false);
    caps.setDocumentFormattingProvider(false);
    caps.setDocumentRangeFormattingProvider(false);
    caps.setDocumentHighlightProvider(false);
    caps.setColorProvider(false);
    caps.setDocumentSymbolProvider(false);
    caps.setDefinitionProvider(false);
    caps.setReferencesProvider(false);
    CodeLensOptions cl = new CodeLensOptions();
    cl.setResolveProvider(false);
    caps.setCodeLensProvider(cl);
    caps.setHoverProvider(true);
    caps.setTextDocumentSync(TextDocumentSyncKind.Full);
    ExecuteCommandOptions exec = new ExecuteCommandOptions();
    MagpieWorkspaceService service = (MagpieWorkspaceService) workspaceService;
    exec.setCommands(service.getCommandNames());
    caps.setExecuteCommandProvider(exec);
    caps.setCodeActionProvider(true);
    InitializeResult v = new InitializeResult(caps);
    return CompletableFuture.completedFuture(v);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
   * InitializedParams)
   */
  @Override
  public void initialized(InitializedParams params) {}

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
    logger.cleanUp();
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.exit(0);
  }

  /**
   * Gets the source file manager for given language.
   *
   * @param language the language
   * @return the source file manager
   */
  public SourceFileManager getSourceFileManager(String language) {
    if (!this.languageSourceFileManagers.containsKey(language)) {
      this.languageSourceFileManagers.put(
          language, new SourceFileManager(language, this.serverClientUri));
    }
    return this.languageSourceFileManagers.get(language);
  }

  /**
   * Add project service for different languages. This should be specified by the user of
   * MagpieServer.<br>
   * An example for using MagpieServer for java projects.
   *
   * <pre>
   * <code>
   * 	MagpieServer server = new MagpieServer(new ServerConfiguration());
   * 	String language = "java";
   * 	IProjectService javaProjectService = new JavaProjectService();
   * 	server.addProjectService(language, javaProjectService);
   * </code>
   * </pre>
   *
   * @param language the language
   * @param projectService the project service
   */
  public void addProjectService(String language, IProjectService projectService) {
    if (!this.languageProjectServices.containsKey(language)) {
      this.languageProjectServices.put(language, projectService);
    }
  }

  public void addCommand(String commandName, WorkspaceCommand processor) {
    getWorkspaceService().commands.put(commandName, processor);
  }

  /**
   * Adds the analysis for different languages running on the server. This should be specified by
   * the user of MagpieServer.<br>
   * An example for adding a user-defined analysis.
   * <pre>
   * <code>
   * MagpieServer server = new MagpieServer(new ServerConfiguration());
   * String language = "java";
   * ServerAnalysis myAnalysis = new MyAnalysis();
   * Either<ServerAnalysis, ToolAnalysis> analysis=Either.forLeft(myAnalysis);
   * server.addAnalysis(analysis,language);
   * </code>
   * <pre>
   * @param analysis the analysis
   * @param languages the languages handled by this analysis
   */
  public void addAnalysis(Either<ServerAnalysis, ToolAnalysis> analysis, String... languages) {
    for (String language : languages) {
      if (!languageAnalyzes.containsKey(language)) {
        languageAnalyzes.put(language, new HashSet<>());
      }
      languageAnalyzes.get(language).add(analysis);
    }
  }

  /**
   * Do analysis.
   *
   * @param language the language
   * @param rerun tells if the analysis should be reran.
   */
  public void doAnalysis(String language, boolean rerun) {
    SourceFileManager fileManager = getSourceFileManager(language);
    if (!languageAnalyzes.containsKey(language)) {
      languageAnalyzes.put(language, Collections.emptyList());
    }
    for (Either<ServerAnalysis, ToolAnalysis> analysis : languageAnalyzes.get(language)) {
      if (analysis.isLeft()) {
        analysis.getLeft().analyze(fileManager.getSourceFileModules().values(), this, rerun);
      } else {
        analysis.getRight().analyze(fileManager.getSourceFileModules().values(), this, rerun);
      }
    }
  }

  /**
   * Consume the analysis results.
   *
   * @param results the results
   * @param source the source
   */
  public void consume(Collection<AnalysisResult> results, String source) {
    Map<String, List<Diagnostic>> publishDiags = new HashMap<>();
    for (AnalysisResult result : results) {
      URL serverURL = result.position().getURL();
      try {
        URL clientURL = new URL(getClientUri(serverURL.toString()));
        switch (result.kind()) {
          case Diagnostic:
            List<Diagnostic> diagList = null;
            if (this.diagnostics.containsKey(clientURL)) {
              diagList = diagnostics.get(clientURL);
            } else {
              diagList = new ArrayList<>();
              this.diagnostics.put(clientURL, diagList);
            }
            if (!isFalsePositive(result)) {
              createDiagnosticConsumer(publishDiags, diagList, source).accept(result);
            }
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
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
    for (String clientUri : publishDiags.keySet()) {
      List<Diagnostic> diagList = publishDiags.get(clientUri);
      PublishDiagnosticsParams pdp = new PublishDiagnosticsParams();
      pdp.setDiagnostics(diagList);
      pdp.setUri(clientUri);
      client.publishDiagnostics(pdp);
    }
    client.showMessage(
        new MessageParams(MessageType.Info, "The analyzer finished analyzing the code."));
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
  public MagpieWorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  public boolean clientSupportsMarkdown() {
    return clientConfig != null
        && clientConfig.getTextDocument() != null
        && clientConfig.getTextDocument().getHover() != null
        && clientConfig.getTextDocument().getHover().getContentFormat() != null
        && clientConfig
            .getTextDocument()
            .getHover()
            .getContentFormat()
            .contains(MarkupKind.MARKDOWN);
  }

  /**
   * Gets the project service.
   *
   * @param language the language
   * @return the project service
   */
  public Optional<IProjectService> getProjectService(String language) {
    return Optional.ofNullable(languageProjectServices.get(language));
  }

  /**
   * Checks if the result was reported as false positive by comparing code and message.
   *
   * @param result the result
   * @return true, if the result was reported as false positive
   */
  protected boolean isFalsePositive(AnalysisResult result) {
    String serverUri = result.position().getURL().toString();
    String clientUri = getClientUri(serverUri);
    for (String uri : falsePositives.keySet()) {
      if (uri.equals(clientUri)) {
        for (Triple<Integer, String, String> fp : falsePositives.get(clientUri)) {
          int diff = Math.abs((result.position().getFirstLine() + 1) - fp.getLeft());
          int threshold = 5;
          if (diff < threshold
              && result.code().equals(fp.getMiddle())
              && result.toString(false).equals(fp.getRight())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Adds the given code action for the given url and range.
   *
   * @param url the url which the code action belongs to.
   * @param range the range which the code action belongs to
   * @param action the action
   */
  protected void addCodeAction(URL url, Range range, CodeAction action) {
    if (!this.codeActions.containsKey(url)) {
      this.codeActions.put(url, new HashMap<>());
    }
    Map<Range, List<CodeAction>> actionList = this.codeActions.get(url);
    if (!actionList.containsKey(range)) {
      actionList.put(range, new ArrayList<>());
    }
    List<CodeAction> actions = actionList.get(range);
    if (!actions.contains(action)) actions.add(action);
    actionList.put(range, actions);
  }

  /**
   * Creates the diagnostic consumer.
   *
   * @param publishDiags map URI to the list of diagnostics to be published for the client.
   * @param diagList the list of diagnostics stored on the server
   * @param source the source
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createDiagnosticConsumer(
      Map<String, List<Diagnostic>> publishDiags, List<Diagnostic> diagList, String source) {
    Consumer<AnalysisResult> consumer =
        result -> {
          Diagnostic d = new Diagnostic();
          d.setMessage(result.toString(false));
          d.setRange(getLocationFrom(result.position()).getRange());
          d.setSource(source);
          d.setCode(result.code());
          List<DiagnosticRelatedInformation> relatedList = new ArrayList<>();
          if (result.related() != null)
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
          String serverUri = result.position().getURL().toString();
          String clientUri = getClientUri(serverUri);
          try {
            URL url = new URL(URLDecoder.decode(clientUri, "UTF-8"));
            if (result.repair() != null) {
              // add code action (quickfix) related to analysis result
              Position fixPos = result.repair().fst;
              if (fixPos != null) {
                String replace = result.repair().snd;
                Range range = getLocationFrom(fixPos).getRange();
                CodeAction fix =
                    CodeActionGenerator.replace(
                        "Fix: replace it with " + replace, range, replace, clientUri, d);
                addCodeAction(url, d.getRange(), fix);
              }
            } else if (result.command() != null) {
              result
                  .command()
                  .forEach(
                      (cmd) -> {
                        CodeAction act = new CodeAction();
                        act.setCommand(cmd);
                        act.setTitle(cmd.getTitle());
                        act.setDiagnostics(Collections.singletonList(d));
                        act.setKind("info");
                        addCodeAction(url, d.getRange(), act);
                      });
            }
            if (config.reportFalsePositive()) {
              // report false positive
              String title = String.format("Report it as false alarm (%s).", d.getMessage());
              CodeAction reportFalsePositive =
                  CodeActionGenerator.generateCommandAction(
                      title, clientUri, d, CodeActionCommand.reportFP.name());
              addCodeAction(url, d.getRange(), reportFalsePositive);
            }
            if (config.reportConfusion()) {
              // report confusion about the warning message
              String title =
                  String.format("I don't understand this warning message (%s).", d.getMessage());
              CodeAction reportConfusion =
                  CodeActionGenerator.generateCommandAction(
                      title, clientUri, d, CodeActionCommand.reportConfusion.name());
              addCodeAction(url, d.getRange(), reportConfusion);
            }
          } catch (MalformedURLException | UnsupportedEncodingException e) {
            e.printStackTrace();
          }
          if (clientUri != null) {
            publishDiags.put(clientUri, diagList);
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
    Consumer<AnalysisResult> consumer =
        result -> {
          try {
            String serverUri = result.position().getURL().toURI().toString();
            String clientUri = getClientUri(serverUri);
            URL clientURL = new URL(clientUri);
            Position pos = replaceURL(result.position(), clientURL);
            Hover hover = new Hover();

            List<Either<String, MarkedString>> contents = new ArrayList<>();
            if (clientConfig != null
                && clientConfig.getTextDocument().getHover().getContentFormat() != null
                && clientConfig
                    .getTextDocument()
                    .getHover()
                    .getContentFormat()
                    .contains(MarkupKind.MARKDOWN)) {
              contents.add(
                  Either.forRight(new MarkedString(MarkupKind.MARKDOWN, result.toString(true))));
            } else {
              for (String str : result.toString(false).split("\n")) {
                Either<String, MarkedString> content = Either.forLeft(str);
                contents.add(content);
              }
            }
            hover.setContents(contents);
            hover.setRange(getLocationFrom(pos).getRange());
            NavigableMap<Position, Hover> hoverMap = new TreeMap<>();
            if (this.hovers.containsKey(clientURL)) {
              hoverMap = this.hovers.get(clientURL);
            }
            hoverMap.put(pos, hover);
            this.hovers.put(clientURL, hoverMap);
          } catch (MalformedURLException e) {
            e.printStackTrace();
          } catch (URISyntaxException e1) {
            e1.printStackTrace();
          }
        };
    return consumer;
  }

  /**
   * Creates the code lens consumer.
   *
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createCodeLensConsumer() {
    Consumer<AnalysisResult> consumer =
        result -> {
          try {
            String serverUri = result.position().getURL().toString();
            String clientUri = getClientUri(serverUri);
            URL clientURL = new URL(clientUri);
            CodeLens codeLens = new CodeLens();
            if (result.repair() != null) {
              Location loc = this.getLocationFrom(result.repair().fst);
              codeLens.setCommand(new Command("fix", CodeActionCommand.fix.name()));
              codeLens
                  .getCommand()
                  .setArguments(Arrays.asList(clientUri, loc.getRange(), result.repair().snd));
            } else {
              codeLens.setCommand(result.command().iterator().next());
            }
            codeLens.setRange(getLocationFrom(result.position()).getRange());
            List<CodeLens> list = new ArrayList<>();
            if (this.codeLenses.containsKey(clientURL)) {
              list = this.codeLenses.get(clientURL);
            }
            list.add(codeLens);
            this.codeLenses.put(clientURL, list);
          } catch (MalformedURLException e) {
            e.printStackTrace();
          }
        };
    return consumer;
  }

  /**
   * Check if URI is in the right format in windows.
   *
   * @param uri the uri
   * @return the string
   */
  protected String checkURI(String uri) {
    if (uri.startsWith("file")) {
      if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
        // take care of uri in windows
        if (!uri.startsWith("file:///")) {
          uri = uri.replace("file://", "file:///");
        }
        if (!uri.startsWith("file:///")) uri = uri.replace("file:/", "file:///");
      } else {
        if (!uri.startsWith("file://")) {
          String[] splits = uri.split(":");
          if (splits.length == 2) {
            String path = splits[1];
            if (path.matches("^(/[^/ ]*)+/?$")) {
              uri = "file://" + path;
            }
          }
        }
      }
    }
    return uri;
  }

  /**
   * Gets the client uri.
   *
   * @param serverUri the server uri
   * @return the client uri
   */
  public String getClientUri(String serverUri) {
    serverUri = checkURI(serverUri);
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
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    return clientUri;
  }

  private Position getPositionWithColumns(Position p) {
    if (p.getFirstCol() >= 0) {
      return p;
    } else {
      Position firstLineP = new LineNumberPosition(p.getURL(), p.getURL(), p.getFirstLine());
      SourceBuffer firstLine = null;
      try {
        firstLine = new SourceBuffer(firstLineP);
      } catch (IOException e) {
        assert false : e;
      }
      String firstLineText = firstLine.toString();

      String pText = null;
      try {
        pText = new SourceBuffer(p).toString();
      } catch (IOException e) {
        assert false : e;
      }

      int lines = pText.split("\n").length;

      String pFirst = pText;
      if (pText.contains("\n")) {
        pFirst = pText.substring(0, pText.indexOf("\n"));
      }

      int endCol;
      int startCol = firstLineText.indexOf(pFirst);

      if (!pText.contains("\n")) {
        endCol = startCol + pText.length();
      } else {
        String lastLineText = pText.substring(pText.lastIndexOf("\n"), pText.length());
        endCol = lastLineText.length();
      }

      return new AbstractSourcePosition() {
        @Override
        public Reader getReader() throws IOException {
          return p.getReader();
        }

        @Override
        public URL getURL() {
          return p.getURL();
        }

        @Override
        public int getFirstCol() {
          return startCol;
        }

        @Override
        public int getFirstLine() {
          return p.getFirstLine();
        }

        @Override
        public int getFirstOffset() {
          return p.getFirstOffset();
        }

        @Override
        public int getLastCol() {
          return endCol;
        }

        @Override
        public int getLastLine() {
          if (p.getLastLine() >= 0) {
            return p.getLastLine();
          } else {
            return p.getFirstLine() + lines - 1;
          }
        }

        @Override
        public int getLastOffset() {
          return p.getLastOffset();
        }
      };
    }
  }

  /**
   * Gets the location from given position.
   *
   * @param pos the pos
   * @return the location from
   */
  protected Location getLocationFrom(Position pos) {
    Location codeLocation = new Location();
    try {
      codeLocation.setUri(checkURI(pos.getURL().toURI().toString()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    Range codeRange = new Range();
    Position detail = getPositionWithColumns(pos);
    codeRange.setEnd(getPositionFrom(detail.getLastLine(), detail.getLastCol()));
    codeRange.setStart(getPositionFrom(detail.getFirstLine(), detail.getFirstCol()));
    codeLocation.setRange(codeRange);
    return codeLocation;
  }

  /**
   * Gets the position from given line and column numbers.
   *
   * @param line the line
   * @param column the column
   * @return the position from
   */
  protected org.eclipse.lsp4j.Position getPositionFrom(int line, int column) {
    org.eclipse.lsp4j.Position codeStart = new org.eclipse.lsp4j.Position();
    codeStart.setLine(line - 1);
    codeStart.setCharacter(column);
    return codeStart;
  }

  /**
   * Replace URL.
   *
   * @param pos the pos
   * @param url the url
   * @return the position
   */
  protected Position replaceURL(Position pos, URL url) {
    return new AbstractSourcePosition() {

      @Override
      public int getLastOffset() {
        return pos.getLastOffset();
      }

      @Override
      public int getLastLine() {
        return pos.getLastLine();
      }

      @Override
      public int getLastCol() {
        return pos.getLastCol();
      }

      @Override
      public int getFirstOffset() {
        return pos.getFirstOffset();
      }

      @Override
      public int getFirstLine() {
        return pos.getFirstLine();
      }

      @Override
      public int getFirstCol() {
        return pos.getFirstCol();
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
   * Lookup pos.
   *
   * @param pos the pos
   * @param url the url
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
   * Find hover for the given lookup position.
   *
   * @param lookupPos the lookup pos
   * @return the hover
   */
  public Hover findHover(Position lookupPos) {
    int size = Integer.MAX_VALUE;
    Hover hover = null;
    if (this.hovers.containsKey(lookupPos.getURL())) {
      NavigableMap<Position, Hover> map = this.hovers.get(lookupPos.getURL());
      for (Position pos : map.keySet()) {
        if ((pos.getFirstLine() <= lookupPos.getFirstLine()
                && (pos.getLastLine() >= lookupPos.getFirstLine()))
            && ((pos.getFirstCol() - 5 <= lookupPos.getFirstCol())
                && (pos.getLastCol() + 5 >= lookupPos.getLastCol()))) {
          int sz =
              (pos.getFirstLine() == pos.getLastLine())
                  ? pos.getLastCol() - pos.getFirstCol()
                  : 80 * (pos.getLastLine() - pos.getFirstLine());
          if (sz < size) {
            hover = map.get(pos);
            size = sz;
          }
        }
      }
    }
    return hover;
  }

  /**
   * Check if two source code positions are near to each other.
   *
   * @param pos1 the pos 1
   * @param pos2 the pos 2
   * @param diff the diff
   * @return true, if successful
   */
  private boolean areNearPositions(Position pos1, Position pos2, int diff) {
    if (pos1.getFirstLine() == pos2.getFirstLine()
        && Math.abs(pos1.getFirstCol() - pos2.getFirstCol()) <= diff) return true;
    return false;
  }

  /**
   * Find code lenses for the given uri.
   *
   * @param uri the uri
   * @return the list of code lenses for the given uri.
   */
  public List<CodeLens> findCodeLenses(URI uri) {
    try {
      if (this.codeLenses.containsKey(uri.toURL())) {
        return this.codeLenses.get(uri.toURL());
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  /**
   * Find code actions attached to the given code action params.
   *
   * @param uri the uri
   * @param params the code action params
   * @return the list of code actions for the given code action params.
   */
  public List<CodeAction> findCodeActions(URI uri, CodeActionParams params) {
    List<Diagnostic> diagnostics = params.getContext().getDiagnostics();
    try {
      URL url = uri.toURL();
      if (this.codeActions.containsKey(url)) {
        Map<Range, List<CodeAction>> actions = this.codeActions.get(url);
        // First look for diagnostics.
        for (Diagnostic dia : diagnostics) {
          if (actions.containsKey(dia.getRange())) {
            return actions.get(dia.getRange());
          }
        }
        if (diagnostics == null || diagnostics.isEmpty()) {
          // if there is no diagnostic, just find code action for this line.
          Range range = params.getRange();
          for (Range r : actions.keySet())
            if (range.getStart().getLine() == r.getStart().getLine()) return actions.get(r);
        }
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  /** Clean up all analysis results. */
  public void cleanUp() {
    for (URL url : diagnostics.keySet()) {
      client.publishDiagnostics(
          new PublishDiagnosticsParams(getClientUri(url.toString()), Collections.emptyList()));
    }
    this.diagnostics.clear();
    this.codeActions.clear();
    this.hovers.clear();
    this.codeLenses.clear();
  }

  /**
   * Record false positive reported by users.
   *
   * @param uri the uri
   * @param diag the code and message in the reported diagnostic
   */
  /*
   *
   */
  public void recordFalsePositive(String uri, Triple<Integer, String, String> diag) {
    if (!falsePositives.containsKey(uri)) {
      this.falsePositives.put(uri, new HashSet<Triple<Integer, String, String>>());
    }
    Set<Triple<Integer, String, String>> dias = this.falsePositives.get(uri);
    dias.add(diag);
    this.falsePositives.put(uri, dias);
  }
}
