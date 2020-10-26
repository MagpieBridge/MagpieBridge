package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * This class represents the analysis results of infer.
 *
 * @author Linghui Luo
 */
public class DefaultAnalysisResult implements AnalysisResult {

  private final Kind kind;
  private final Position position;
  private final String message;
  private final Iterable<Pair<Position, String>> related;
  private final DiagnosticSeverity severity;
  private final Pair<Position, String> repair;
  private final String code;

  public DefaultAnalysisResult(
      Kind kind,
      Position pos,
      String msg,
      Iterable<Pair<Position, String>> relatedInfo,
      DiagnosticSeverity severity,
      Pair<Position, String> repair,
      String code) {
    this.kind = kind;
    this.position = pos;
    this.message = msg;
    this.related = relatedInfo;
    this.severity = severity;
    this.repair = repair;
    this.code = code;
  }

  @Override
  public Kind kind() {
    return this.kind;
  }

  @Override
  public Position position() {
    return position;
  }

  @Override
  public Iterable<Pair<Position, String>> related() {
    return related;
  }

  @Override
  public DiagnosticSeverity severity() {
    return severity;
  }

  @Override
  public Pair<Position, String> repair() {
    return repair;
  }

  @Override
  public String toString(boolean useMarkdown) {
    return message;
  }

  @Override
  public String toString() {
    return getClass().getName()
        + " [kind="
        + kind
        + ", position="
        + position
        + ", code="
        + code
        + ", message="
        + message
        + ", related="
        + related
        + ", severity="
        + severity
        + ", repair="
        + repair
        + "]";
  }

  public String code() {
    return code;
  }
}
