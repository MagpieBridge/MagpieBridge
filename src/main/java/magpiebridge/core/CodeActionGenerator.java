package magpiebridge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

/** @author Linghui Luo */
public class CodeActionGenerator {

  public static CodeAction replace(
      String title, Range range, String replaceText, String uri, List<Diagnostic> diags) {
    CodeAction codeAction = new CodeAction(title);
    // CodeAction codeAction = new CodeAction(title);
    TextEdit tEdit = new TextEdit(range, replaceText);
    Map<String, List<TextEdit>> changes = new HashMap<>();
    changes.put(uri, Collections.singletonList(tEdit));
    WorkspaceEdit edit = new WorkspaceEdit(changes);
    codeAction.setKind(CodeActionKind.QuickFix);
    codeAction.setEdit(edit);
    codeAction.setCommand(
        new Command(title + "(Command)", CodeActionKind.QuickFix, new ArrayList<>()));
    // codeAction.setCommand(new Command(title, CodeActionKind.QuickFix, new ArrayList<>()));
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
