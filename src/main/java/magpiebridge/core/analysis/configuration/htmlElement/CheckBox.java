package magpiebridge.core.analysis.configuration.htmlElement;

import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;

public class CheckBox extends ConfigurationOption {
  private boolean isChildSelectable;

  public boolean isChildSelectable() {
    return isChildSelectable;
  }

  public void setChildSelectable(boolean childSelectable) {
    isChildSelectable = childSelectable;
  }

  public CheckBox(String name) {
    super(name, OptionType.checkbox);
    this.isChildSelectable = true;
  }

  public CheckBox(String name, boolean isChildSelectable) {
    super(name, OptionType.checkbox);
    this.isChildSelectable = isChildSelectable;
  }
}
