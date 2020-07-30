package magpiebridge.core.analysis.configuration.htmlElement;

import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;

/**
 * This class represents the Checkbox with the below additional features.
 *
 * <p>1. isChildSelectable - if true, then by selecting the parent checkbox, all its children also
 * will be selected.
 *
 * @author Ranjith Krishnamurthy
 */
public class CheckBox extends ConfigurationOption {
  private boolean isChildSelectable;

  /**
   * This is the getter for isChildSelectable.
   *
   * @return Boolean - isChildSelectable
   */
  public boolean isChildSelectable() {
    return isChildSelectable;
  }

  /**
   * This is the setter for isChildSelectable
   *
   * @param childSelectable boolean - isChildSelectable
   */
  public void setChildSelectable(boolean childSelectable) {
    isChildSelectable = childSelectable;
  }

  /**
   * This constructs the Checkbox with default isChildSelectable to true.
   *
   * @param name Name of the Checkbox
   */
  public CheckBox(String name) {
    super(name, OptionType.checkbox);
    this.isChildSelectable = true;
  }

  /**
   * This contructs the checkbox with the isChildSelectable provided.
   *
   * @param name Name of the Checkbox
   * @param value Value to set the checkbox on or off. To set the checkbox on value should be "true"
   *     or "on".
   */
  public CheckBox(String name, String value) {
    super(name, OptionType.checkbox, value);
    this.isChildSelectable = true;
  }
}
