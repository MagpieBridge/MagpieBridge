package magpiebridge.core.analysis.configuration.htmlElement;

import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;

/**
 * This class represents the Checkbox with additional features.
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
     * @param name              Name of the Checkbox
     * @param isChildSelectable boolean - isChildSelectable
     */
    public CheckBox(String name, boolean isChildSelectable) {
        super(name, OptionType.checkbox);
        this.isChildSelectable = isChildSelectable;
    }
}
