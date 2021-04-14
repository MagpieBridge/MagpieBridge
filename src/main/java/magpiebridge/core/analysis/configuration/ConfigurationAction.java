package magpiebridge.core.analysis.configuration;

import magpiebridge.core.Analysis;

/**
 * This class defines an action allowed by a {@link Analysis} running at the server.
 *
 * @author Linghui Luo
 */
public class ConfigurationAction {

  private String id;
  private String name;
  private Runnable action;
  private String source;

  public ConfigurationAction(String name, Runnable action) {
    this(name, action, name);
  }

  public ConfigurationAction(String name, Runnable action, String id) {
    this.name = name;
    this.action = action;
    this.id = id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Runnable getAction() {
    return action;
  }

  public String getSource() {
    return source;
  }

  public ConfigurationAction setSource(String source) {
    this.source = source;
    return this;
  }

  @Override
  public String toString() {
    return "ConfigurationAction [id="
        + id
        + ", name="
        + name
        + ", action="
        + action
        + ", source="
        + source
        + "]";
  }
}
