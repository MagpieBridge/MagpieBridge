package magpiebridge.core.analysis.configuration;

import static j2html.TagCreator.body;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.p;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;
import static j2html.TagCreator.span;
import static j2html.TagCreator.title;
import static j2html.TagCreator.ul;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.FlowCodePosition;
import magpiebridge.util.SourceCodeReader;

/**
 * The class generates a HTML page to show flow graph. It uses the related() function of the
 * AnalysisResult.
 */
public class DataFlowPathHtmlGenerator {

  private static String serverAddress;
  private static ContainerTag dataFlowGraph;
  private static UnescapedText sidebarList;
  private static String lastNodeId;
  private static List<String> previousTaintedVariable = new ArrayList<String>();

  public static String generateHTML(AnalysisResult result, String serverAddress)
      throws IOException {
    DataFlowPathHtmlGenerator.serverAddress = serverAddress;
    dataFlowGraph = script(rawHtml(""));
    sidebarList = rawHtml("");

    return html(generateHeader(), generateBody(result)).renderFormatted();
  }

  private static ContainerTag generetaH1Title() {
    return h3("Welcome to MagpieBridge Data Flow Page!");
  }

  private static ContainerTag sidebarHeader() {
    return h3("Project Information");
  }

  private static ContainerTag generateGlobalScript() {
    String code =
        "if (typeof acquireVsCodeApi != 'undefined'){window.vscode = acquireVsCodeApi();}";
    return script(rawHtml(code));
  }

  private static ContainerTag generateSidebarCollapseScript() {
    String code =
        "$(document).ready(function () {\r\n"
            + "    $('#sidebarCollapse').on('click', function () {\r\n"
            + "    $('#sidebar').toggleClass('active');\r\n"
            + "    $('#content').toggleClass('active');\r\n"
            + "  });\r\n"
            + "});";
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
            + "        'text-halign': 'center',\r\n"
            + "        'text-wrap': 'wrap',\r\n"
            + "        'text-max-width': 260,\r\n"
            + "        'width': 280,\r\n"
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
            + "		   'label': 'data(label)',\r\n"
            + "        'color':'red',\r\n"
            + "        'font-size': '12px',"
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
            + "  userZoomingEnabled: true,\r\n"
            + "  autoungrabify: false,\r\n"
            + "  autounselectify: true,"
            + "  grabbable: false, \r\n"
            + "  pannable: false, \r\n"
            + "  locked: true,"
            + "});";
    code +=
        "let options = {\r\n" + "	name: 'dagre'," + "	'nodeSep': 25," + "	'rankSep': 25," + "};";
    code += "cy.fit();";
    return script(rawHtml(code));
  }

  private static ContainerTag generateSidebarLinkClickScript() {
    String code =
        "$(\".node-line\").on(\"click\", function(){\r\n"
            + "          const nid = this.getAttribute(\"node\");\r\n"
            + "          const node = cy.getElementById(nid);\r\n"
            + "          cy.zoom(7);\r\n"
            + "          cy.center(node);\r\n"
            + "          setDefaultBackgroundOfAllNodes();\r\n"
            + "          identifySinkSource();\r\n"
            + "          node.style('background-color', '#F27100');"
            + "      })";
    return script(rawHtml(code));
  }

  private static ContainerTag generatesGraphButtonScript() {
    String code =
        "var data = \"<div class='btn-div-sm'>"
            + "<button type='button' class='btn btn-outline-dark btn-sm' onClick='resetGraph()'>"
            + "<i class='fa fa-repeat' aria-hidden='true'></i></button>"
            + "<button type='button' class='btn btn-outline-dark btn-sm' onClick='increaseZoom()'>"
            + "<i class='fa fa-plus' aria-hidden='true'></i></button>"
            + "<button type='button' class='btn btn-outline-dark btn-sm' onClick='decreaseZoom()'>"
            + "<i class='fa fa-minus' aria-hidden='true'></i></button></div>"
            + "<div class='right-graph-div'>"
            + "<button type='button' class='btn btn-outline-dark btn-md' data-toggle='modal' data-target='#helpModel'>"
            + "<i class='fa fa-question' aria-hidden='true'></i></button></div>\"\r\n"
            + "$('#cy > div:first').prepend(data);";
    return script(rawHtml(code));
  }

  private static ContainerTag generatesFunctionsScript() {
    String code =
        "          function identifySinkSource(){\r\n"
            + "                 cy.getElementById('n0').style('border-width', 4);\r\n"
            + "                 cy.getElementById('"
            + lastNodeId
            + "').style('background-color', '#ba0000');\r\n"
            + "              }\r\n"
            + "\r\n"
            + "              function setDefaultBackgroundOfAllNodes(){\r\n"
            + "                 cy.nodes().style('background-color', '#007bff');\r\n"
            + "             }\r\n"
            + "\r\n"
            + "             function increaseZoom(){\r\n"
            + "                 var zoom = parseFloat(cy.zoom()) + 0.50;\r\n"
            + "                 cy.zoom(zoom)\r\n"
            + "\r\n"
            + "             }\r\n"
            + "\r\n"
            + "             function decreaseZoom(){\r\n"
            + "                var zoom = parseFloat(cy.zoom()) - 0.50;\r\n"
            + "                 cy.zoom(zoom)\r\n"
            + "             }\r\n"
            + "\r\n"
            + "             function resetGraph(){\r\n"
            + "                cy.elements().remove();\r\n"
            + "                nodesSet();\r\n"
            + "             }\r\n"
            + "\r\n"
            + "            function nodesSet(){\r\n"
            + "              cy.add(cyNodes);\r\n"
            + "              identifySinkSource();\r\n"
            + "              cy.layout(options).run();\r\n"
            + "            }";

    return script(rawHtml(code));
  }

  private static ContainerTag generateMouseOverNodeScript() {
    String code =
        " cy.on('mouseover ', 'node', function(){\r\n"
            + "                  var node = $('.node-line[node=\"'+ this.data('id')+'\"]');\r\n"
            + "                  var fileId = node.closest(\"ul\").attr('id');\r\n"
            + "                  var fileLink =  $('.dropdown-toggle[href=\"#'+fileId+'\"]');\r\n"
            + "                  if(fileLink.attr('aria-expanded') == 'false'){\r\n"
            + "                     fileLink.click();\r\n"
            + "                  }\r\n"
            + "                  node.focus();\r\n"
            + "                  var sidebar = $('#sidebar');\r\n"
            + "              });\r\n"
            + "\r\n"
            + "              cy.on('tap', function(event){\r\n"
            + "                var evtTarget = event.target;\r\n"
            + "                if( evtTarget == cy ){\r\n"
            + "                  $(':focus').blur();\r\n"
            + "                  setDefaultBackgroundOfAllNodes();\r\n"
            + "                  identifySinkSource();\r\n"
            + "                } \r\n"
            + "              });";
    return script(rawHtml(code));
  }

  private static ContainerTag generateNodeClickScript() {
    String postUrl = "http:/" + serverAddress + "/flow/show-line";
    String code =
        "cy.on('tap', 'node', function(){\r\n"
            + "setDefaultBackgroundOfAllNodes();\r\n"
            + "identifySinkSource();\r\n"
            + "this.style('background-color', '#F27100');\r\n"
            + "   var position = {\r\n"
            + "     url : this.data('url'),\r\n"
            + "     firstLine : this.data('firstLine'),\r\n"
            + "     firstCol : this.data('firstCol'),\r\n"
            + "     lastLine : this.data('lastLine'),\r\n"
            + "     lastCol : this.data('lastCol'),\r\n"
            + "     code : this.data('value')\r\n"
            + "   };\r\n"
            + "   $.ajax({\r\n"
            + "      url: '"
            + postUrl
            + "',\r\n"
            + "      type: 'post',\r\n"
            + "      dataType: 'json',\r\n"
            + "      contentType: 'application/json',\r\n"
            + "      success: function (data) {\r\n"
            + "        console.log(data);\r\n"
            + "      },\r\n"
            + "      data: JSON.stringify(position)\r\n"
            + "   });\r\n"
            + "});";
    return script(rawHtml(code));
  }

  private static void setDataFlowGraphAndSidebar(Iterable<Pair<Position, String>> flows)
      throws IOException {
    if (flows == null) {
      return;
    }
    String code = " let cyNodes = [";
    HashMap<Position, Integer> existingNodes = new HashMap<Position, Integer>();
    String from = "";
    String to = "";
    Position fromPosition = null;
    Position toPosition = null;
    int nodeCount = 0;
    int edgeCount = 0;
    Position previousPosition = null;
    String previousLine = "";
    // filename and its line numbers for making sidebar
    List<Pair<String, Vector<Pair<Integer, String>>>> sidebarInfos = new ArrayList<>();

    String currentFileName = "";
    String tempFileName = "";
    Vector<Pair<Integer, String>> lineNumbers = new Vector<>();
    Pair<Integer, String> line;
    for (Pair<Position, String> flow : flows) {
      tempFileName = getFileNameFromPosition(flow.fst);

      if (previousPosition == null) {
        previousPosition = flow.fst;
        previousLine = flow.snd;
        currentFileName = tempFileName;

        code +=
            getGraphElement("nodes", generateNodeData(nodeCount, previousLine, previousPosition));
        existingNodes.put(previousPosition, nodeCount);
        line =
            Pair.make(existingNodes.get(previousPosition), sidebarPositionName(previousPosition));
        lineNumbers.add(line);
        nodeCount++;
        continue;
      }

      fromPosition = previousPosition;
      from = previousLine;
      toPosition = flow.fst;
      to = flow.snd;

      if (!existingNodes.containsKey(fromPosition)) {
        code += getGraphElement("nodes", generateNodeData(nodeCount, from, fromPosition));

        existingNodes.put(fromPosition, nodeCount);
        nodeCount++;
      }

      if (!existingNodes.containsKey(toPosition)) {
        code += getGraphElement("nodes", generateNodeData(nodeCount, to, toPosition));

        existingNodes.put(toPosition, nodeCount);
        nodeCount++;
      }

      // if the current file name does not match with previous filename
      // then we will save the previous file name with its line numbers
      if (!tempFileName.isEmpty() && !tempFileName.equals(currentFileName)) {
        if (!lineNumbers.isEmpty()) {
          Pair<String, Vector<Pair<Integer, String>>> pair =
              Pair.make(currentFileName, lineNumbers);
          sidebarInfos.add(pair);
        }
        currentFileName = tempFileName;
        lineNumbers = new Vector<>();
      }
      line = Pair.make(existingNodes.get(toPosition), sidebarPositionName(toPosition));
      lineNumbers.add(line);

      code +=
          getGraphElement(
              "edges",
              new String[] {
                "id: 'e" + edgeCount + "'",
                "source: 'n" + existingNodes.get(fromPosition) + "'",
                "target: 'n" + existingNodes.get(toPosition) + "'",
                "label: '" + lineToVariableName(previousLine) + "'",
              });
      edgeCount++;
      previousPosition = flow.fst;
      previousLine = flow.snd;
    }

    // For the last edge entry
    if (!lineNumbers.isEmpty()) {
      Pair<String, Vector<Pair<Integer, String>>> pair = Pair.make(currentFileName, lineNumbers);
      sidebarInfos.add(pair);
    }

    code += "]; nodesSet();";
    lastNodeId = nodeCount != 0 ? "n" + (nodeCount - 1) : "n0";
    dataFlowGraph = script(rawHtml(code));
    setSidebar(sidebarInfos);
  }

  private static String lineToVariableName(String line) {
    String[] splitOnEqual = line.split("=");
    int taintVarPos = 0;
    if (splitOnEqual.length == 2
        && !line.contains("if(")
        && !line.contains("if ")
        && !line.contains("for(")
        && !line.contains("for ")
        && !line.contains("while(")
        && !line.contains("while ")) {

      String variableWithType = splitOnEqual[0].replaceAll("[-+*/](?![^(]*\\))", "");
      String[] splitOnSpace = variableWithType.split(" ");
      taintVarPos = splitOnSpace.length > 0 ? splitOnSpace.length - 1 : 0;
      previousTaintedVariable.add(splitOnSpace[taintVarPos]);
      return splitOnSpace[taintVarPos];
    } else {
      for (int i = 0; i < previousTaintedVariable.size(); i++) {
        if (line.contains(previousTaintedVariable.get(i) + " ")
            || line.contains(previousTaintedVariable.get(i) + ")")
            || line.contains(previousTaintedVariable.get(i) + ",")) {
          return previousTaintedVariable.get(i);
        }
      }
    }
    return "";
  }

  private static String sidebarPositionName(Position position) {
    String methodName = "";
    if (position instanceof FlowCodePosition) {
      FlowCodePosition codePostion = (FlowCodePosition) position;
      if (codePostion.getMehodName() != null) {
        methodName += codePostion.getMehodName() + " : ";
      }
    }
    return methodName + "Line " + position.getFirstLine();
  }

  private static String[] generateNodeData(int id, String value, Position position) {
    return new String[] {
      "id: 'n"
          + id
          + "', value: '"
          + value
          + "', url: \""
          + position.getURL()
          + "\""
          + ", firstLine: '"
          + position.getFirstLine()
          + "', firstCol: '"
          + position.getFirstCol()
          + "', lastLine: '"
          + position.getLastLine()
          + "', lastCol: '"
          + position.getLastCol()
          + "'"
    };
  }

  private static void setSidebar(
      Iterable<Pair<String, Vector<Pair<Integer, String>>>> sidebarInfos) {
    String code = "";
    String fileName = "";
    String collapseId = "";
    String locationCount = "";
    String sidebarPostionName = "";
    int node, numberOfElement, count = 1;
    for (Pair<String, Vector<Pair<Integer, String>>> sidebarInfo : sidebarInfos) {
      fileName = sidebarInfo.fst;
      numberOfElement = sidebarInfo.snd.size();
      collapseId = "side-line-" + count;
      locationCount =
          numberOfElement > 1 ? numberOfElement + " locations" : numberOfElement + " location";
      code +=
          "<li> <a href='#"
              + collapseId
              + "' data-toggle='collapse' aria-expanded='false' class='dropdown-toggle'>"
              + fileName
              + "<br/><small>("
              + locationCount
              + ")</small></a>"
              + "<ul class='collapse list-unstyled' id='"
              + collapseId
              + "'>";
      for (Pair<Integer, String> line : sidebarInfo.snd) {
        node = line.fst;
        sidebarPostionName = line.snd;
        code +=
            "<li>"
                + "<a class='node-line' href='#' node='n"
                + node
                + "'>"
                + sidebarPostionName
                + "</a>\r\n"
                + "</li>";
      }
      code += "</ul></li>";
      count++;
    }
    sidebarList = rawHtml(code);
  }

  private static String getGraphElement(String group, String[] datas) {
    String element = " { group: '" + group + "', data: { ";
    for (String data : datas) {
      element += data + ", ";
    }
    element += " }}, ";
    return element;
  }

  private static ContainerTag sideBarList() {
    String projectName = "Project Name";
    return ul(p(projectName), sidebarList).withClass("list-unstyled components");
  }

  private static ContainerTag helpModal() {
    return div(div(div(
                    div(
                            h5("Help").withClass("modal-title").withId("helpModelLabel"),
                            button(span(rawHtml("&times;")).attr("aria-hidden", "true"))
                                .withClass("close")
                                .attr("data-dismiss", "modal")
                                .attr("aria-label", "Close"))
                        .withClass("modal-header"),
                    div(ul(
                                li("Grab to move the Graph.").withClass("list-group-item"),
                                li("Scroll to zoom.").withClass("list-group-item"),
                                li("Click nodes in the graph to view the code in the IDE.")
                                    .withClass("list-group-item"),
                                li("Graph edges with variable name.").withClass("list-group-item"))
                            .withClass("list-group"))
                        .withClass("modal-body"),
                    div(button("Close")
                            .withClasses("btn", "btn-secondary")
                            .attr("data-dismiss", "modal"))
                        .withClass("modal-footer"))
                .withClass("modal-content"))
            .withClass("modal-dialog")
            .attr("role", "document"))
        .withClasses("modal", "fade")
        .withId("helpModel")
        .attr("tabindex", "-1")
        .attr("role", "dialog")
        .attr("aria-labelledby", "helpModelLabel")
        .attr("aria-hidden", "true");
  }

  private static DomContent generateBody(AnalysisResult result) throws IOException {
    String problemDescription = result.severity() + ": " + result.toString(true);
    setDataFlowGraphAndSidebar(result.related());
    return body(
        div(
                nav(
                        div(sidebarHeader()).withClass("sidebar-header"), sideBarList() // sidebar
                        )
                    .withId("sidebar"),
                div(
                        nav(div(generateSidebarCollapseButton()).withClass("container-fluid"))
                            .withClasses("navbar", "navbar-expand-lg", "navbar-light", "bg-light"),
                        generetaH1Title(),
                        div(
                                generateGlobalScript(),
                                div(h5(problemDescription))
                                    .withClasses("col-md-12", "alert", "div-danger"),
                                div(div().withId("cy")).withClass("col-md-12"),
                                helpModal())
                            .withClass("row"),
                        generateDataFlowScriptConfiguration(),
                        generatesFunctionsScript(),
                        dataFlowGraph,
                        generateNodeClickScript(),
                        generateMouseOverNodeScript(),
                        generateSidebarLinkClickScript(),
                        generateSidebarCollapseScript(),
                        generatesGraphButtonScript())
                    .withId("content"))
            .withClass("wrapper"));
  }

  private static DomContent generateHeader() {
    return head(
        title("MagpieBridge Data Flow Page"),
        new UnescapedText(
            "<!-- Latest compiled and minified CSS -->\n"
                + "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css\" integrity=\"sha384-9gVQ4dYFwwWSjIDZnLEWnxCjeSWFphJiwGPXr1jddIhOegiu1FwO5qRGvFXOdJZ4\" crossorigin=\"anonymous\">\n"
                + "<link href=\"https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css\" rel=\"stylesheet\">"
                + "<!-- Latest compiled and minified JavaScript -->\n"
                + "<script src=\"https://code.jquery.com/jquery-3.6.0.min.js\"></script>"
                + "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js\"></script>"
                + "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.2.5/cytoscape.js\"></script>\r\n"
                + "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/dagre/0.8.5/dagre.min.js\"></script>\r\n"
                + "<script src=\"https://cdn.jsdelivr.net/npm/cytoscape-dagre@2.2.2/cytoscape-dagre.min.js\"></script>"
                + generateCustomStyleSheet()));
  }

  private static ContainerTag generateSidebarCollapseButton() {
    return button(rawHtml("<i class=\"fas fa-align-left\"></i>"))
        .withClasses("btn", "btn-info")
        .withId("sidebarCollapse");
  }

  private static String generateCustomStyleSheet() {
    String custom =
        "<style>"
            + "@import \"https://fonts.googleapis.com/css?family=Poppins:300,400,500,600,700\";\r\n"
            + "body {\r\n"
            + "    font-family: 'Poppins', sans-serif;\r\n"
            + "    background: #fafafa;\r\n"
            + "}\r\n"
            + "\r\n"
            + "p {\r\n"
            + "    font-family: 'Poppins', sans-serif;\r\n"
            + "    font-size: 1.1em;\r\n"
            + "    font-weight: 300;\r\n"
            + "    line-height: 1.7em;\r\n"
            + "    color: #999;\r\n"
            + "}\r\n"
            + "\r\n"
            + "a,\r\n"
            + "a:hover,\r\n"
            + "a:focus {\r\n"
            + "    color: inherit;\r\n"
            + "    text-decoration: none;\r\n"
            + "    transition: all 0.3s;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".node-line:focus{\r\n"
            + "     background-color: #F27100;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".navbar {\r\n"
            + "    padding: 15px 10px;\r\n"
            + "    background: #fff;\r\n"
            + "    border: none;\r\n"
            + "    border-radius: 0;\r\n"
            + "    margin-bottom: 40px;\r\n"
            + "    box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);\r\n"
            + "    display: none;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".navbar-btn {\r\n"
            + "    box-shadow: none;\r\n"
            + "    outline: none !important;\r\n"
            + "    border: none;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".line {\r\n"
            + "    width: 100%;\r\n"
            + "    height: 1px;\r\n"
            + "    border-bottom: 1px dashed #ddd;\r\n"
            + "    margin: 40px 0;\r\n"
            + "}\r\n"
            + "\r\n"
            + "/* ---------------------------------------------------\r\n"
            + "    SIDEBAR STYLE\r\n"
            + "----------------------------------------------------- */\r\n"
            + "#cy{\r\n"
            + "    min-width: 380px; \r\n"
            + "    min-height: 700px; \r\n"
            + "    display: block; \r\n"
            + "    border:1px solid;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".wrapper {\r\n"
            + "    display: flex;\r\n"
            + "    width: 100%;\r\n"
            + "    align-items: stretch;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar {\r\n"
            + "    min-width: 250px;\r\n"
            + "    max-width: 250px;\r\n"
            + "    background: #0F80C1;\r\n"
            + "    color: #fff;\r\n"
            + "    transition: all 0.3s;\r\n"
            + "    position: fixed;\r\n"
            + "    overflow-y: auto;\r\n"
            + "    top: 0;\r\n"
            + "    bottom: 0;\r\n"
            + "}\r\n"
            + "#sidebarCollapse {\r\n"
            + "    display: none;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar.active {\r\n"
            + "    margin-left: 0px;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#content.active {\r\n"
            + "    margin-left: 250px;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar .sidebar-header {\r\n"
            + "    padding: 20px;\r\n"
            + "    background: #0F80C1;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar ul.components {\r\n"
            + "    padding: 20px 0;\r\n"
            + "    border-bottom: 1px solid #47748b;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar ul p {\r\n"
            + "    color: #fff;\r\n"
            + "    padding: 10px;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar ul li a {\r\n"
            + "    padding: 10px;\r\n"
            + "    font-size: 0.9em;\r\n"
            + "    display: block;\r\n"
            + "    word-wrap:break-word;\r\n"
            + "    hyphens: auto;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar ul li a:hover {\r\n"
            + "    color: #fff;\r\n"
            + "    background: #7BBEEB;\r\n"
            + "}\r\n"
            + "\r\n"
            + "#sidebar ul li.active>a,\r\n"
            + "a[aria-expanded=\"true\"] {\r\n"
            + "    color: #fff;\r\n"
            + "    background: #0F80C1;\r\n"
            + "}\r\n"
            + "\r\n"
            + "a[data-toggle=\"collapse\"] {\r\n"
            + "    position: relative;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".dropdown-toggle::after {\r\n"
            + "    display: block;\r\n"
            + "    position: absolute;\r\n"
            + "    top: 50%;\r\n"
            + "    right: 20px;\r\n"
            + "    transform: translateY(-50%);\r\n"
            + "}\r\n"
            + ".div-danger {\r\n"
            + "    color: white;\r\n"
            + "    background-color: #ba0000;\r\n"
            + "    border-color: #ba0000;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".btn-div-sm{\r\n"
            + "    width:10%;\r\n"
            + "    font-size:11px;\r\n"
            + "    z-index:5;\r\n"
            + "    position: absolute;\r\n"
            + "}\r\n"
            + "\r\n"
            + ".right-graph-div{\r\n"
            + "    top: 0;\r\n"
            + "    right: 0;\r\n"
            + "    z-index:5;\r\n"
            + "    position: absolute;\r\n"
            + "}"
            + "ul ul a {\r\n"
            + "    font-size: 0.9em !important;\r\n"
            + "    padding-left: 30px !important;\r\n"
            + "    background: #0F80C1;\r\n"
            + "}\r\n"
            + "\r\n"
            + "ul.CTAs {\r\n"
            + "    padding: 20px;\r\n"
            + "}\r\n"
            + "\r\n"
            + "ul.CTAs a {\r\n"
            + "    text-align: center;\r\n"
            + "    font-size: 0.9em !important;\r\n"
            + "    display: block;\r\n"
            + "    border-radius: 5px;\r\n"
            + "    margin-bottom: 5px;\r\n"
            + "}\r\n"
            + "\r\n"
            + "a.download {\r\n"
            + "    background: #fff;\r\n"
            + "    color: #7386D5;\r\n"
            + "}\r\n"
            + "\r\n"
            + "a.article,\r\n"
            + "a.article:hover {\r\n"
            + "    background: #0F80C1 !important;\r\n"
            + "    color: #fff !important;\r\n"
            + "}\r\n"
            + "\r\n"
            + "/* ---------------------------------------------------\r\n"
            + "    CONTENT STYLE\r\n"
            + "----------------------------------------------------- */\r\n"
            + "\r\n"
            + "#content {\r\n"
            + "    width: 100%;\r\n"
            + "    padding: 20px;\r\n"
            + "    min-height: 100vh;\r\n"
            + "    transition: all 0.3s;\r\n"
            + "    margin-left: 250px;\r\n"
            + "}\r\n"
            + "\r\n"
            + "/* ---------------------------------------------------\r\n"
            + "    MEDIAQUERIES\r\n"
            + "----------------------------------------------------- */\r\n"
            + "\r\n"
            + "@media (max-width: 768px) {\r\n"
            + "    #sidebar {\r\n"
            + "        margin-left: -250px;\r\n"
            + "    }\r\n"
            + "    #sidebar.active {\r\n"
            + "        margin-left: 0;\r\n"
            + "    }\r\n"
            + "    #sidebarCollapse {\r\n"
            + "        display: block;\r\n"
            + "    }\r\n"
            + "    #sidebarCollapse span {\r\n"
            + "        display: none;\r\n"
            + "    }\r\n"
            + "    #content {\r\n"
            + "        margin-left: 0px;\r\n"
            + "    }\r\n"
            + "    #content.active {\r\n"
            + "        margin-left: 250px;\r\n"
            + "    }\r\n"
            + "    .navbar {\r\n"
            + "        display: block;\r\n"
            + "    }\r\n"
            + "}"
            + "</style>";
    return custom;
  }

  private static String getFileNameFromPosition(Position position) throws MalformedURLException {
    File file = SourceCodeReader.getFileWithPosition(position);
    return file.exists() && file.isFile() ? file.getName() : "";
  }
}
