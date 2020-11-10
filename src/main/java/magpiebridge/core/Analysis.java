package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import magpiebridge.core.analysis.configuration.ConfigurationAction;
import magpiebridge.core.analysis.configuration.ConfigurationOption;

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
   * @param files the files that have been opened in the editor.
   * @param server the server which consumes the analysis results
   * @param rerun tells if the analysis should be reran
   */
  public void analyze(Collection<? extends Module> files, T server, boolean rerun);

  /**
   * Define configuration options allowed by the analysis, override it if there are options
   * supported.
   *
   * @return a list of configuration options
   */
  public default List<ConfigurationOption> getConfigurationOptions() {
    return new ArrayList<>();
  }

  /**
   * Define interactions allowed by the analysis with users, override it if there are actions
   * supported.
   *
   * @return a list of configured actions
   */
  public default List<ConfigurationAction> getConfiguredActions() {
    return new ArrayList<>();
  }

  /**
   * Configure the analysis with the given configuration options. The default implement doesn't do
   * anything.
   *
   * @param configuration the configuration used to configure the analysis.
   */
  public default void configure(List<ConfigurationOption> configuration) {}

  /**
   * Clean up defined by the analysis. This will be called @link{{@link MagpieServer#shutdown()}}.
   */
  public default void cleanUp() {}
}
