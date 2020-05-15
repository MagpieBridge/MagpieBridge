package magpiebridge.core.analysis.configuration;

import static j2html.TagCreator.*;
import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.text;

import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;
import j2html.tags.Tag;
import j2html.tags.UnescapedText;
import java.util.ArrayList;
import java.util.List;

public class HtmlGenerator {
  /** @author Linghui Luo */
  public static String generateHTML(
      List<ConfigurationOption> configration, List<ConfigurationAction> actions) {
    return html(generateHeader(), generateBody(configration, actions)).renderFormatted();
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
                                div(h2("Configuration"), generateForm(configration))
                                    .withClass("col-md-6"),
                                div(h2("Actions"), generateActions(actions).withClass("col-md-6")))
                            .withClass("row"))
                    .withClass("center-block"))
                .withClass("row"))
            .withClass("container"));
  }

  private static ContainerTag generateActions(List<ConfigurationAction> actions) {
    ContainerTag ret = div();
    for (ConfigurationAction action : actions) {
      ret.with(generateButton(action.getName()));
    }
    return ret;
  }

  private static ContainerTag generateForm(List<ConfigurationOption> configration) {
    ContainerTag ret = form().withMethod("post").withAction("/config");
    List<Tag> tags = new ArrayList<Tag>();
    for (ConfigurationOption o : configration) {
      tags.add(generateTag(o, 0));
    }
    ret.with(tags);
    ret.with(generateSubmit());
    return ret;
  }

  private static ContainerTag generateTag(ConfigurationOption o, int i) {
    i++;
    ContainerTag ret = div();
    String name = o.getName();
    if (o.getType().equals(OptionType.checkbox)) {
      ret.with(generateCheckbox(o), generateLabel(name));
    } else if (o.getType().equals(OptionType.text)) {
      ret.with(generateLabel(name), generateTextfield(o));
    }
    ret.with(br());
    if (o.hasChildren()) {
      List<Tag> tags = new ArrayList<Tag>();
      for (ConfigurationOption child : o.getChildren()) {
        Tag tag = generateTag(child, i);
        tag.withStyle("margin-left: " + (i * 50) + "px");
        tags.add(tag);
      }
      ret.with(tags);
    }
    return ret;
  }

  private static Tag generateButton(String name) {
    return a().withClasses("btn", "btn-default")
        .withRole("button")
        .withName(name)
        .withId(name)
        .withHref("?action=" + name)
        .with(text(name));
  }

  private static Tag generateCheckbox(ConfigurationOption o) {
    EmptyTag checkBox = input().withType("checkbox").withName(o.getName()).withId(o.getName());
    if (o.getValueAsBoolean()) {
      checkBox.attr("checked");
    }
    return checkBox;
  }

  private static Tag generateSubmit() {
    return input()
        .withClasses("btn", "btn-default")
        .withType("submit")
        .withValue("Submit Configuration");
  }

  private static Tag generateTextfield(ConfigurationOption o) {
    return input()
        .withType("text")
        .withName(o.getName())
        .withId(o.getName())
        .withValue(o.getValue());
  }

  private static Tag generateLabel(String name) {
    return label(name);
  }
}
