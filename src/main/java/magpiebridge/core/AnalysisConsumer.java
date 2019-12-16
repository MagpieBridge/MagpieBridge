package magpiebridge.core;

import java.util.Collection;

@FunctionalInterface
public interface AnalysisConsumer {

  void consume(Collection<AnalysisResult> results, String source);
}
