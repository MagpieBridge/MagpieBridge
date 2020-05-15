package magpiebridge.core;

import java.io.PrintWriter;
import java.util.function.Function;
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
  private boolean reportFalsePositive;
  private boolean reportConfusion;
  private boolean doAnalysisByOpen;
  private boolean doAnalysisBySave;
  private boolean doAnalysisByIdle;
  private boolean showConfigurationPage;
  private long timeOut; // timeout in millisecond

  private MagpieMessageLogger logger;
  private PrintWriter traceWriter;

  public ServerConfiguration() {
    this.doAnalysisByOpen = true;
    this.doAnalysisBySave = true;
    this.doAnalysisByIdle = false;
    this.reportFalsePositive = false;
    this.reportConfusion = false;
    this.showConfigurationPage = false;
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

  public ServerConfiguration setReportFalsePositive(boolean reportFalsePositive) {
    this.reportFalsePositive = reportFalsePositive;
    return this;
  }

  public boolean reportConfusion() {
    return reportConfusion;
  }

  public ServerConfiguration setReportConfusion(boolean reportConfusion) {
    this.reportConfusion = reportConfusion;
    return this;
  }

  public ServerConfiguration setShowConfigurationPage(boolean showConfigurationPage) {
    this.showConfigurationPage = showConfigurationPage;
    return this;
  }

  /**
   * Set up the server to run analysis whenever a source file is opened.
   *
   * @param doAnalysisByOpen true, if the server runs the analysis when the first time a source file
   *     is opened at in an editor. The default value is true.
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
   * Set up the server to run analysis when the user has been idle(doing nothing in the editor) for
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

  public MagpieMessageLogger getMagpieMessageLogger() {
    return this.logger;
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
}
