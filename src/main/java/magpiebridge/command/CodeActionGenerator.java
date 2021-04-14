/*
 * @author Linghui Luo
 */
package magpiebridge.command;

import java.util.ArrayList;
import java.util.List;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.MagpieWorkspaceService;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/**
 * The Class CodeActionGenerator generates different types of code actions. See {@link
 * CodeActionCommand}.
 *
 * @author Linghui Luo
 */
public class CodeActionGenerator {

  /**
   * Generate a CodeAction which is a {@link CodeActionCommand#fixFromMB} command and it replaces
   * code in the {@code range} with {@code replaceText}. This CodeAction will be executed when a
   * {@code workspace/executeCommand} request is sent from the client to the {@link MagpieServer}.
   * See {@link MagpieWorkspaceService#executeCommand(org.eclipse.lsp4j.ExecuteCommandParams)}.
   *
   * @param title the title
   * @param range the range
   * @param replaceText the replace text
   * @param uri the URI which the replacement should happen
   * @param diag the diagnostic related to this CodeAction
   * @return the generated code action
   */
  public static CodeAction replace(
      String title, Range range, String replaceText, String uri, Diagnostic diag) {
    CodeAction codeAction = new CodeAction(title);
    codeAction.setKind(CodeActionKind.QuickFix);
    List<Object> args = new ArrayList<>();
    args.add(uri);
    args.add(range);
    args.add(replaceText);
    args.add(diag);
    codeAction.setCommand(new Command(title, CodeActionCommand.fixFromMB.name(), args));
    return codeAction;
  }

  /**
   * Generate a CodeAction which is either {@link CodeActionCommand#reportFPFromMB} or {@link
   * CodeActionCommand#reportConfusionFromMB}. This CodeAction will be executed when a {@code
   * workspace/executeCommand} request is sent from the client to the {@link MagpieServer}. See
   * {@link MagpieWorkspaceService#executeCommand(org.eclipse.lsp4j.ExecuteCommandParams)}.
   *
   * @param title the title
   * @param uri the URI which the code action should happen
   * @param diag the diagnostic related to this CodeAction
   * @param cmd the command type
   * @return the generated code action
   */
  public static CodeAction generateCommandAction(
      String title, String uri, Diagnostic diag, String cmd) {
    CodeAction codeAction = new CodeAction(title);
    codeAction.setKind(CodeActionKind.Source);
    List<Object> args = new ArrayList<>();
    args.add(uri);
    args.add(diag);
    codeAction.setCommand(new Command(title, cmd, args));
    return codeAction;
  }
}
