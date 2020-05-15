package magpiebridge.core.analysis.configuration;

import magpiebridge.core.Analysis;

/**
 * This class defines an action allowed by a {@link Analysis} running at the server.
 *
 * @author Linghui Luo
 */
public class ConfigurationAction {

  private String name;
  private Runnable action;
  private String source;

  public ConfigurationAction(String name, Runnable action) {
    this.name = name;
    this.action = action;
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
}
