package magpiebridge.projectservice.java;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import magpiebridge.core.IProjectService;

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

  private JavaLanguage javaLanguage;

  private HashMap<String, String> classToFileRelation;

  /** Instantiates a new java project service. */
  public JavaProjectService() {
    this(JavaLanguage.JAVA);
  }

  public JavaProjectService(JavaLanguage javaLanguage) {
    this.sourcePath = Collections.emptySet();
    this.sourceClassFullQualifiedNames = Collections.emptySet();
    this.classPath = Collections.emptySet();
    this.libraryPath = Collections.emptySet();
    this.externalDependencies = Collections.emptySet();
    this.rootPath = Optional.empty();
    this.javaLanguage = javaLanguage;
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
    this(sourcePath, classPath, externalDependencies, JavaLanguage.JAVA);
  }

  public JavaProjectService(
      Set<Path> sourcePath,
      Set<Path> classPath,
      Set<String> externalDependencies,
      JavaLanguage javaLanguage) {
    this.sourcePath = sourcePath;
    this.classPath = classPath;
    this.externalDependencies = externalDependencies;
    this.javaLanguage = javaLanguage;
  }

  /**
   * Gets the source path.
   *
   * @return the source path
   */
  public Set<Path> getSourcePath() {
    this.classToFileRelation = new HashMap<>();

    if (this.sourcePath.isEmpty()) {
      if (rootPath.isPresent()) {
        // if source path is not specified by the user, infer the source path.
        InferSourcePath infer = new InferSourcePath();

        if (javaLanguage == JavaLanguage.KOTLIN) {
          this.sourcePath = infer.kotlinSourcePath(rootPath.get());
        } else {
          this.sourcePath = infer.sourcePath(rootPath.get());
        }

        this.sourceClassFullQualifiedNames = infer.getClassFullQualifiedNames();

        this.classToFileRelation.putAll(infer.getClassToFileRelation());
      }
    }
    return sourcePath;
  }

  public HashMap<String, String> getClassToFileRelation() {
    if (this.sourcePath.isEmpty()) {
      getSourcePath();
    }

    if (classToFileRelation.isEmpty()) return new HashMap<>();

    return classToFileRelation;
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
        InferConfig infer = new InferConfig(rootPath.get(), externalDependencies);
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
        InferConfig infer = new InferConfig(rootPath.get(), externalDependencies);
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
      InferConfig infer = new InferConfig(rootPath.get(), externalDependencies);
      projectType = infer.getProjectType();
    }
    return this.projectType.toString();
  }
}
