package magpiebridge.core;

import com.google.gson.JsonObject;

/**
 * This class should handle false-positive diagnostics reported by the user. Once a diagnostic is
 * reported as false positive, the {@link MagpieServer} won't report it next time.
 *
 * @author Linghui Luo
 */
public abstract class FalsePositiveHandler {
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
   * Checks if the result was once reported as false positive. This is called every time a
   * diagnostic is reported to the server.
   *
   * @param result the result
   * @return true, if the result was reported as false positive
   */
  public abstract boolean isFalsePositive(AnalysisResult result);

  /**
   * Record false positive reported by users.
   *
   * @param uri the uri of the reported diagnostic
   * @param diag information about the reported diagnostic
   */
  public abstract void recordFalsePositive(String uri, JsonObject diag);
}
