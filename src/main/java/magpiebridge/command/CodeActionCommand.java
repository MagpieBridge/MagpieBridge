/*
 * @author Linghui Luo
 */
package magpiebridge.command;

/**
 * The default types of code action commands related to a diagnostic MagpieBridge offers. <br>
 * {@link CodeActionCommand#fix} stands for quick fix actions. See {@link FixCommand} <br>
 * {@link CodeActionCommand#reportFP} stands for reporting false positives, i.e., this allows users
 * to report false positives. See {@link ReportFalsePositiveCommand}<br>
 * {@link CodeActionCommand#reportConfusion} stands for reporting confusions, i.e., this allows
 * users to report warnings they feel confused. See {@link ReportConfusionCommand} {@link
 * CodeActionCommand#openURL} denotes that the system should open a URL, e.g. for displaying
 * information to the user.
 *
 * @author Linghui Luo
 */
public enum CodeActionCommand {
  fix,
  reportFP,
  reportConfusion,
  openURL
}
