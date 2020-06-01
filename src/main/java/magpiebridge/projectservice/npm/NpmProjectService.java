package magpiebridge.projectservice.npm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import magpiebridge.core.IProjectService;

public class NpmProjectService implements IProjectService {

  private static final Logger logger = Logger.getLogger(NpmProjectService.class.getName());

  private Optional<Path> rootPath = Optional.empty();
  private Optional<Path> dependencyPath = Optional.empty();
  private Optional<NpmPackage> projectPackage = Optional.empty();
  private Map<String, NpmPackage> projectDependencies = new HashMap<>();

  @Override
  public void setRootPath(Path rootPath) {
    projectDependencies.clear();
    this.rootPath = Optional.ofNullable(rootPath);
    this.rootPath.ifPresent(path -> {
      if (Files.exists(path) && Files.isDirectory(path)) {
        Path node_modules = path.resolve("node_modules");
        if (Files.exists(node_modules)) {
          dependencyPath = Optional.of(node_modules);
        }
      }
    });
  }

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

  @Override
  public String getProjectType() {
    return "npm";
  }

  public Optional<NpmPackage> getDependency(String name) {
    if (projectDependencies.isEmpty()) {
      initDependencies();
    }
    return Optional.ofNullable(projectDependencies.get(name));
  }

  public Collection<NpmPackage> getDependencies() {
    if (projectDependencies.isEmpty()) {
      initDependencies();
    }
    return projectDependencies.values();
  }

  private void initDependencies() {
    this.rootPath.ifPresent(path -> {
      if (Files.exists(path) && Files.isDirectory(path)) {
        Path packageJsonPath = path.resolve("package.json");
        if (Files.exists(packageJsonPath)) {
          projectPackage = Optional.of(new NpmPackage());
          exploreDependencies(projectPackage.get(), packageJsonPath, 0);
        }
      } else {
        // put marker in dependency list so we can stop checking
        projectDependencies.put("none", null);
      }
    });
  }

  public Optional<Path> getDependencyPath() {
    return dependencyPath;
  }
}
