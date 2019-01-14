package magpie.magpiebridge;

import java.util.Collection;

import com.ibm.wala.classLoader.Module;

public interface ServerAnalysis {
	public String source();
	public void analyze(Collection<Module> files, MagpieServer server);
}
