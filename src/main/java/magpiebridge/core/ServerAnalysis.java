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
   * Perform analysis.
   *
   * @param files the files to be considered
   * @param server the server
   */
  public void analyze(Collection<Module> files, MagpieServer server);

  /**
   * Do anything which ought to be done before the actual analysis. It is only called once when the
   * first file is opened.
   *
   * @param ps the project service
   */
  public void prepare(IProjectService ps);
}
