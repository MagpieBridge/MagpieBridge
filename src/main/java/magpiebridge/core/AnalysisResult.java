package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import org.eclipse.lsp4j.DiagnosticSeverity;

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
   * The suggested repair.
   *
   * @return a pair of source code position and the new code.
   */
  public Pair<Position, String> repair();

  /**
   * The code which this result refers to.
   *
   * @return the code
   */
  public String code();
}
