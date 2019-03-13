package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import java.util.Collection;

/** @author Julian Dolby and Linghui Luo */
public interface ServerAnalysis {
  public String source();

  public void analyze(Collection<Module> files, MagpieServer server);
}
