package magpiebridge.core;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import magpiebridge.project.java.InferConfig;
import magpiebridge.project.java.InferSourcePath;

/**
 * 
 * @author Linghui Luo
 *
 */
public class JavaProjectService implements IProjectService {
	private Path rootPath;
	private Set<Path> sourcePath;
	private Set<Path> classPath;
	private Set<String> externalDependencies;

	public JavaProjectService() {
		this.sourcePath = Collections.emptySet();
		this.classPath = Collections.emptySet();
		this.externalDependencies = Collections.emptySet();
	}

	public JavaProjectService(Set<Path> sourcePath, Set<Path> classPath, Set<String> externalDependencies) {
		this.sourcePath = sourcePath;
		this.classPath = classPath;
		this.externalDependencies = externalDependencies;
	}

	public Set<Path> getSourcePath() {
		if (this.sourcePath.isEmpty()) {
			// if source path is not specified by the user, infer the source path.
			this.sourcePath = InferSourcePath.sourcePath(rootPath);
		}
		return sourcePath;
	}

	public Set<Path> getClassPath() {
		if (this.classPath.isEmpty()) {
			// if class path is not specified by the user, infer the source path.
			InferConfig infer = new InferConfig(rootPath, externalDependencies);
			this.classPath = infer.classPath();
		}
		return classPath;
	}

	public Path getRootPath() {
		return rootPath;
	}

	@Override
	public void setRootPath(Path rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * Sets the source path, usually called by user.
	 *
	 * @param sourcePath the new source path
	 */
	public void setSourcePath(Set<Path> sourcePath) {
		this.sourcePath = sourcePath;
	}

	/**
	 * Sets the class path.
	 *
	 * @param classPath the new class path, usually called by user.
	 */
	public void setClassPath(Set<Path> classPath) {
		this.classPath = classPath;
	}

	/**
	 * Sets the external dependencies, usually called by user.
	 *
	 * @param dependences the new external dependencies
	 */
	public void setExternalDependencies(Set<String> dependences) {
		this.externalDependencies = dependences;
	}

}
