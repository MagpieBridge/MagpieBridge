package magpiebridge.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
    TextEdit tEdit = new TextEdit(range, replaceText);
    Map<String, List<TextEdit>> changes = new HashMap<>();
    changes.put(uri, Collections.singletonList(tEdit));
    WorkspaceEdit edit = new WorkspaceEdit(changes);
    codeAction.setKind(CodeActionKind.QuickFix);
    codeAction.setEdit(edit);
    List<Object> args = new LinkedList<Object>(diags);
    args.add(0, uri);
    codeAction.setCommand(new Command(title, CodeActionKind.QuickFix, args));
    return codeAction;
  }
}
