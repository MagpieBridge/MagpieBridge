package magpiebridge.core.analysis.configuration;

/** @author Linghui Luo */
public class ConfigurationAction {

  private String name;
  private Runnable action;

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

  public OptionType getType() {
    return OptionType.action;
  }
}
