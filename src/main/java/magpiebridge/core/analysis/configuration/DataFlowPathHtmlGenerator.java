package magpiebridge.core.analysis.configuration;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import java.io.IOException;
import java.util.HashMap;
import magpiebridge.core.AnalysisResult;
import magpiebridge.util.SourceCodeReader;

/**
 * The class generates a HTML page to show flow graph. It uses the flows() function of the
 * AnalysisResult.
 */
public class DataFlowPathHtmlGenerator {

  private static String serverAddress;

  public static String generateHTML(AnalysisResult result, String serverAddress)
      throws IOException {
    DataFlowPathHtmlGenerator.serverAddress = serverAddress;

    return html(generateHeader(), generateBody(result)).renderFormatted();
  }

  private static ContainerTag generetaH1Title() {
    return h1("Welcome to MagpieBridge Data Flow Page!");
  }

  private static ContainerTag generateGlobalScript() {
    String code =
        "if (typeof acquireVsCodeApi != 'undefined'){window.vscode = acquireVsCodeApi();}";
    return script(rawHtml(code));
  }

  private static ContainerTag generateDataFlowScriptConfiguration() {
    String code =
        "var cy = cytoscape({\r\n"
            + "  container: document.getElementById('cy'),"
            + "  style: [{\r\n"
            + "      selector: 'node',\r\n"
            + "      style: {\r\n"
            + "        label: 'data(value)',\r\n"
            + "        'text-valign': 'center',\r\n"
            + "        'text-halign': 'right',\r\n"
            + "        'text-margin-x': '-155',\r\n"
            + "        'text-wrap': 'wrap',\r\n"
            + "        'text-max-width': 150,\r\n"
            + "        'width': 180,\r\n"
            + "        'background-fit': 'contain',\r\n"
            + "        'background-color': '#007bff',\r\n"
            + "        'color': '#fff',\r\n"
            + "        'shape': 'roundrectangle',\r\n"
            + "        'height': 24,\r\n"
            + "        'border-width': 1,\r\n"
            + "        'padding-right': 5,\r\n"
            + "        'padding-left': 5,\r\n"
            + "        'padding-top': 5,\r\n"
            + "        'padding-bottom': 5,\r\n"
            + "        'text-events': 'yes',\r\n"
            + "        'font-size': 12,\r\n"
            + "      }\r\n"
            + "    },\r\n"
            + "    {\r\n"
            + "      selector: 'edge',\r\n"
            + "      style: {\r\n"
            + "        'width': 1,\r\n"
            + "        'curve-style': 'bezier',\r\n"
            + "        'line-color': 'black',\r\n"
            + "        'line-style': 'solid',\r\n"
            + "        'target-arrow-shape': 'triangle-backcurve',\r\n"
            + "        'target-arrow-color': 'black',\r\n"
            + "        'text-rotation': 'autorotate',\r\n"
            + "      }\r\n"
            + "    },\r\n"
            + "    {\r\n"
            + "      selector: '$node > node',\r\n"
            + "      style: {\r\n"
            + "        'text-rotation': '-90deg',\r\n"
            + "        'text-halign': 'left',\r\n"
            + "        'text-margin-x': -10,\r\n"
            + "        'text-margin-y': -40,\r\n"
            + "      }\r\n"
            + "    },\r\n"
            + "    {\r\n"
            + "      selector: '.Badge',\r\n"
            + "      style: {\r\n"
            + "        'border-width': 3,\r\n"
            + "      }\r\n"
            + "    },\r\n"
            + "  ],\r\n"
            + "  minZoom: 0.5,\r\n"
            + "  maxZoom: 1.5,\r\n"
            + "  zoomingEnabled: true,\r\n"
            + "  userZoomingEnabled: false,\r\n"
            + "  autoungrabify: false,\r\n"
            + "  autounselectify: true,"
            + "});";
    code +=
        "let options = {\r\n" + "	name: 'dagre'," + "	'nodeSep': 25," + "	'rankSep': 10," + "};";
    return script(rawHtml(code));
  }

  private static ContainerTag generateDataFlow(Iterable<Pair<Position, Position>> flows)
      throws IOException {
    String code = " cy.add([";
    HashMap<Position, Integer> existingNodes = new HashMap<Position, Integer>();
    String from = "";
    String to = "";
    Position fromPosition = null;
    Position toPosition = null;
    String value = "";
    int nodeCount = 0;
    int edgeCount = 0;

    for (Pair<Position, Position> flow : flows) {
      fromPosition = flow.fst;
      from = SourceCodeReader.getWholeCodeLineInString(fromPosition, false);
      toPosition = flow.snd;
      to = SourceCodeReader.getWholeCodeLineInString(toPosition, false);

      if (!existingNodes.containsKey(fromPosition)) {
        code +=
            getGraphElement(
                "nodes", new String[] {"id: 'n" + nodeCount + "', value: '" + from + "'"});

        existingNodes.put(fromPosition, nodeCount);
        nodeCount++;
      }

      if (!existingNodes.containsKey(toPosition)) {
        code +=
            getGraphElement(
                "nodes", new String[] {"id: 'n" + nodeCount + "', value: '" + to + "'"});

        existingNodes.put(toPosition, nodeCount);
        nodeCount++;
      }

      code +=
          getGraphElement(
              "edges",
              new String[] {
                "id: 'e" + edgeCount + "'",
                "source: 'n" + existingNodes.get(fromPosition) + "'",
                "target: 'n" + existingNodes.get(toPosition) + "'",
              });
      edgeCount++;
    }
    code += "]); cy.layout(options).run();";
    return script(rawHtml(code));
  }

  private static String getGraphElement(String group, String[] datas) {
    String element = " { group: '" + group + "', data: { ";
    for (String data : datas) {
      element += data + ", ";
    }
    element += " }}, ";
    return element;
  }

  private static DomContent generateBody(AnalysisResult result) throws IOException {
    return body(
        div(
                div(div(
                            generetaH1Title(),
                            div(
                                    generateGlobalScript(),
                                    div(
                                        h2("Data Flow"),
                                        div(div()
                                                .withId("cy")
                                                .withStyle(
                                                    "min-width: 600px; min-height: 600px; display: block;"))
                                            .withClass("col-md-12")))
                                .withClass("row"))
                        .withClass("center-block"))
                    .withClass("row"),
                generateDataFlowScriptConfiguration(),
                generateDataFlow(result.flows()))
            .withClass("container"));
  }

  private static DomContent generateHeader() {
    return head(
        title("MagpieBridge"),
        new UnescapedText(
            "<!-- Latest compiled and minified CSS -->\n"
                + "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">\n"
                + "\n"
                + "<!-- Optional theme -->\n"
                + "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css\" integrity=\"sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp\" crossorigin=\"anonymous\">\n"
                + "\n"
                + "<!-- Latest compiled and minified JavaScript -->\n"
                + "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>"
                + "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>"
                + "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.2.5/cytoscape.js\"></script>\r\n"
                + "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/dagre/0.8.5/dagre.min.js\"></script>\r\n"
                + "<script src=\"https://cdn.jsdelivr.net/npm/cytoscape-dagre@2.2.2/cytoscape-dagre.min.js\"></script>"));
  }
}
