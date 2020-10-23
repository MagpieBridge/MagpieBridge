package magpiebridge.core;

import com.google.gson.JsonObject;

public abstract class SuppressWarningHandler {
  protected MagpieServer server;

  /**
   * register the handler when server is initialized.
   *
   * @param server the server which uses this handler
   */
  public void registerAt(MagpieServer server) {
    this.server = server;
  }

  /**
   * Checks if the result is suppressed by user.
   *
   * @param result the result
   * @return true, if the result is suppressed by user.
   */
  public abstract boolean isSuppressed(AnalysisResult result);

  /**
   * Record warning suppressed by users.
   *
   * @param uri the uri of the reported warning
   * @param diag information about the reported warning
   */
  public abstract void recordSuppression(String uri, JsonObject diag);
}
