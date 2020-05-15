package magpiebridge.core;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Default {@link FalsePositiveHandler} supported by the {@link MagpieServer}.
 *
 * @author Linghui Luo
 */
public class DefaultFalsePositiveHandler extends FalsePositiveHandler {
  /** The false positives reported by users. */
  protected Map<String, Set<Triple<Integer, String, String>>> falsePositives;

  public DefaultFalsePositiveHandler() {
    this.falsePositives = new HashMap<>();
  }

  /**
   * Checks if the result was reported as false positive by comparing code and message.
   *
   * @param result the result
   * @return true, if the result was reported as false positive
   */
  @Override
  public boolean isFalsePositive(AnalysisResult result) {
    String serverUri = result.position().getURL().toString();
    String clientUri = server.getClientUri(serverUri);
    for (String uri : falsePositives.keySet()) {
      if (uri.equals(clientUri)) {
        for (Triple<Integer, String, String> fp : falsePositives.get(clientUri)) {
          int diff = Math.abs((result.position().getFirstLine() + 1) - fp.getLeft());
          int threshold = 5;
          if (diff < threshold
              && result.code().equals(fp.getMiddle())
              && result.toString(false).equals(fp.getRight())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public void recordFalsePositive(String uri, JsonObject diagnostic) {
    // just record start line number, code and message to identify diagnostic.
    if (diagnostic.has("range") && diagnostic.has("code") && diagnostic.has("message")) {
      int lineNumber =
          diagnostic
              .get("range")
              .getAsJsonObject()
              .get("start")
              .getAsJsonObject()
              .get("line")
              .getAsInt();
      String code = diagnostic.get("code").getAsString();
      String message = diagnostic.get("message").getAsString();
      Triple<Integer, String, String> diag = Triple.of(lineNumber, code, message);
      if (!falsePositives.containsKey(uri)) {
        this.falsePositives.put(uri, new HashSet<Triple<Integer, String, String>>());
      }
      Set<Triple<Integer, String, String>> dias = this.falsePositives.get(uri);
      dias.add(diag);
      this.falsePositives.put(uri, dias);
    }
  }
}
