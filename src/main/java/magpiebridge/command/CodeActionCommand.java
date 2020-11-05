package magpiebridge.command;

/**
 * The default types of code action commands related to a diagnostic MagpieBridge offers. <br>
 * 1. {@link CodeActionCommand#fix} stands for quick fix actions. See {@link FixCommand}. <br>
 * 2. {@link CodeActionCommand#reportFP} stands for reporting false positives, i.e., this allows
 * users to report false positives. See {@link ReportFalsePositiveCommand}.<br>
 * 3. {@link CodeActionCommand#reportConfusion} stands for reporting confusions, i.e., this allows
 * users to report warnings they feel confused. See {@link ReportConfusionCommand}.<br>
 * 4. {@link CodeActionCommand#openURL} denotes that the system should open a URL, e.g. for
 * displaying information to the user. See {@link OpenURLCommand}.<br>
 * 5. {@link CodeActionCommand#suppressWarning} stands for suppressing a warning. See {@link
 * SuppressWarningCommand}.<br>
 *
 * @author Linghui Luo
 */
public enum CodeActionCommand {
  fix,
  reportFP,
  reportConfusion,
  openURL,
  suppressWarning
}
