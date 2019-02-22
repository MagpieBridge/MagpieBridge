package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;

import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * 
 * @author Julian Dolby and Linghui Luo
 *
 */
public interface AnalysisResult {
  public Kind kind();

  public String toString(boolean useMarkdown);

  public Position position();

  public Iterable<Pair<Position, String>> related();

  public DiagnosticSeverity severity();

  public String repair();
}
