package magpiebridge.projectservice.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import magpiebridge.core.IProjectService;
import magpiebridge.util.FileUtils;

/**
 * The Class JavaProjectService provides the configuration information of a java project.
 *
 * @author Linghui Luo
 */
public class JavaProjectService implements IProjectService {

  /** The root path. */
  private Optional<Path> rootPath;

  /** The source path. */
  private Set<Path> sourcePath;

  /** The source class full qualified names. */
  private Set<String> sourceClassFullQualifiedNames;

  /** The class path. */
  private Set<Path> classPath;

  /** The library path. */
  private Set<Path> libraryPath;

  /** The external dependencies. */
  private Set<String> externalDependencies;

  private JavaProjectType projectType;

  private Map<String, Path> signatureToSourceFile;

  private Set<Path> javaFiles;

  private InferConfig infer;

  /** Instantiates a new java project service. */
  public JavaProjectService() {
    this.sourcePath = Collections.emptySet();
    this.sourceClassFullQualifiedNames = Collections.emptySet();
    this.classPath = Collections.emptySet();
    this.libraryPath = Collections.emptySet();
    this.externalDependencies = Collections.emptySet();
    this.rootPath = Optional.empty();
    this.signatureToSourceFile = new HashMap<>();
    this.javaFiles = Collections.emptySet();
  }

  /**
   * Instantiates a new java project service with customized source code path, class path and
   * external dependencies.
   *
   * @param sourcePath the source path
   * @param classPath the class path
   * @param externalDependencies the external dependencies
   */
  public JavaProjectService(
      Set<Path> sourcePath, Set<Path> classPath, Set<String> externalDependencies) {
    this();
    this.sourcePath = sourcePath;
    this.classPath = classPath;
    this.externalDependencies = externalDependencies;
  }

  /**
   * Gets the source path.
   *
   * @return the source path
   */
  public Set<Path> getSourcePath() {
    if (this.sourcePath.isEmpty()) {
      if (rootPath.isPresent()) {
        // if source path is not specified by the user, infer the source path.
        InferSourcePath infer = new InferSourcePath();
        this.sourcePath = infer.sourcePath(rootPath.get());
        this.sourceClassFullQualifiedNames = infer.getClassFullQualifiedNames();
      }
    }
    return sourcePath;
  }

  /**
   * Gets the source class full qualified names.
   *
   * @return the source class full qualified names
   */
  public Set<String> getSourceClassFullQualifiedNames() {
    if (this.sourcePath.isEmpty()) {
      getSourcePath();
    }
    return this.sourceClassFullQualifiedNames;
  }

  /**
   * Gets the class path.
   *
   * @return the class path
   */
  public Set<Path> getClassPath() {
    if (this.classPath.isEmpty()) {
      // if class path is not specified by the user, infer the source path.
      if (rootPath.isPresent()) {
        if (infer == null) {
          infer = new InferConfig(rootPath.get(), externalDependencies);
        }
        this.classPath = infer.classPath();
        this.libraryPath = infer.libraryClassPath();
      }
    }
    return classPath;
  }

  /**
   * Gets the library class path.
   *
   * @return the library path
   */
  public Set<Path> getLibraryPath() {
    if (this.libraryPath.isEmpty()) {
      if (rootPath.isPresent()) {
        if (infer == null) {
          infer = new InferConfig(rootPath.get(), externalDependencies);
        }
        this.classPath = infer.classPath();
        this.libraryPath = infer.libraryClassPath();
      }
    }
    return this.libraryPath;
  }

  /**
   * Gets the project root path.
   *
   * @return the root path
   */
  public Optional<Path> getRootPath() {
    return rootPath;
  }

  /*
   * (non-Javadoc)
   *
   * @see magpiebridge.core.IProjectService#setRootPath(java.nio.file.Path)
   */
  @Override
  public void setRootPath(Path rootPath) {
    this.rootPath = Optional.ofNullable(rootPath);
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

  @Override
  public String getProjectType() {
    if (projectType == null) {
      if (infer == null) {
        infer = new InferConfig(rootPath.get(), externalDependencies);
      }
      projectType = infer.getProjectType();
    }
    return this.projectType.toString();
  }

  /**
   * Get the source file path from class signature.
   *
   * @param classSignature
   * @return
   * @throws IOException
   */
  public Optional<Path> getSourceFilePath(String classSignature) throws IOException {
    if (javaFiles.isEmpty() && rootPath.isPresent()) {
      javaFiles = FileUtils.collectFilePathWithExtension(rootPath.get(), ".java");
    }
    if (this.signatureToSourceFile.containsKey(classSignature)) {
      return Optional.of(signatureToSourceFile.get(classSignature));
    }
    String postfix = classSignature.replace(".", "/") + ".java";
    for (Path f : javaFiles) {
      if (f.toAbsolutePath().endsWith(postfix)) {
        this.signatureToSourceFile.put(classSignature, f);
        return Optional.of(f);
      }
    }
    return Optional.empty();
  }

  public Process mvnCommand(String cmd) throws IOException {
    if (infer == null) {
      infer = new InferConfig(rootPath.get(), externalDependencies);
    }
    return infer.mvnCommand(cmd);
  }
}
