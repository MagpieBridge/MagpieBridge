package magpiebridge.core;

/**
 * The default types of code action commands MagpieBridge offers. <br>
 * {@link CodeActionCommand#fix} stands for quick fix actions. <br>
 * {@link CodeActionCommand#reportFP} stands for reporting false positives, i.e., this allows users
 * to report false positives. <br>
 * {@link CodeActionCommand#reportConfusion} stands for reporting confusions, i.e., this allows
 * users to report warnings they feel confused.
 *
 * @author Linghui Luo
 */
public enum CodeActionCommand {
  fix,
  reportFP,
  reportConfusion
}
