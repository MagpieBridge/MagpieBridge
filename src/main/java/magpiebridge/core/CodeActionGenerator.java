package magpiebridge.core;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/** @author Linghui Luo */
public class CodeActionGenerator {

  public static CodeAction replace(
      String title, Range range, String replaceText, String uri, Diagnostic diag) {
    CodeAction codeAction = new CodeAction(title);
    codeAction.setKind(CodeActionKind.QuickFix);
    List<Object> args = new ArrayList<>();
    args.add(uri);
    args.add(range);
    args.add(replaceText);
    args.add(diag);
    codeAction.setCommand(new Command(title, CodeActionCommand.fix.name(), args));
    return codeAction;
  }

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
