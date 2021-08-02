package magpiebridge.core;

import java.io.PrintWriter;
import java.util.function.Function;
import magpiebridge.core.analysis.configuration.MagpieHttpServer;
import magpiebridge.util.MagpieMessageLogger;
import magpiebridge.util.MessageLogger;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Configuration class for {@link MagpieServer}. This class uses a Builder pattern to set up each
 * option.
 *
 * @author Linghui Luo
 */
public class ServerConfiguration {
  private boolean supportWarningSuppression;
  private boolean reportFalsePositive;
  private boolean reportConfusion;
  private boolean doAnalysisByOpen;
  private boolean doAnalysisByFirstOpen;
  private boolean doAnalysisBySave;
  private boolean doAnalysisByIdle;
  private boolean showConfigurationPage;
  private boolean showSarifFileUploadPage;
  private boolean addDefaultActions;
  private FalsePositiveHandler falsePositiveHandler;
  private ConfusionHandler confusionHandler;
  private SuppressWarningHandler suppressWarningHandler;
  private long timeOut; // timeout in millisecond

  private MagpieMessageLogger logger;
  private PrintWriter traceWriter;
  private boolean useMagpieHTTPServer;

  public ServerConfiguration() {
    this.doAnalysisByFirstOpen = true;
    this.doAnalysisBySave = true;
    this.useMagpieHTTPServer = true;
    this.doAnalysisByOpen = false;
    this.doAnalysisByIdle = false;
    this.supportWarningSuppression = false;
    this.reportFalsePositive = false;
    this.reportConfusion = false;
    this.showConfigurationPage = false;
    this.showSarifFileUploadPage = false;
    this.addDefaultActions = false;
    this.suppressWarningHandler = new DefaultSupressWarningHandler();
    this.falsePositiveHandler = new DefaultFalsePositiveHandler();
    this.confusionHandler = new DefaultConfusionHandler();
    this.timeOut = 0;
    // default no-op logger
    this.logger =
        new MagpieMessageLogger() {

          @Override
          public Function<MessageConsumer, MessageConsumer> getWrapper() {
            return c -> c;
          }

          @Override
          public void cleanUp() {}
        };
  }

  public boolean reportFalsePositive() {
    return reportFalsePositive;
  }

  /**
   * Set up {@link DefaultFalsePositiveHandler} for handling false-positive diagnostics reported by
   * user.
   *
   * @param reportFalsePositive true, if reporting false-positive diagnostics is enabled
   * @return the server configuration
   */
  public ServerConfiguration setReportFalsePositive(boolean reportFalsePositive) {
    this.reportFalsePositive = reportFalsePositive;
    return this;
  }

  /**
   * Set up handler for false-positive diagnostics reported by user.
   *
   * @param reportFalsePositive true, if reporting false-positive diagnostics is enabled
   * @param handler the handler for false-positive diagnostics
   * @return the server configuration
   */
  public ServerConfiguration setReportFalsePositive(
      boolean reportFalsePositive, FalsePositiveHandler handler) {
    this.reportFalsePositive = reportFalsePositive;
    this.falsePositiveHandler = handler;
    return this;
  }

  public boolean reportConfusion() {
    return reportConfusion;
  }

  public boolean supportWarningSuppression() {
    return supportWarningSuppression;
  }

  /**
   * Set up handler for suppressing warnings.
   *
   * @param supportWarningSuppression true, if warning suppression should be supported
   * @param handler the handler for suppressing warnings.
   * @return the server configuration
   */
  public ServerConfiguration setSuppressWarningHandler(
      boolean supportWarningSuppression, SuppressWarningHandler handler) {
    this.supportWarningSuppression = supportWarningSuppression;
    this.suppressWarningHandler = handler;
    return this;
  }

  /**
   * Set up {@link DefaultConfusionHandler} for handling confusing diagnostics reported by user.
   *
   * @param reportConfusion true, if reporting confusing diagnostics is enabled
   * @return the server configuration
   */
  public ServerConfiguration setReportConfusion(boolean reportConfusion) {
    this.reportConfusion = reportConfusion;
    return this;
  }
  /**
   * Set up handler for confusing diagnostics reported by user.
   *
   * @param reportConfusion true, if reporting confusing diagnostics is enable
   * @param confusionHandler the handler for confusing diagnostics
   * @return the server configuration
   */
  public ServerConfiguration setConfusionHandler(
      boolean reportConfusion, ConfusionHandler confusionHandler) {
    this.confusionHandler = confusionHandler;
    return this;
  }

  /**
   * Set up the server to start a configuration page (HTML page in client or browser) after
   * initialization. If you want to use your own HTTP server, use {@link
   * MagpieServer#addHttpServer(String)} to set up the URL.
   *
   * @param showConfigurationPage true, if the server should start a configuration page. The default
   *     value is false.
   * @param addDefaultActions true, if the server should add default action button <code>
   *     Run Analysis</code> to the default configuration page.
   * @return the server configuration
   */
  public ServerConfiguration setShowConfigurationPage(
      boolean showConfigurationPage, boolean addDefaultActions) {
    this.showConfigurationPage = showConfigurationPage;
    this.addDefaultActions = addDefaultActions;
    return this;
  }
  /**
   * @param showSarifFileUploadPage
   * @return
   */
  public ServerConfiguration setShowSarifFileUploadPage(boolean showSarifFileUploadPage) {
    this.showSarifFileUploadPage = showSarifFileUploadPage;
    return this;
  }

  /**
   * Set up the server to run analysis when the first time a source file is opened at in an editor.
   *
   * @param doAnalysisByFirstOpen true, if the server runs the analysis when the first time a source
   *     file is opened at in an editor. The default value is true.
   * @return the server configuration
   */
  public ServerConfiguration setDoAnalysisByFirstOpen(boolean doAnalysisByFirstOpen) {
    this.doAnalysisByFirstOpen = doAnalysisByFirstOpen;
    return this;
  }

  /**
   * Set up the server to run analysis whenever a source file is opened at in an editor.
   *
   * @param doAnalysisByOpen true, if the server runs the analysis whenever a file is opened. The
   *     default value is false.
   * @return the server configuration
   */
  public ServerConfiguration setDoAnalysisByOpen(boolean doAnalysisByOpen) {
    this.doAnalysisByOpen = doAnalysisByOpen;
    return this;
  }
  /**
   * Set up the server to run analysis whenever a source file is saved.
   *
   * @param doAnalysisBySave true, if the server runs the analysis whenever a source file is saved.
   *     The default value is true.
   * @return the server configuration
   */
  public ServerConfiguration setDoAnalysisBySave(boolean doAnalysisBySave) {
    this.doAnalysisBySave = doAnalysisBySave;
    if (this.doAnalysisBySave) this.doAnalysisByIdle = false;
    return this;
  }

  /**
   * Set up the server to run analysis when the user has been idle (doing nothing in the editor) for
   * the given time period; and all changed source files have been saved.
   *
   * @param doAnalysisByIdle true, if the server runs the analysis when the user is idle. The
   *     default value is false. When this parameter is set to true, the doAnalysisBySave option
   *     will be automatically set to false.
   * @param timeOut the time to wait for idle
   * @return the server configuration
   */
  public ServerConfiguration setDoAnalysisByIdle(boolean doAnalysisByIdle, long timeOut) {
    this.doAnalysisByIdle = doAnalysisByIdle;
    if (this.doAnalysisByIdle) this.doAnalysisBySave = false;
    this.timeOut = timeOut;
    return this;
  }

  /**
   * Set up the MagpieMessageLogger for logging messages in any format you can define.The default
   * logger does nothing. Use {@link MessageLogger} to log messages into your temporary directory.
   *
   * @param logger the MagpieMessageLogger
   * @return the ServerConfiguration with the logger set up
   */
  public ServerConfiguration setMagpieMessageLogger(@NonNull MagpieMessageLogger logger) {
    this.logger = logger;
    return this;
  }

  /**
   * Set up the LSPMessageTracer for only logging LSP messages in a way that the LSP Inspector can
   * parse. https://microsoft.github.io/language-server-protocol/inspector/
   *
   * @param writer the PrintWriter for writing the trace
   * @return the ServerConfiguration with the tracer set up
   */
  public ServerConfiguration setLSPMessageTracer(PrintWriter writer) {
    this.traceWriter = writer;
    return this;
  }

  /**
   * Set up the server to communicate with {@link MagpieHttpServer}.
   *
   * @param useMagpieHTTPServer true if server communicates with {@link MagpieHttpServer}, otherwise
   *     use {@link MagpieServer#addHttpServer(String)} to add server url.
   * @return the ServerConfiguration with the option set up
   */
  public ServerConfiguration setUseMagpieHTTPServer(boolean useMagpieHTTPServer) {
    this.useMagpieHTTPServer = useMagpieHTTPServer;
    return this;
  }

  public MagpieMessageLogger getMagpieMessageLogger() {
    return this.logger;
  }

  public boolean doAnalysisByFirstOpen() {
    return this.doAnalysisByFirstOpen;
  }

  public boolean doAnalysisByOpen() {
    return this.doAnalysisByOpen;
  }

  public boolean doAnalysisBySave() {
    return this.doAnalysisBySave;
  }

  public boolean doAnalysisByIdle() {
    return this.doAnalysisByIdle;
  }

  public long timeOut() {
    return timeOut;
  }

  public PrintWriter traceWriter() {
    return this.traceWriter;
  }

  public boolean showConfigurationPage() {
    return this.showConfigurationPage;
  }

  public FalsePositiveHandler getFalsePositiveHandler() {
    return this.falsePositiveHandler;
  }

  public ConfusionHandler getConfusionHanlder() {
    return this.confusionHandler;
  }

  public boolean addDefaultActions() {
    return this.addDefaultActions;
  }

  public SuppressWarningHandler getSuppressWarningHandler() {
    return this.suppressWarningHandler;
  }

  public boolean useMagpieHTTPServer() {
    return this.useMagpieHTTPServer;
  }

  public boolean showSarifFileUploadPage() {
    return this.showSarifFileUploadPage;
  }
}
