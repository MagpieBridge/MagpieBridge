package magpiebridge.core;

import java.io.PrintWriter;
import java.util.function.Function;
import magpiebridge.util.MagpieMessageLogger;
import magpiebridge.util.MessageLogger;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Configuration class for {@link MagpieServer}. This class uses a Builder pattern.
 *
 * @author Linghui Luo
 */
public class ServerConfiguration {
  private boolean reportFalsePositive;
  private boolean reportConfusion;
  private boolean doAnalysisByOpen;
  private boolean doAnalysisBySave;
  private MagpieMessageLogger logger;
  private PrintWriter traceWriter;

  public ServerConfiguration() {
    this.reportFalsePositive = false;
    this.reportConfusion = false;
    this.doAnalysisByOpen = true;
    this.doAnalysisBySave = true;
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

  public ServerConfiguration setDoAnalysisByOpen(boolean doAnalysisByOpen) {
    this.doAnalysisByOpen = doAnalysisByOpen;
    return this;
  }

  public ServerConfiguration setDoAnalysisBySave(boolean doAnalysisBySave) {
    this.doAnalysisBySave = doAnalysisBySave;
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

  public PrintWriter traceWriter() {
    return this.traceWriter;
  }
}
