package magpiebridge.core.analysis.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.Kind;
import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * Converts SARIF JSON to analysis results
 *
 */
public class SARIFElement {
  private List<AnalysisResult> analysisResults;

  public SARIFElement(JsonObject sarif) throws MalformedURLException {
    this.analysisResults = new ArrayList<AnalysisResult>();
    JsonArray runs = sarif.get("runs").getAsJsonArray();
    JsonArray results = new JsonArray();
    JsonObject run = new JsonObject();
    JsonObject result = new JsonObject();
    for (int i = 0; i < runs.size(); i++) {
      run = runs.get(i).getAsJsonObject();
      results = run.get("results").getAsJsonArray();
      for (int j = 0; j < results.size(); j++) {
        result = results.get(j).getAsJsonObject();
        insertAnalysisResult(result);
      }
    }
  }

  public List<AnalysisResult> getAnalysisResults() {
    return analysisResults;
  }

  private void insertAnalysisResult(JsonObject result) throws MalformedURLException {
    // TODO: Ensuring check for not present parameter according to the required list
    JsonObject message = result.get("message").getAsJsonObject();
    JsonArray locations = result.get("locations").getAsJsonArray();
    String messageText = message.get("text").getAsString();
    Position errorPosition = locationToPosition(locations.get(0).getAsJsonObject());
    List<Pair<Position, String>> relatedInfo = new ArrayList<>();
    String code = locationToCode(locations.get(0).getAsJsonObject());
    JsonArray codeFlows = result.get("codeFlows").getAsJsonArray();
    JsonObject codeFlow = new JsonObject();
    JsonArray threadFlows = new JsonArray();
    JsonObject threadFlow = new JsonObject();
    JsonArray threadFlowLocations = new JsonArray();

    for (int i = 0; i < codeFlows.size(); i++) {
      codeFlow = codeFlows.get(i).getAsJsonObject();
      threadFlows =
          notNullAndHas(codeFlow, "threadFlows")
              ? codeFlow.get("threadFlows").getAsJsonArray()
              : null;
      for (int j = 0; j < threadFlows.size(); j++) {
        threadFlow = threadFlows.get(j).getAsJsonObject();
        threadFlowLocations =
            notNullAndHas(threadFlow, "locations")
                ? threadFlow.get("locations").getAsJsonArray()
                : null;
        if (threadFlowLocations != null) {
          relatedInfo = locationsToFlow(threadFlowLocations);
          if (code == null && relatedInfo != null && relatedInfo.size() != 0) {
            int lastLocation = relatedInfo.size() - 1;
            code = relatedInfo.get(lastLocation).snd;
          }

          analysisResults.add(
              new SARIFResult(
                  Kind.Diagnostic,
                  errorPosition,
                  messageText,
                  relatedInfo,
                  DiagnosticSeverity.Error,
                  null,
                  code));
        }
      }
    }
  }

  private List<Pair<Position, String>> locationsToFlow(JsonArray locations)
      throws MalformedURLException {
    List<Pair<Position, String>> flows = new ArrayList<>();
    JsonObject locationWrap = new JsonObject();
    JsonObject location = new JsonObject();
    String code;

    for (int i = 0; i < locations.size(); i++) {
      locationWrap = locations.get(i).getAsJsonObject();
      location = locationWrap.get("location").getAsJsonObject();
      code = locationToCode(location);
      Position position = locationToPosition(location);
      Pair<Position, String> pair = Pair.make(position, code);
      flows.add(pair);
    }

    return flows.isEmpty() ? null : flows;
  }

  private Position locationToPosition(JsonObject location) throws MalformedURLException {
    JsonObject phsicalLocation =
        notNullAndHas(location, "physicalLocation")
            ? location.getAsJsonObject("physicalLocation")
            : null;
    JsonObject artifactLocation =
        notNullAndHas(phsicalLocation, "artifactLocation")
            ? phsicalLocation.getAsJsonObject("artifactLocation")
            : null;
    JsonObject region =
        notNullAndHas(phsicalLocation, "region") ? phsicalLocation.getAsJsonObject("region") : null;

    int firstLine = notNullAndHas(region, "startLine") ? region.get("startLine").getAsInt() : 0;
    int lastLine = notNullAndHas(region, "endLine") ? region.get("endLine").getAsInt() : 0;
    int firstCol = notNullAndHas(region, "startColumn") ? region.get("startColumn").getAsInt() : 0;
    int lastCol = notNullAndHas(region, "endColumn") ? region.get("endColumn").getAsInt() : 0;
    URL url =
        notNullAndHas(artifactLocation, "uri")
            ? new URL(artifactLocation.get("uri").getAsString())
            : null;
    SARIFCodePosition position = new SARIFCodePosition(firstLine, firstCol, lastLine, lastCol, url);
    return position;
  }

  private String locationToCode(JsonObject location) {
    JsonObject message = location.has("message") ? location.getAsJsonObject("message") : null;
    String text = notNullAndHas(message, "text") ? message.get("text").toString() : null;
    return text;
  }

  private boolean notNullAndHas(JsonObject obj, String property) {
    return obj != null && obj.has(property);
  }
}
