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

  /**
   * consume the analysis results by the server.
   *
   * @param results analysis results
   * @param source where the results come from
   */
  void consume(Collection<? extends AnalysisResult> results, String source);
}
