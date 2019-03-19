package magpiebridge.projectservice.java;

import com.google.common.annotations.VisibleForTesting;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@VisibleForTesting
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

  @VisibleForTesting
  public static ProcessBuilder newProcessBuilderWithEnv(Path workspaceRoot) {
    ProcessBuilder pb = new ProcessBuilder();
    if (Strings.isNullOrEmpty(pb.environment().getOrDefault("ANDROID_HOME", null))) {
      androidSdkPath(workspaceRoot)
          .map(Path::toString)
          .ifPresent(sdkPath -> pb.environment().put("ANDROID_HOME", sdkPath));
    }
    return pb;
  }

  private static Optional<Path> androidSdkPath(Path workspaceRoot) {
    Path localProperties = workspaceRoot.resolve("local.properties");
    Path sdkDirFromLocalProperties;
    if (Files.exists(localProperties)) {
      Pattern sdkDirPattern = Pattern.compile("sdk\\.dir=(.*)");
      try {
        Optional<String> op =
            Files.readAllLines(localProperties).stream()
                .map(sdkDirPattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .findFirst();
        if (!op.isPresent()) {
          return null;
        } else {
          String path = op.get();
          if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            // take care of windows
            path = path.replace("\\:", ":");
          }
          sdkDirFromLocalProperties = Paths.get(path);
        }
      } catch (IOException e) {
        sdkDirFromLocalProperties = null;
      }
    } else {
      sdkDirFromLocalProperties = null;
    }

    Path userHome = Paths.get(System.getProperty("user.home"));

    String envAndroidSdkPathStr = System.getenv("ANDROID_SDK_PATH");
    Path envAndroidSdkPath =
        Strings.isNullOrEmpty(envAndroidSdkPathStr) ? null : Paths.get(envAndroidSdkPathStr);

    String envAndroidHomePathStr = System.getenv("ANDROID_HOME");
    Path envAndroidHomePath =
        Strings.isNullOrEmpty(envAndroidHomePathStr) ? null : Paths.get(envAndroidHomePathStr);

    return Stream.of(
            sdkDirFromLocalProperties,
            envAndroidSdkPath,
            envAndroidHomePath,
            userHome
                .resolve("AppData")
                .resolve("Local")
                .resolve("Android")
                .resolve("Sdk"), // Windows
            userHome.resolve("Library").resolve("Android").resolve("sdk"), // Mac
            userHome.resolve("Android").resolve("Sdk")) // Linux
        .filter(Objects::nonNull)
        .filter(Files::exists)
        .findFirst();
  }

  static Optional<Path> findGradleJar(
      Path gradleHome, Artifact artifact, boolean source, Path workspaceRoot) {
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
    Path androidSdkPath = androidSdkPath(workspaceRoot).orElse(null);
    if (androidSdkPath != null) {
      Path extrasPath = androidSdkPath.resolve("extras");
      Stream<String> patternPart1 = Stream.of(toGlobPathPart(extrasPath), "extras", "**");
      Stream<String> patternPart2 = Stream.of(artifact.groupId.split("\\."));
      Stream<String> patternPart3 =
          Stream.of(
              artifact.artifactId,
              artifact.version,
              InferConfig.fileNameJarOrAar(artifact, source));
      String pattern =
          "glob:"
              + Stream.concat(Stream.concat(patternPart1, patternPart2), patternPart3)
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
      Set<String> subProjects = gradleSubprojects(workspaceRoot);
      LOG.info("Gradle subprojects: " + subProjects);

      // For each subproject, collect dependencies
      Pattern dependencyPattern = Pattern.compile("--- (.*):(.*):(.*)");
      Set<Artifact> dependencies = new LinkedHashSet<>();
      for (String subProject : subProjects) {
        LOG.info("Running " + gradleBinary + " " + subProject + ":dependencies");
        Process dependencyListing =
            newProcessBuilderWithEnv(workspaceRoot)
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
      LOG.info("Gradle dependencies: " + dependencies);

      return dependencies;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Set<String> gradleSubprojects(Path workspaceRoot) {
    String gradleBinary = getGradleBinary(workspaceRoot).toString();
    Pattern projectPattern = Pattern.compile("[pP]roject '(.*)'$");
    LOG.info("Running " + gradleBinary + " projects");
    try {
      Process projectsListing =
          newProcessBuilderWithEnv(workspaceRoot)
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
                .collect(Collectors.toCollection(LinkedHashSet::new)); // Ensures
        // mutability
        subProjects.add(""); // Add root project
      }
      return subProjects;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

  static Set<Path> workspaceClassPath(Path workspaceRoot) {
    Stream<Path> subprojectDirs =
        InferConfigGradle.gradleSubprojects(workspaceRoot).stream()
            .filter(subproject -> !subproject.isEmpty())
            .map(subproject -> removePrefix(subproject, ":"))
            .flatMap(
                subproject ->
                    Stream.of(
                        workspaceRoot
                            .resolve(subproject)
                            .resolve("build")
                            .resolve("intermediates")
                            .resolve("javac"),
                        workspaceRoot.resolve(subproject).resolve("build").resolve("classes")));

    Stream<Path> rootProjectDirs =
        Stream.of(
            workspaceRoot.resolve("build").resolve("intermediates").resolve("javac"),
            workspaceRoot.resolve("build").resolve("classes"));
    return Stream.concat(rootProjectDirs, subprojectDirs)
        .filter(Files::exists)
        .collect(Collectors.toSet());
  }

  private static String removePrefix(String str, String prefix) {
    return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
  }

  static Set<Path> gradleBuildClassPath(Path workspaceRoot, Path gradleHome) {
    LOG.info("Looking up gradle dependencies");
    Collection<Artifact> artifacts = gradleDependencies(workspaceRoot);
    int depCount = artifacts.size();
    AtomicInteger c = new AtomicInteger();
    return artifacts
        .parallelStream()
        .map(dep -> findGradleJar(gradleHome, dep, false, workspaceRoot))
        .peek(__ -> LOG.info("Processed " + c.incrementAndGet() + "/" + depCount + " dependencies"))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }
}
