package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import java.util.List;
import javax.annotation.Nullable;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * The Interface AnalysisResult.
 *
 * @author Julian Dolby and Linghui Luo
 */
public interface AnalysisResult {

  /**
   * Kind.
   *
   * @return the kind
   */
  public Kind kind();

  /**
   * The message of this result.
   *
   * @param useMarkdown the use markdown
   * @return the string
   */
  public String toString(boolean useMarkdown);

  /**
   * The source code position this result refers to.
   *
   * @return the position
   */
  public Position position();

  /**
   * The related information (source code position and corresponding message) of this result.
   *
   * @return the related information
   */
  public Iterable<Pair<Position, String>> related();

  /**
   * Severity of this result, usually used for diagnostics.
   *
   * @return the diagnostic severity
   */
  public DiagnosticSeverity severity();

  /**
   * The suggested repair, if any.
   *
   * @return a pair of source code position and the new code.
   */
  @Nullable
  public Either<Pair<Position, String>, List<TextEdit>> repair();

  /**
   * The code which this result refers to.
   *
   * @return the code
   */
  public String code();

  /**
   * A Command to be used in a CodeLens
   *
   * @return the command, or null if no special command
   */
  public default Iterable<Command> command() {
    assert kind() != Kind.CodeLens || repair() != null;
    return null;
  }
}
