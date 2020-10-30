package magpiebridge.core;

import com.google.gson.JsonObject;

/** The DefaultSupressWarningHandler only suppress the warning for once. */
public class DefaultSupressWarningHandler extends SuppressWarningHandler {
  @Override
  public boolean isSuppressed(AnalysisResult result) {
    return false;
  }

  @Override
  public void recordSuppression(String uri, JsonObject diag) {
    this.server.removeDiagnosticFromClient(uri, diag);
  }
}
