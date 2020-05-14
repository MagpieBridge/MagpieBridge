package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.apache.http.NameValuePair;

/**
 * Interface for any analysis that should be run by the MagpieServer.
 *
 * @author Julian Dolby and Linghui Luo
 */
public interface Analysis {
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
  public void analyze(Collection<? extends Module> files, AnalysisConsumer server, boolean rerun);

  /**
   * If desired, HTML to gather needed configuration, suitable for inclusion in a form
   *
   * @param rootPath the root of the workspace, if any, as supplied by the client
   * @return HTML text of form snippet or null
   */
  public default String configuration(Path rootPath) {
    return null;
  }

  public default void configure(List<NameValuePair> qparams) {}
}
