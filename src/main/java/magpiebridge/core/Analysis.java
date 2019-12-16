package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import java.util.Collection;

/**
 * Interface for any analysis should be ran by the MagpieServer.
 *
 * @author Julian Dolby and Linghui Luo
 */
public interface Analysis<T extends AnalysisConsumer> {
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
   * @param server the server which consumes the analysis results
   * @param rerun tells if the analysis should be reran
   */
  public void analyze(Collection<? extends Module> files, T server, boolean rerun);
}
