/*
 * @author Linghui Luo
 */
package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import java.util.Collection;

/**
 * The Interface ServerAnalysis.
 *
 * @author Julian Dolby and Linghui Luo
 */
public interface ServerAnalysis {

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
   * @param rerun if the analysis should be reran.
   */
  public void analyze(Collection<Module> files, MagpieServer server, boolean rerun);
}
