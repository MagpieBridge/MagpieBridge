package magpiebridge.projectservice.npm;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * The Class NpmPackage.
 *
 * @author Jonas
 */
public class NpmPackage {

  /** The name. */
  private String name;

  /** The version. */
  private String version;

  /** The path. */
  private Optional<Path> path = Optional.empty();

  /** The dependencies. */
  private Optional<List<NpmPackage>> dependencies = Optional.empty();

  /** Instantiates a new npm package. */
  public NpmPackage() {}

  /**
   * Instantiates a new npm package.
   *
   * @param name the name
   * @param version the version
   */
  public NpmPackage(String name, String version) {
    this();
    this.name = name;
    this.version = version;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Sets the path.
   *
   * @param path the new path
   */
  public void setPath(Path path) {
    this.path = Optional.ofNullable(path);
  }

  /**
   * Gets the path if it was found in the dependency location.
   *
   * @return the path
   */
  public Optional<Path> getPath() {
    return path;
  }

  /**
   * Sets the dependencies.
   *
   * @param dependencies the new dependencies
   */
  public void setDependencies(List<NpmPackage> dependencies) {
    this.dependencies = Optional.ofNullable(dependencies);
  }

  /**
   * Gets the dependencies. Will be empty if this package has no dependencies
   *
   * @return the dependencies
   */
  public Optional<List<NpmPackage>> getDependencies() {
    return dependencies;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "NpmPackage [name="
        + name
        + ", version="
        + version
        + ", path="
        + path
        + ", dependencies="
        + dependencies
        + "]";
  }
}
