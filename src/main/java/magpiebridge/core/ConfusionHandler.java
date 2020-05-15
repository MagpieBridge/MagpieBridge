package magpiebridge.core;

import com.google.gson.JsonObject;

/**
 * This class should handle confusing diagnostics reported by the user.
 *
 * @author Linghui Luo
 */
public abstract class ConfusionHandler {

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
   * Record confusing diagnostics reported by users.
   *
   * @param uri the uri of the reported diagnostic
   * @param diagnostic information about the reported diagnostic
   */
  public abstract void recordConfusion(String uri, JsonObject diagnostic);
}
