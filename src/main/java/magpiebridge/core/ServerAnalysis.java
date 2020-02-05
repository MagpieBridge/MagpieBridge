package magpiebridge.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.lsp4j.Diagnostic;

/**
 * The Interface for analysis which reports warnings in the client IDE.
 *
 * @author Julian Dolby and Linghui Luo
 */
public interface ServerAnalysis extends Analysis {

  default List<Diagnostic> history(Collection<AnalysisResult> current, List<Diagnostic> past) {
    return new LinkedList<>();
  }
}
