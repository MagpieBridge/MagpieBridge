package magpiebridge.core;

/**
 * Configuration class for {@link MagpieServer}.
 *
 * @author Linghui Luo
 */
public class ServerConfiguration {
  private boolean reportFalsePositive;
  private boolean reportConfusion;
  private boolean doAnalysisByOpen;
  private boolean doAnalysisBySave;

  public ServerConfiguration() {
    this.reportFalsePositive = false;
    this.reportConfusion = false;
    this.doAnalysisByOpen = true;
    this.doAnalysisBySave = true;
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

  public void setDoAnalysisByOpen(boolean doAnalysisByOpen) {
    this.doAnalysisByOpen = doAnalysisByOpen;
  }

  public void setDoAnalysisBySave(boolean doAnalysisBySave) {
    this.doAnalysisBySave = doAnalysisBySave;
  }

  public boolean doAnalysisByOpen() {
    return this.doAnalysisByOpen;
  }

  public boolean doAnalysisBySave() {
    return this.doAnalysisBySave;
  }
}
