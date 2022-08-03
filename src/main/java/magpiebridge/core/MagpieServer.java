/*
 * @author Linghui Luo
 */
package magpiebridge.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import magpiebridge.command.OpenURLCommand;
import magpiebridge.core.analysis.configuration.ConfigurationAction;
import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.MagpieHttpServer;
import magpiebridge.core.analysis.configuration.OptionType;
import magpiebridge.file.SourceFileManager;
import magpiebridge.util.ExceptionLogger;
import magpiebridge.util.MagpieMessageLogger;
import magpiebridge.util.URIUtils;
import org.apache.http.NameValuePair;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
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

  public static ExceptionLogger ExceptionLogger = new ExceptionLogger();

  protected static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

  protected static ServerSocket serverSocket;

  /** The server configuration. */
  protected ServerConfiguration config;

  /** The client. */
  protected MagpieClient client;

  /** The client config */
  protected ClientCapabilities clientConfig;

  /** The text document service. */
  protected TextDocumentService textDocumentService;

  /** The workspace service. */
  protected MagpieWorkspaceService workspaceService;

  protected AnalysisResultConsumerFactory resultsConsumerFactory;

  protected SuppressWarningHandler suppressWarningHandler;

  protected FalsePositiveHandler falsePositiveHandler;

  protected ConfusionHandler confusionHandler;

  /** The language analyzes. language mapped to a set of analyzes. */
  protected Map<String, Collection<Either<ServerAnalysis, ToolAnalysis>>> languageAnalyses;

  /** The user-defined configuration of each analysis running on the server. */
  protected List<ConfigurationOption> analysisConfiguration;
  /**
   * The language source file managers. language mapped to its corresponding source file manager.
   */
  protected Map<String, SourceFileManager> languageSourceFileManagers;

  /** The language project services. language mapped to its project service. */
  protected Map<String, IProjectService> languageProjectServices;

  /** The version control service used by the project */
  protected Optional<VersionControlService> versionControlService;

  /** The diagnostics. */
  protected Map<URL, List<Diagnostic>> diagnostics;

  /** The hovers. */
  protected Map<URL, NavigableMap<Position, Hover>> hovers;

  /** The code lenses. */
  protected Map<URL, List<CodeLens>> codeLenses;

  /** The code actions. */
  protected Map<URL, Map<Range, List<CodeAction>>> codeActions;

  /** The root path. */
  protected Optional<Path> rootPath;

  /** Map server-side URI to client-side URI. */
  protected Map<String, String> serverClientUri;

  /** The logger. */
  protected MagpieMessageLogger logger;

  private String httpserverUrl;

  /**
   * Instantiates a new MagpieServer using default {@link MagpieTextDocumentService} and {@link
   * MagpieWorkspaceService} with given {@link ServerConfiguration}.
   *
   * @param config the config
   */
  public MagpieServer(ServerConfiguration config) {
    ExceptionLogger = new ExceptionLogger(this);
    this.config = config;
    // set up everything depends on the config.
    this.logger = config.getMagpieMessageLogger();
    this.falsePositiveHandler = config.getFalsePositiveHandler();
    this.falsePositiveHandler.registerAt(this);
    this.confusionHandler = config.getConfusionHanlder();
    this.confusionHandler.registerAt(this);
    this.suppressWarningHandler = config.getSuppressWarningHandler();
    this.suppressWarningHandler.registerAt(this);
    this.textDocumentService = new MagpieTextDocumentService(this);
    this.workspaceService = new MagpieWorkspaceService(this);
    this.resultsConsumerFactory = new AnalysisResultConsumerFactory(this);
    this.languageAnalyses = new HashMap<>();
    this.analysisConfiguration = new ArrayList<>();
    this.languageSourceFileManagers = new HashMap<String, SourceFileManager>();
    this.languageProjectServices = new HashMap<String, IProjectService>();
    this.versionControlService = Optional.empty();
    this.diagnostics = new HashMap<>();
    this.hovers = new HashMap<>();
    this.codeLenses = new HashMap<>();
    this.codeActions = new HashMap<>();
    this.serverClientUri = new HashMap<>();
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
    Launcher<MagpieClient> launcher;
    launcher =
        new Builder<MagpieClient>()
            .setLocalService(this)
            .setRemoteInterface(MagpieClient.class)
            .setInput(in)
            .setOutput(out)
            .setExecutorService(THREAD_POOL)
            .wrapMessages(logger.getWrapper())
            .traceMessages(config.traceWriter())
            .create();
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
      Launcher<MagpieClient> launcher =
          new Builder<MagpieClient>()
              .setLocalService(this)
              .setRemoteInterface(MagpieClient.class)
              .setInput(connectionSocket.getInputStream())
              .setOutput(connectionSocket.getOutputStream())
              .setExecutorService(Executors.newCachedThreadPool())
              .traceMessages(config.traceWriter())
              .wrapMessages(logger.getWrapper())
              .create();
      connect(launcher.getRemoteProxy());
      launcher.startListening();
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
  }

  /**
   * Launch on socket port at local host. Will create a new {@link MagpieServer} instance for each
   * new connecting client using the given supplier. Example:
   *
   * <pre>
   * <code>
   *  Supplier&#60;MagpieServer&#62; supplier = ()-&#62;{
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
   * @param createServer the server supplier
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
        Launcher<MagpieClient> launcher =
            new Builder<MagpieClient>()
                .setLocalService(server)
                .setRemoteInterface(MagpieClient.class)
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
                    MagpieServer.ExceptionLogger.log(e);
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
    this.client = (MagpieClient) client;
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
  public void initialized(InitializedParams params) {
    if (this.rootPath.isPresent()) {
      for (String language : languageProjectServices.keySet()) {

        if (this.getProjectService(language).isPresent()) {
          this.getProjectService(language).get().setRootPath(this.rootPath.get());
        }
      }
      if (this.versionControlService.isPresent())
        this.versionControlService.get().setRootPath(this.rootPath.get());
    }

    if (config.showConfigurationPage()) {
      createAndStartLocalHttpServer();
    }

    if (config.showSarifFileUploadPage()) {
      createAndStartSarifFileUploadHttpServer();
    }
  }

  protected void createAndStartLocalHttpServer() {
    try {
      initAnalysisConfiguration();
      if (config.useMagpieHTTPServer()) {
        this.httpserverUrl = MagpieHttpServer.createAndStartLocalHttpServer(this);
      }
      OpenURLCommand.showHTMLinClientOrBroswer(this, client, httpserverUrl);
    } catch (IOException | URISyntaxException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
  }

  protected void createAndStartSarifFileUploadHttpServer() {
    try {
      initAnalysisConfiguration();
      if (config.showSarifFileUploadPage()) {
        this.httpserverUrl = MagpieHttpServer.createAndStartSarifFileUploadHttpServer(this);
      }
      OpenURLCommand.showHTMLinClientOrBroswer(this, client, httpserverUrl);
    } catch (IOException | URISyntaxException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
  }

  public List<ConfigurationOption> getAnalysisConfiguration() {
    return analysisConfiguration;
  }

  protected void initAnalysisConfiguration() {
    // Reset the available configuration options and read them from analyses.
    // This is useful when the configuration options change
    analysisConfiguration = new ArrayList<>();
    for (Entry<String, Collection<Either<ServerAnalysis, ToolAnalysis>>> entry :
        languageAnalyses.entrySet()) {
      String language = entry.getKey();
      Collection<Either<ServerAnalysis, ToolAnalysis>> analyses = entry.getValue();
      for (Either<ServerAnalysis, ToolAnalysis> e : analyses) {
        String source = (e.isLeft() ? e.getLeft() : e.getRight()).source();
        for (ConfigurationOption option :
            (e.isLeft() ? e.getLeft() : e.getRight()).getConfigurationOptions()) {
          ConfigurationOption o = option.setSource(source + ": " + language);
          if (!analysisConfiguration.contains(o)) analysisConfiguration.add(o);
        }
      }
    }
  }

  public List<ConfigurationAction> getConfigurationActions() {
    List<ConfigurationAction> actions = new ArrayList<>();
    for (Entry<String, Collection<Either<ServerAnalysis, ToolAnalysis>>> entry :
        languageAnalyses.entrySet()) {
      String language = entry.getKey();
      Collection<Either<ServerAnalysis, ToolAnalysis>> analyses = entry.getValue();
      for (Either<ServerAnalysis, ToolAnalysis> e : analyses) {
        String source = (e.isLeft() ? e.getLeft() : e.getRight()).source();
        if (config.addDefaultActions()) {
          actions.add(
              new ConfigurationAction(
                      "Run Analysis",
                      () -> {
                        this.cleanUp();
                        this.doSingleAnalysis(language, e, true);
                      })
                  .setSource(source + ": " + language));
        }
        for (ConfigurationAction action :
            (e.isLeft() ? e.getLeft() : e.getRight()).getConfiguredActions()) {
          actions.add(action.setSource(source + ": " + language));
        }
      }
    }
    return actions;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.lsp4j.services.LanguageServer#shutdown()
   */
  @Override
  public CompletableFuture<Object> shutdown() {
    for (SourceFileManager sfm : this.languageSourceFileManagers.values()) {
      sfm.cleanUp();
    }
    for (String lang : languageAnalyses.keySet()) {
      for (Either<ServerAnalysis, ToolAnalysis> analysis : languageAnalyses.get(lang)) {
        if (analysis.isLeft()) {
          analysis.getLeft().cleanUp();
        } else {
          analysis.getRight().cleanUp();
        }
      }
    }
    this.client = null;
    this.clientConfig = null;
    this.codeActions = null;
    this.codeLenses = null;
    this.config = null;
    this.diagnostics = null;
    this.hovers = null;
    this.languageAnalyses = null;
    this.languageProjectServices = null;
    this.languageSourceFileManagers = null;
    this.rootPath = null;
    this.serverClientUri = null;
    this.textDocumentService = null;
    this.workspaceService = null;
    this.falsePositiveHandler = null;
    this.confusionHandler = null;
    logger.cleanUp();
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
    MagpieServer.ExceptionLogger.cleanUp();
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
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
          language,
          new SourceFileManager(
              language, this.serverClientUri, this.config.getLanguageExtensionHandler()));
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

  public void addVersionControlService(VersionControlService versionControlService) {
    this.versionControlService = Optional.of(versionControlService);
  }

  public void addCommand(String commandName, WorkspaceCommand command) {
    getWorkspaceService().addCommand(commandName, command);
  }

  /**
   * Adds the analysis for different languages running on the server. This should be specified by
   * the user of MagpieServer.<br>
   * An example for adding a user-defined analysis.
   *
   * <pre>
   * <code>
   * MagpieServer server = new MagpieServer(new ServerConfiguration());
   * String language = "java";
   * ServerAnalysis myAnalysis = new MyAnalysis();
   * Either&#60;ServerAnalysis, ToolAnalysis&#62; analysis=Either.forLeft(myAnalysis);
   * server.addAnalysis(analysis,language);
   * </code>
   * </pre>
   *
   * @param analysis the analysis
   * @param languages the languages handled by this analysis
   */
  public void addAnalysis(Either<ServerAnalysis, ToolAnalysis> analysis, String... languages) {
    for (String language : languages) {
      if (!languageAnalyses.containsKey(language)) {
        languageAnalyses.put(language, new HashSet<>());
      }
      languageAnalyses.get(language).add(analysis);
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
    if (!languageAnalyses.containsKey(language)) {
      languageAnalyses.put(language, Collections.emptyList());
    }
    for (Either<ServerAnalysis, ToolAnalysis> analysis : languageAnalyses.get(language)) {
      doSingleAnalysis(language, analysis, rerun);
    }
  }

  protected void doSingleAnalysis(
      String language, Either<ServerAnalysis, ToolAnalysis> analysis, boolean rerun) {
    SourceFileManager fileManager = getSourceFileManager(language);
    Analysis<AnalysisConsumer> a = null;
    if (analysis.isLeft()) {
      a = analysis.getLeft();
    } else {
      a = analysis.getRight();
    }
    if (a != null) {
      if (config.isVerbose()) {
        this.forwardMessageToClient(
            new MessageParams(MessageType.Info, a.source() + " started analyzing the code."));
      }
      a.analyze(fileManager.getSourceFileModules().values(), this, rerun);
      if (config.isVerbose()) {
        this.forwardMessageToClient(
            new MessageParams(MessageType.Info, a.source() + " finished analyzing the code."));
      }
    }
  }

  /**
   * This method removes the given Diagnostic from the given uri in the client.
   *
   * @param uri the uri of the source code file
   * @param jdiag the jsonfied dagnostic
   */
  public void removeDiagnosticFromClient(String uri, JsonObject jdiag) {
    Gson gson = new Gson();
    Diagnostic diag = gson.fromJson(jdiag, Diagnostic.class);
    if (uri.startsWith("file:///")) uri = uri.replace("file:///", "file:/");
    try {
      List<Diagnostic> diags = this.diagnostics.get(new URL(uri));
      // We only compare the code range and message.
      List<Diagnostic> updated =
          diags.stream()
              .filter(
                  d ->
                      !(d.getRange().equals(diag.getRange())
                          && d.getMessage().equals(diag.getMessage())))
              .collect(Collectors.toList());
      PublishDiagnosticsParams pdp = new PublishDiagnosticsParams();
      pdp.setDiagnostics(updated);
      pdp.setUri(uri);
      client.publishDiagnostics(pdp);
    } catch (MalformedURLException e) {
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
  }

  /**
   * Consume the analysis results.
   *
   * @param results the results
   * @param source the source
   */
  @Override
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
            boolean isFP = falsePositiveHandler.isFalsePositive(result);
            boolean isSuppressed = suppressWarningHandler.isSuppressed(result);
            if (!isFP && !isSuppressed) {
              resultsConsumerFactory
                  .createDiagnosticConsumer(publishDiags, diagList, source)
                  .accept(result);
            }
            break;
          case Hover:
            resultsConsumerFactory.createHoverConsumer().accept(result);
            break;
          case CodeLens:
            resultsConsumerFactory.createCodeLensConsumer().accept(result);
            break;
          default:
            break;
        }
      } catch (MalformedURLException e) {
        MagpieServer.ExceptionLogger.log(e);
        e.printStackTrace();
      }
    }
    for (Entry<String, List<Diagnostic>> entry : publishDiags.entrySet()) {
      List<Diagnostic> diagList = entry.getValue();
      PublishDiagnosticsParams pdp = new PublishDiagnosticsParams();
      pdp.setDiagnostics(diagList);
      pdp.setUri(entry.getKey());
      client.publishDiagnostics(pdp);
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
  public MagpieWorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  /** @return the {@link FalsePositiveHandler} configured for Server. */
  public FalsePositiveHandler getFalsePositiveHandler() {
    return this.falsePositiveHandler;
  }

  public SuppressWarningHandler getSuppressWarningHandler() {
    return this.suppressWarningHandler;
  }

  /** @return the {@link ConfusionHandler} configured for Server. */
  public ConfusionHandler getConfusionHandler() {
    return this.confusionHandler;
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

  public Optional<VersionControlService> getVersionControlService() {
    return this.versionControlService;
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

  protected void addCodeLens(URL url, CodeLens codeLens) {
    if (!this.codeLenses.containsKey(url)) {
      this.codeLenses.put(url, new ArrayList<>());
    }
    List<CodeLens> lenses = this.codeLenses.get(url);
    lenses.add(codeLens);
  }

  /**
   * Gets the client uri.
   *
   * @param serverUri the server uri
   * @return the client uri
   */
  public String getClientUri(String serverUri) {
    serverUri = URIUtils.checkURI(serverUri);
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
        MagpieServer.ExceptionLogger.log(e);
        e.printStackTrace();
      }
    }
    return clientUri;
  }

  /**
   * Find hover for the given lookup position.
   *
   * @param lookupPos the lookup pos
   * @return the hover
   */
  protected Hover findHover(Position lookupPos) {
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
   * Find code lenses for the given uri.
   *
   * @param uri the uri
   * @return the list of code lenses for the given uri.
   */
  protected List<CodeLens> findCodeLenses(URI uri) {
    try {
      if (this.codeLenses.containsKey(uri.toURL())) {
        return this.codeLenses.get(uri.toURL());
      }
    } catch (MalformedURLException e) {
      MagpieServer.ExceptionLogger.log(e);
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
  protected List<CodeAction> findCodeActions(URI uri, CodeActionParams params) {
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
      MagpieServer.ExceptionLogger.log(e);
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  /** Clean up all analysis results. */
  public void cleanUp() {
    for (URL url : diagnostics.keySet()) {
      String clientUri = getClientUri(url.toString());
      if (clientUri != null) {
        client.publishDiagnostics(new PublishDiagnosticsParams(clientUri, Collections.emptyList()));
      }
    }
    this.diagnostics.clear();
    this.codeActions.clear();
    this.hovers.clear();
    this.codeLenses.clear();
  }

  /**
   * This method allows to submit new runnable task to the thread pool of the server.
   *
   * @param task the runnable task which should be ran by the server.
   */
  public void submittNewTask(Runnable task) {
    THREAD_POOL.submit(task);
  }

  /** @return true if the client supports showing HTML page. */
  public boolean clientSupportShowHTML() {
    return clientConfig != null
        && clientConfig.getExperimental() instanceof JsonObject
        && ((JsonObject) clientConfig.getExperimental()).has("supportsShowHTML")
        && ((JsonObject) clientConfig.getExperimental()).get("supportsShowHTML").getAsBoolean();
  }

  /** @return true if the client supports showing input box. */
  public boolean clientSupportShowInputBox() {
    return clientConfig != null
        && clientConfig.getExperimental() instanceof JsonObject
        && ((JsonObject) clientConfig.getExperimental()).has("supportsShowInputBox")
        && ((JsonObject) clientConfig.getExperimental()).get("supportsShowInputBox").getAsBoolean();
  }

  /**
   * Set up the analyses based on the configuration options chosen by the users in the configuration
   * page. This is an advanced feature and is only used when {@link
   * ServerConfiguration#showConfigurationPage()} returns true.
   *
   * @param requestOptions configuration options chosen by the user
   * @return new configuration which is set up based on the given options.
   */
  public List<ConfigurationOption> setConfigurationOptions(Map<String, String> requestOptions) {
    List<ConfigurationOption> configuration = getAnalysisConfiguration();
    setConfigurationOptionsRecursively(configuration, requestOptions);
    languageAnalyses
        .values()
        .forEach(
            a ->
                a.forEach(
                    e -> {
                      (e.isLeft() ? e.getLeft() : e.getRight()).configure(configuration);
                    }));
    initAnalysisConfiguration();
    return getAnalysisConfiguration();
  }

  protected void setConfigurationOptionsRecursively(
      List<? extends ConfigurationOption> configuration, Map<String, String> requestOptions) {
    for (ConfigurationOption option : configuration) {
      if (requestOptions.containsKey(option.getName())) {
        String value = requestOptions.get(option.getName());
        ((ConfigurationOption) option).setValue(value);
      } else {
        if (option.getType().equals(OptionType.checkbox)) option.setValue("off");
      }
      if (option.hasChildren()) {
        setConfigurationOptionsRecursively(option.getChildren(), requestOptions);
      }
    }
  }

  /**
   * Perform an action based on the user interaction in the configuration page. This is an advanced
   * feature and is only used when {@link ServerConfiguration#showConfigurationPage()} returns true.
   *
   * @param actionName name of the action chosen by the user.
   * @param sourceName source (analysis name) of the action.
   */
  public void performConfiguredAction(NameValuePair actionName, NameValuePair sourceName) {
    for (ConfigurationAction action : getConfigurationActions()) {
      if (action.getName().equals(actionName.getValue())
          && action.getSource().equals(sourceName.getValue())) submittNewTask(action.getAction());
    }
  }

  /**
   * Forward a message which should be shown in the client.
   *
   * @param message the message to be forwarded
   */
  public void forwardMessageToClient(MessageParams message) {
    if (client != null) client.showMessage(message);
  }

  /**
   * Forward MessageRequest which should be shown in the client.
   *
   * @param messageRequest the messageRequest to be forwarded
   * @return the response from the client.
   */
  public CompletableFuture<MessageActionItem> forwardMessageRequestToClient(
      ShowMessageRequestParams messageRequest) {
    if (client != null) return client.showMessageRequest(messageRequest);
    else return null;
  }

  /**
   * Show an input box in client to ask the user for input if the client supports showing input box.
   *
   * @param messages the messages to ask user
   * @return the user input
   */
  public CompletableFuture<Map<String, String>> showInputBoxInClient(List<String> messages) {
    if (clientSupportShowInputBox()) return client.showInputBox(messages);
    else return null;
  }

  /**
   * add a http server to communicate with the magpie server.
   *
   * @param url the url of the http server
   */
  public void addHttpServer(String url) {
    httpserverUrl = url;
  }
}
