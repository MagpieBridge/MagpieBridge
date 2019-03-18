package magpiebridge.projectservice.java;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InferConfigGradle {

  private static final Logger LOG = Logger.getLogger("main");

  private InferConfigGradle() {}

  private static String toGlobPathPart(Path path) {
    Path absolutePath = path.toAbsolutePath();
    String root = Strings.nullToEmpty(absolutePath.getRoot().toString()).replace('\\', '/');

    return root
        + StreamSupport.stream(absolutePath.spliterator(), false)
            .map(p -> p.getFileName().toString())
            .collect(Collectors.joining("/"));
  }

  static Optional<Path> findGradleJar(Path gradleHome, Artifact artifact, boolean source) {
    // Search for
    // caches/modules-*/files-*/groupId/artifactId/version/*/artifactId-version[-sources].jar
    Path base = gradleHome.resolve("caches");
    String gradleCachePattern =
        "glob:"
            + String.join(
                "/", // File.separator does *not* work on Windows
                toGlobPathPart(base),
                "modules-*",
                "files-*",
                artifact.groupId,
                artifact.artifactId,
                artifact.version,
                "*",
                InferConfig.fileNameJarOrAar(artifact, source));
    PathMatcher match = FileSystems.getDefault().getPathMatcher(gradleCachePattern);

    try {
      Optional<Path> gradleCacheMatch = Files.walk(base, 7).filter(match::matches).findFirst();
      if (gradleCacheMatch.isPresent()) {
        return gradleCacheMatch;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Try Android SDK paths
    String androidSdkPath = System.getenv("ANDROID_SDK_PATH");
    if (!Strings.isNullOrEmpty(androidSdkPath)) {
      Path extrasPath = Paths.get(androidSdkPath, "extras");
      Stream<String> patternPart1 =
          Stream.of(toGlobPathPart(extrasPath), "extras", "**");
      Stream<String> patternPart2 = Stream.of(artifact.groupId.split("\\."));
      Stream<String> patternPart3 =
          Stream.of(
              artifact.artifactId,
              artifact.version,
              InferConfig.fileNameJarOrAar(artifact, source));
      String pattern =
          Stream.concat(Stream.concat(patternPart1, patternPart2), patternPart3)
              .collect(Collectors.joining("/"));
      PathMatcher androidSdkMatch = FileSystems.getDefault().getPathMatcher(pattern);

      try {
        if (Files.exists(extrasPath)) {
          return Files.walk(extrasPath).filter(androidSdkMatch::matches).findFirst();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return Optional.empty();
  }

  static Collection<Artifact> gradleDependencies(Path workspaceRoot) {
    String gradleBinary = getGradleBinary(workspaceRoot).toString();

    try {
      Set<String> subProjects = gradleSubprojects(gradleBinary, workspaceRoot);
      LOG.info("Subprojects: " + subProjects);

      // For each subproject, collect dependencies
      Pattern dependencyPattern = Pattern.compile("--- (.*):(.*):(.*)");
      Set<Artifact> dependencies = new LinkedHashSet<>();
      for (String subProject : subProjects) {
        LOG.info("Running " + gradleBinary + " " + subProject + ":dependencies");
        Process dependencyListing =
            new ProcessBuilder()
                .directory(workspaceRoot.toFile())
                .command(gradleBinary, subProject + ":dependencies")
                .start();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(dependencyListing.getInputStream()))) {
          reader
              .lines()
              .map(dependencyPattern::matcher)
              .filter(Matcher::find)
              .map(matcher -> new Artifact(matcher.group(1), matcher.group(2), matcher.group(3)))
              .forEach(dependencies::add);
        }
      }
      LOG.info("Dependencies: " + dependencies);

      return dependencies;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Set<String> gradleSubprojects(String gradleBinary, Path workspaceRoot)
      throws IOException {
    // Find all subprojects
    Pattern projectPattern = Pattern.compile("[pP]roject '(.*)'$");
    LOG.info("Running " + gradleBinary + " projects");
    Process projectsListing =
        new ProcessBuilder()
            .directory(workspaceRoot.toFile())
            .command(gradleBinary, "projects")
            .start();
    Set<String> subProjects;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(projectsListing.getInputStream()))) {
      subProjects =
          reader
              .lines()
              .filter(line -> !line.isEmpty() && !line.startsWith("Root project"))
              .map(projectPattern::matcher)
              .filter(Matcher::find)
              .map(matcher -> matcher.group(1))
              .collect(Collectors.toCollection(LinkedHashSet::new)); // Ensures mutability
      subProjects.add(""); // Add root project
    }
    return subProjects;
  }

  private static Path getGradleBinary(Path workspaceRoot) {
    boolean isWindows = System.getProperty("os.name").startsWith("Windows");

    // Try gradle wrapper
    if (isWindows) {
      Path gradlewBat = workspaceRoot.resolve("gradlew.bat");
      if (Files.exists(gradlewBat)) {
        return gradlewBat;
      }
    } else {
      Path gradlew = workspaceRoot.resolve("gradlew");
      if (Files.exists(gradlew)) {
        return gradlew;
      }
    }

    // Try system-wide gradle installation
    String gradlePath = InferConfig.findExecutableOnPath("gradle");
    if (gradlePath != null && Files.exists(Paths.get(gradlePath))) {
      return Paths.get(gradlePath);
    }

    throw new RuntimeException("Could not find gradle binary.");
  }

  static boolean hasGradleProject(Path workspaceRoot) {
    return Files.exists(workspaceRoot.resolve("build.gradle"));
  }
}
