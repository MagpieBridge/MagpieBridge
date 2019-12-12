package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import java.util.Collection;

public interface AbstractAnalysis<T extends AnalysisConsumer> {
  /**
   * The source of this analysis, usually the name of the analysis.
   *
   * @return the string
   */
  public String source();

  /**
   * The files to be analyzed.
   *
   * @param files the files
   * @param server the server
   */
  public void analyze(Collection<? extends Module> files, T server);
}
