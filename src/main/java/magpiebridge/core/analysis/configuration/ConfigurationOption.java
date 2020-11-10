package magpiebridge.core.analysis.configuration;

import java.util.ArrayList;
import java.util.List;
import magpiebridge.core.Analysis;

/**
 * This class defines a configuration option for a {@link Analysis} running at the server.
 *
 * @author Linghui Luo
 */
public class ConfigurationOption {
  private String id;
  private final String name;
  private final OptionType type;
  private List<ConfigurationOption> children;
  private String value;
  private String source;
  private Object extra;

  public ConfigurationOption(String name, OptionType type) {
    this.name = name;
    this.type = type;
    this.id = name;
    this.children = new ArrayList<>();
  }

  public ConfigurationOption(String name, OptionType type, String defaultValue) {
    this(name, type);
    this.value = defaultValue;
  }

  public ConfigurationOption(String name, OptionType type, String id, String defaultValue) {
    this(name, type, defaultValue);
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public OptionType getType() {
    return type;
  }

  public ConfigurationOption addChild(ConfigurationOption... options) {
    if (children == null) children = new ArrayList<ConfigurationOption>();
    for (ConfigurationOption o : options) this.children.add(o);
    return this;
  }

  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  public List<ConfigurationOption> getChildren() {
    return children;
  }

  public String getId() {
    return id;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setExtra(Object extra) {
    this.extra = extra;
  }

  public Object getExtra() {
    return this.extra;
  }

  public String getValue() {
    return value;
  }

  public boolean getValueAsBoolean() {
    return "on".equals(value) || "true".equals(value);
  }

  public String getSource() {
    return source;
  }

  public ConfigurationOption setSource(String source) {
    this.source = source;
    if (children != null) for (ConfigurationOption o : children) o = o.setSource(source);
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((children == null) ? 0 : children.hashCode());
    result = prime * result + ((extra == null) ? 0 : extra.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConfigurationOption other = (ConfigurationOption) obj;
    if (children == null) {
      if (other.children != null) return false;
    } else if (!children.equals(other.children)) return false;
    if (extra == null) {
      if (other.extra != null) return false;
    } else if (!extra.equals(other.extra)) return false;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (source == null) {
      if (other.source != null) return false;
    } else if (!source.equals(other.source)) return false;
    if (type != other.type) return false;
    if (value == null) {
      if (other.value != null) return false;
    } else if (!value.equals(other.value)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "ConfigurationOption [id="
        + id
        + ", name="
        + name
        + ", type="
        + type
        + ", children="
        + children
        + ", value="
        + value
        + ", source="
        + source
        + ", extra="
        + extra
        + "]";
  }

  public void setChildren(List<ConfigurationOption> children) {
    this.children = children;
  }
}
