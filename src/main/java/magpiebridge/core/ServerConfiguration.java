package magpiebridge.core;

/**
 * Configuration class for {@link MagpieServer}.
 *
 * @author Linghui Luo
 */
public class ServerConfiguration {
  private boolean reportFalsePositive;
  private boolean reportConfusion;

  public ServerConfiguration() {
    this.reportFalsePositive = false;
    this.reportConfusion = false;
  }

  public boolean reportFalsePositive() {
    return reportFalsePositive;
  }

  public void setReportFalsePositive(boolean reportFalsePositive) {
    this.reportFalsePositive = reportFalsePositive;
  }

  public boolean reportConfusion() {
    return reportConfusion;
  }

  public void setReportConfusion(boolean reportConfusion) {
    this.reportConfusion = reportConfusion;
  }
}
