package magpiebridge.core;

import java.util.Collection;

public interface AnalysisConsumer {

  void consume(Collection<AnalysisResult> results, String source);
}
