package magpiebridge.core.analysis.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import java.io.IOException;
import magpiebridge.core.AnalysisResult;
import magpiebridge.util.SourceCodeReader;

/**
 * This class converts AnalysisResult to SARIF JSON format. It uses different functions to create
 * different part of SARIF format.
 */
public class SARIFConverter {
  private static final String SARIF_RULES_URL = "https://json.schemastore.org/sarif-2.1.0.json";
  private static final String STATIC_ANALYSIS_TOOL = "MagpieBridge";
  private static final String SARIF_VERSION = "2.1.0";

  private AnalysisResult analysisResult;

  public SARIFConverter(AnalysisResult analysisResult) {
    this.analysisResult = analysisResult;
  }

  /**
   * Used for AnalysisResult conversion to SARIF format
   *
   * @return SARIF format as JSON
   * @throws IOException happens by parsing file
   */
  public JsonObject makeSarif() throws IOException {
    JsonObject json = new JsonObject();
    JsonArray runs = new JsonArray();
    runs.add(this.getRun());
    json.addProperty("version", SARIFConverter.SARIF_VERSION);
    json.addProperty("$schema", SARIFConverter.SARIF_RULES_URL);
    json.add("runs", runs);
    return json;
  }

  private JsonObject getRun() throws IOException {
    JsonObject run = new JsonObject();
    JsonArray results = new JsonArray();

    results.add(this.getResult());
    run.add("tool", this.getTool());
    run.add("results", results);

    return run;
  }

  private JsonObject getTool() {
    JsonObject tool = new JsonObject();
    JsonObject driver = new JsonObject();
    driver.addProperty("name", SARIFConverter.STATIC_ANALYSIS_TOOL);
    tool.add("driver", driver);

    return tool;
  }

  private JsonObject getResult() throws IOException {
    JsonObject result = new JsonObject();
    JsonObject message = new JsonObject();
    JsonObject physicalLocation = new JsonObject();
    JsonArray codeFlows = new JsonArray();
    JsonArray locations = new JsonArray();

    message.addProperty("text", this.analysisResult.toString(true));
    physicalLocation.add("physicalLocation", getPhysicalLocation(this.analysisResult.position()));
    locations.add(physicalLocation);
    codeFlows.add(getCodeFlow());

    result.addProperty("ruleId", "ERROR"); // what will be the rule id it is very importent
    result.addProperty("level", this.analysisResult.severity().toString());
    result.add("message", message);
    result.add("locations", locations);
    result.add("codeFlows", codeFlows);

    return result;
  }

  private JsonObject getCodeFlow() throws IOException {
    JsonObject codeFlow = new JsonObject();
    JsonArray threadFlows = new JsonArray();
    threadFlows.add(getThreadFlows());
    codeFlow.add("threadFlows", threadFlows);

    return codeFlow;
  }

  private JsonObject getThreadFlows() throws IOException {
    JsonObject location = new JsonObject();
    JsonObject threadFlows = new JsonObject();
    JsonArray locations = new JsonArray();
    for (Pair<Position, String> flow : this.analysisResult.related()) {
      location = getLocation(flow.fst);

      locations.add(location);
    }

    threadFlows.add("locations", locations);

    return threadFlows;
  }

  private JsonObject getLocation(Position position) throws IOException {
    String code = SourceCodeReader.getWholeCodeLineInString(position, false);

    JsonObject location = new JsonObject();
    JsonObject message = new JsonObject();
    message.addProperty("text", code.trim());
    location.add("physicalLocation", getPhysicalLocation(position));
    location.add("message", message);

    JsonObject locationWrap = new JsonObject();
    locationWrap.add("location", location);

    return locationWrap;
  }

  private JsonObject getPhysicalLocation(Position position) {
    String url = position.getURL().toString();
    int startLine = position.getFirstLine();
    int startColumn = position.getFirstCol();
    int endLine = position.getLastLine();
    int endColumn = position.getLastCol();

    JsonObject artifactLocation = new JsonObject();
    artifactLocation.addProperty("uri", url);

    JsonObject region = new JsonObject();
    region.addProperty("startLine", startLine);
    region.addProperty("startColumn", startColumn);
    region.addProperty("endLine", endLine);
    region.addProperty("endColumn", endColumn);

    JsonObject physicalLocation = new JsonObject();
    physicalLocation.add("artifactLocation", artifactLocation);
    physicalLocation.add("region", region);

    return physicalLocation;
  }
}
