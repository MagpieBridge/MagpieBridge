package magpiebridge.core.analysis.configuration;

/**
 * Type of {@link ConfigurationOption}.
 *
 * @author Linghui Luo
 */
public enum OptionType {
  checkbox, // a configuration option displays as a check box
  text, // a configuration needs a text input
  alert, // a alert box with a message
  container // a container of configuration options
}
