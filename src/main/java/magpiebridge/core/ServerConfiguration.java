package magpiebridge.core;

/**
 * Configuration class for {@link MagpieServer}.
 *
 * @author Linghui Luo
 */
public class ServerConfiguration {
  private boolean reportFalsePositive;
  private boolean reportConfusion;
  private boolean doPreparation;

  public ServerConfiguration() {
    this.reportFalsePositive = false;
    this.reportConfusion = false;
    this.doPreparation = false;
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

  public boolean doPreparation() {
    return doPreparation;
  }

  public void setDoPreparation(boolean doPreparation) {
    this.doPreparation = doPreparation;
  }
}
