package magpiebridge.core;

import java.util.Collection;

/**
 * Define analysis consumer which consumes the analysis results. The {@link MagpieServer} is such an
 * implementation of it.
 *
 * @author Julian Dolby and Linghui Luo
 */
@FunctionalInterface
public interface AnalysisConsumer {

  void consume(Collection<AnalysisResult> results, String source);
}
