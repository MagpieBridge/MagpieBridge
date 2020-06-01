package magpiebridge.projectservice.npm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import magpiebridge.core.IProjectService;

/**
 * The Class NpmProjectService.
 */
public class NpmProjectService implements IProjectService {

  /** The Constant logger. */
  private static final Logger logger = Logger.getLogger(NpmProjectService.class.getName());

  /** The root path. */
  private Optional<Path> rootPath = Optional.empty();
  
  /** The dependency path. Usually node_modules. */
  private Optional<Path> dependencyPath = Optional.empty();
  
  /** The path to the package.json. */
  private Optional<Path> packageJson = Optional.empty();
  
  /** The project package parsed from the package.json. */
  private Optional<NpmPackage> projectPackage = Optional.empty();
  
  /** All project dependencies. */
  private Map<String, NpmPackage> projectDependencies = new HashMap<>();


  /**
   * Sets the root path.
   *
   * @param rootPath the new root path
   */
  @Override
  public void setRootPath(Path rootPath) {
    projectDependencies.clear();
    this.rootPath = Optional.ofNullable(rootPath);
    this.rootPath.ifPresent(path -> {
      if (Files.exists(path) && Files.isDirectory(path)) {
        Path packageJsonPath = path.resolve("package.json");
        if (Files.exists(packageJsonPath)) {
          this.packageJson = Optional.of(packageJsonPath);
        }
        Path node_modules = path.resolve("node_modules");
        if (Files.exists(node_modules)) {
          dependencyPath = Optional.of(node_modules);
        }
      }
    });
  }

  /**
   * Explore dependencies.
   *
   * @param parentPackage the parent package
   * @param packageJsonPath the package json path
   * @param depth the depth
   */
  private void exploreDependencies(NpmPackage parentPackage, Path packageJsonPath, int depth) {
    try {
      JsonObject packageJson = new Gson()
          .fromJson(String.join("\n", Files.readAllLines(packageJsonPath)), JsonObject.class);
      if (packageJson.has("name")) {
        parentPackage.setName(packageJson.get("name").getAsString());
      }
      if (packageJson.has("version")) {
        parentPackage.setVersion(packageJson.get("version").getAsString());
      }
      if (logger.isLoggable(Level.FINE)) {
        String depthPrefix = "";
        if (depth > 0) {
          depthPrefix = "|";
          for (int i = 0; i < depth; i++) {
            depthPrefix += "-";
          }
        }
        logger.fine(depthPrefix + parentPackage.getName() + "@" + parentPackage.getVersion());
      }
      if (packageJson.has("dependencies")) {
        List<NpmPackage> dependencies = packageJson.get("dependencies").getAsJsonObject().entrySet()
            .stream().map(dependency -> {
              if (!projectDependencies.containsKey(dependency.getKey())) {
                String name = dependency.getKey();
                String version = dependency.getValue().getAsString();
                NpmPackage dependencyPackage = new NpmPackage(name, version);
                projectDependencies.put(name, dependencyPackage);
                dependencyPath.ifPresent(node_modules -> {
                  Path packagePath = node_modules.resolve(name);
                  if (Files.exists(packagePath)) {
                    dependencyPackage.setPath(packagePath);
                    Path dependencyPackageJsonPath = packagePath.resolve("package.json");
                    if (Files.exists(dependencyPackageJsonPath)) {
                      exploreDependencies(dependencyPackage, dependencyPackageJsonPath, depth + 1);
                    }
                  }
                });
              }
              return projectDependencies.get(dependency.getKey());
            }).collect(Collectors.toList());
        parentPackage.setDependencies(dependencies);
      }
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Finished processing " + parentPackage.getName());
      }
    } catch (JsonSyntaxException | IOException e) {
    }
  }

  /**
   * Gets the project type.
   *
   * @return the project type
   */
  @Override
  public String getProjectType() {
    return "npm";
  }

  /**
   * Gets the dependency for the given name if it exists.
   *
   * @param name the name
   * @return the dependency.
   */
  public Optional<NpmPackage> getDependency(String name) {
    if (projectDependencies.isEmpty()) {
      initDependencies();
    }
    return Optional.ofNullable(projectDependencies.get(name));
  }

  /**
   * Gets the path to the package.json.
   *
   * @return the package json
   */
  public Optional<Path> getPackageJson() {
    return packageJson;
  }
  
  /**
   * Gets the project package. Will be empty if the package.json does not exist
   *
   * @return the project package
   */
  public Optional<NpmPackage> getProjectPackage() {
    if (!projectPackage.isPresent()) {
      initDependencies();
    }
    return projectPackage;
  }
  
  /**
   * Gets the direct dependencies.
   *
   * @return the direct dependencies
   */
  public Optional<List<NpmPackage>> getDirectDependencies(){
    if (projectPackage.isPresent()) {
      return projectPackage.get().getDependencies();
    }
    return Optional.empty();
  }
  
  /**
   * Gets the list of all dependencies (includes transient dependencies).
   *
   * @return the dependencies
   */
  public List<NpmPackage> getDependencies() {
    if (projectDependencies.isEmpty()) {
      initDependencies();
    }
    return new ArrayList<>(projectDependencies.values());
  }
  
  /**
   * Recompute dependencies.
   */
  public void recomputeDependencies() {
    //recompute all paths and the set of dependencies assuming the root path has not changed
    rootPath.ifPresent(this::setRootPath);
    initDependencies();
  }

  /**
   * Initializes the dependencies.
   */
  private void initDependencies() {
    this.packageJson.ifPresent(packageJsonPath -> {
          projectPackage = Optional.of(new NpmPackage());
          exploreDependencies(projectPackage.get(), packageJsonPath, 0);
    });
  }

  /**
   * Gets the dependency path.
   *
   * @return the dependency path
   */
  public Optional<Path> getDependencyPath() {
    return dependencyPath;
  }
}
