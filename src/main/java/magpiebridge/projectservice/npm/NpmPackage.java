package magpiebridge.projectservice.npm;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class NpmPackage {

  private String name;
  private String version;
  private Optional<Path> path = Optional.empty();
  private Optional<List<NpmPackage>> dependencies = Optional.empty();

  public NpmPackage() {}

  public NpmPackage(String name, String version) {
    this();
    this.name = name;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setPath(Path path) {
    this.path = Optional.ofNullable(path);
  }

  public Optional<Path> getPath() {
    return path;
  }

  public void setDependencies(List<NpmPackage> dependencies) {
    this.dependencies = Optional.ofNullable(dependencies);
  }

  public Optional<List<NpmPackage>> getDependencies() {
    return dependencies;
  }

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
