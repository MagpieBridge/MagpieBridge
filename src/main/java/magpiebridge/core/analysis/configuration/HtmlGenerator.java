package magpiebridge.core.analysis.configuration;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;
import static j2html.TagCreator.text;
import static j2html.TagCreator.title;

import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;
import j2html.tags.UnescapedText;
import java.util.ArrayList;
import java.util.List;
import magpiebridge.core.Analysis;
import magpiebridge.core.analysis.configuration.htmlElement.CheckBox;

/**
 * The class generates a HTML page based on the {@link ConfigurationOption}s and {@link
 * ConfigurationAction}s defined in {@link Analysis#getConfigurationOptions()} and {@link
 * Analysis#getConfiguredActions()} by an {@link Analysis} running on the server.
 *
 * @author Linghui Luo
 */
public class HtmlGenerator {

  private static String sourceOption;
  private static String sourceAction;
  private static String serverAddress;

  public static String generateHTML(
      List<ConfigurationOption> configuration,
      List<ConfigurationAction> actions,
      String serverAddress) {
    sourceOption = null;
    sourceAction = null;
    HtmlGenerator.serverAddress = serverAddress;
    return html(generateHeader(), generateBody(configuration, actions)).renderFormatted();
  }

  private static ContainerTag generateHeader() {
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
                + "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>"));
  }

  private static ContainerTag generetaH1Title() {
    return h1("Welcome to MagpieBridge Configuration and Controller Page!");
  }

  private static ContainerTag generateBody(
      List<ConfigurationOption> configration, List<ConfigurationAction> actions) {
    return body(
        div(div(div(
                        generetaH1Title(),
                        div(
                                generateGlobalScript(),
                                div(
                                        h2("Configuration"),
                                        generateForm(configration),
                                        generateScript())
                                    .withClass("col-md-6"),
                                div(h2("Actions"), generateActions(actions).withClass("col-md-6")))
                            .withClass("row"))
                    .withClass("center-block"))
                .withClass("row"))
            .withClass("container"));
  }

  // FIXME: FIX THE FORMDATA
  private static ContainerTag generateScript() {
    String url = "http:/" + serverAddress + "/config";
    String scriptCode =
        "function checkboxSelection(className, parentID) {"
            + "var clist = document.getElementsByClassName(className);"
            + "var parentStatus = document.getElementById(parentID).checked;"
            + "for(var i = 0; i < clist.length; ++i) {"
            + "  clist[i].checked = parentStatus;"
            + "}}"
            + "function getFormData(){"
            + "  var confForm = document.getElementById('configForm');"
            + "  var formData = new FormData(confForm);"
            + "  var data = [...formData.entries()];\n"
            + "  var asString = data.map(x => `${encodeURIComponent(x[0])}=${encodeURIComponent(x[1])}`).join('&');"
            + "  return asString;"
            + "}"
            + "var submitConfiguration = {};"
            + "if (typeof acquireVsCodeApi == 'undefined'){"
            + "    submitConfiguration = function(){"
            + "      var formData = getFormData();"
            + "      var httpRequest = new XMLHttpRequest();"
            + "      httpRequest.open('POST','"
            + url
            + "');"
            + "      httpRequest.send(formData);"
            + "    }"
            + "  }else{"
            + "     submitConfiguration = function(){"
            + "       var formData = getFormData();"
            + "       var message = '"
            + url
            + "';"
            + "       message += '?'+formData;"
            + "       window.vscode.postMessage({command: 'configuration',text: message });"
            + "     }"
            + "  }";
    return script(rawHtml(scriptCode));
  }

  private static ContainerTag generateScriptForButtonClick(String functionName, String uri) {
    String url = "http:/" + serverAddress + "/config" + uri.replace(" ", "%20");
    String scriptCode =
        "var "
            + functionName
            + " = {};"
            + "if (typeof acquireVsCodeApi == 'undefined'){"
            + functionName
            + " = function(){"
            + "var httpRequest = new XMLHttpRequest();"
            + "var url = '"
            + url
            + "';"
            + "httpRequest.open('GET',url);"
            + "httpRequest.send();"
            + "}}else{"
            + functionName
            + " = function(){\n"
            + "window.vscode.postMessage({command: 'action',text: '"
            + url
            + "'});"
            + "}}";
    return script(rawHtml(scriptCode));
  }

  private static String cleanClassName(String className) {
    return className.replaceAll("[^A-Za-z0-9]", "");
  }

  private static ContainerTag generateActions(List<ConfigurationAction> actions) {
    ContainerTag ret = div();
    for (ConfigurationAction action : actions) {
      if (!action.getSource().equals(sourceAction)) {
        ret.with(h3(action.getSource()));
        sourceAction = action.getSource();
      }
      String source = action.getSource();
      String functionName = action.getName().concat(source.split(":")[0]).replace(" ", "");
      String uri = "?action=" + action.getName() + "&" + "source=" + source;
      ret.with(generateButton(action.getName(), functionName, uri), br());

      ret.with(generateScriptForButtonClick(functionName, uri));
    }
    return ret;
  }

  private static ContainerTag generateGlobalScript() {
    String code =
        "if (typeof acquireVsCodeApi != 'undefined'){window.vscode = acquireVsCodeApi();}";
    return script(rawHtml(code));
  }

  private static ContainerTag generateForm(List<ConfigurationOption> configration) {
    ContainerTag ret = form().withMethod("post").withAction("/config").withId("configForm");
    List<ContainerTag> tags = new ArrayList<ContainerTag>();
    for (ConfigurationOption o : configration) {
      tags.add(generateOption(o, 0, cleanClassName(o.getName())));
    }
    ret.with(tags);
    ret.with(generateSubmit());
    return ret;
  }

  private static ContainerTag generateOption(ConfigurationOption o, int i, String className) {
    i++;
    ContainerTag ret = div();
    if (!o.getSource().equals(sourceOption)) {
      ret.with(h3(o.getSource()));
      sourceOption = o.getSource();
    }
    String name = o.getName();
    if (o.getType().equals(OptionType.container)) {
      ret.with(generateLabel(name));
    } else if (o.getType().equals(OptionType.checkbox)) {
      ret.with(generateCheckbox(o, className), generateLabel(name));
    } else if (o.getType().equals(OptionType.text)) {
      ret.with(generateLabel(name), generateTextfield(o));
    } else if (o.getType().equals(OptionType.alert)) {
      ret = script(rawHtml("alert(\"" + o.getName() + "\");"));
      return ret;
    }
    ret.with(br());
    if (o.hasChildren()) {
      List<ContainerTag> tags = new ArrayList<ContainerTag>();
      for (ConfigurationOption child : o.getChildren()) {
        ContainerTag tag = generateOption(child, i, cleanClassName(o.getName() + "child"));
        tag.withStyle("margin-left: " + (i * 50) + "px");
        tags.add(tag);
      }
      ret.with(tags);
    }
    return ret;
  }

  private static ContainerTag generateButton(String name, String functionName, String uri) {
    return a().withClasses("btn", "btn-primary")
        .withRole("button")
        .withName(name)
        .withId(functionName)
        .withHref(uri)
        .with(text(name))
        .attr("onclick", functionName.replace(" ", "") + "()")
        .withStyle("margin: 5px");
  }

  private static EmptyTag generateCheckbox(ConfigurationOption o, String className) {
    EmptyTag checkBox =
        input().withType("checkbox").withName(o.getName()).withId(o.getName()).withClass(className);

    if (o instanceof CheckBox) {
      CheckBox tempCheckBox = (CheckBox) o;

      if (tempCheckBox.isChildSelectable()) {
        String childClassName = cleanClassName(o.getName() + "child");
        checkBox.attr(
            "onclick",
            "checkboxSelection('" + cleanClassName(childClassName) + "', '" + o.getName() + "')");
      }
    }

    if (o.getValueAsBoolean()) {
      checkBox.attr("checked");
    }
    return checkBox;
  }

  private static EmptyTag generateSubmit() {
    return input()
        .withClasses("btn", "btn-primary")
        .withType("submit")
        .withValue("Submit Configuration")
        .attr("onclick", "submitConfiguration()");
  }

  private static EmptyTag generateTextfield(ConfigurationOption o) {
    return input()
        .withType("text")
        .withName(o.getName())
        .withId(o.getName())
        .withValue(o.getValue());
  }

  private static ContainerTag generateLabel(String name) {
    return label(name);
  }
}
