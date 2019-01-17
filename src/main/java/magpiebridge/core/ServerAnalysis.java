package magpiebridge.core;

import java.util.Collection;

import com.ibm.wala.classLoader.Module;
/**
 * 
 * @author Julian Dolby and Linghui Luo
 *
 */
public interface ServerAnalysis {
	public String source();
	public void analyze(Collection<Module> files, MagpieServer server);
}
