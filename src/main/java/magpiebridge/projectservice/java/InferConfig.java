package magpiebridge.projectservice.java;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author George Fraser
 * @see https://github.com/georgewfraser/java-language-server.git
 *     <p>Modified and extended by Linghui Luo
 */
public class InferConfig {
  private static final Logger LOG = Logger.getLogger("main");

  /** Root of the workspace that is currently open in VSCode */
  private final Path workspaceRoot;
  /** External dependencies specified manually by the user */
  private final Collection<String> externalDependencies;
  /** Location of the maven repository, usually ~/.m2 */
  private final Path mavenHome;
  /** Location of the gradle cache, usually ~/.gradle */
  private final Path gradleHome;

  InferConfig(
      Path workspaceRoot,
      Collection<String> externalDependencies,
      Path mavenHome,
      Path gradleHome) {
    this.workspaceRoot = workspaceRoot;
    this.externalDependencies = externalDependencies;
    this.mavenHome = mavenHome;
    this.gradleHome = gradleHome;
  }

  public InferConfig(Path workspaceRoot, Collection<String> externalDependencies) {
    this(workspaceRoot, externalDependencies, defaultMavenHome(), defaultGradleHome());
  }

  public InferConfig(Path workspaceRoot) {
    this(workspaceRoot, Collections.emptySet(), defaultMavenHome(), defaultGradleHome());
  }

  private static Path defaultMavenHome() {
    return Paths.get(System.getProperty("user.home")).resolve(".m2");
  }

  private static Path defaultGradleHome() {
    String gradleUserHome = System.getenv("GRADLE_USER_HOME");
    if (gradleUserHome != null
        && !gradleUserHome.isEmpty()
        && Files.exists(Paths.get(gradleUserHome))) {
      return Paths.get(gradleUserHome);
    }

    return Paths.get(System.getProperty("user.home")).resolve(".gradle");
  }

  /** @return */
  public Set<Path> classPath() {
    HashSet<Path> result = new HashSet<Path>();
    result.addAll(buildClassPath());
    result.addAll(workspaceClassPath());
    return result;
  }

  public Set<Path> libraryClassPath() {
    return buildClassPath();
  }

  /**
   * Find directories that contain java .class files in the workspace, for example files generated
   * by maven in target/classes
   */
  public Set<Path> workspaceClassPath() {
    // externalDependencies
    if (!externalDependencies.isEmpty()) {
      return Collections.emptySet();
    }

    // Maven
    if (Files.exists(workspaceRoot.resolve("pom.xml"))) {
      try {
        return Files.walk(workspaceRoot)
            .flatMap(this::mavenOutputDirectory)
            .collect(Collectors.toSet());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Bazel
    if (Files.exists(workspaceRoot.resolve("WORKSPACE"))) {
      Path bazelBin = workspaceRoot.resolve("bazel-bin");

      if (Files.exists(bazelBin) && Files.isSymbolicLink(bazelBin)) {
        return bazelOutputDirectories(bazelBin);
      }
    }

    // Gradle
    if (hasGradleProject()) {
      return Stream.of(
              workspaceRoot.resolve("build").resolve("intermediates").resolve("javac"),
              workspaceRoot.resolve("build").resolve("classes"))
          .filter(Files::exists)
          .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }

  /** Recognize build root files like pom.xml and return compiler output directories */
  public Stream<Path> mavenOutputDirectory(Path file) {
    if (file.getFileName().toString().equals("pom.xml")) {
      Path target = file.resolveSibling("target");

      if (Files.exists(target) && Files.isDirectory(target)) {
        return Stream.of(target.resolve("classes"), target.resolve("test-classes"));
      }
    }

    return Stream.empty();
  }

  /**
   * Find .jar files for external dependencies, for examples maven dependencies in ~/.m2 or jars in
   * bazel-genfiles
   */
  private Set<Path> buildClassPath() {
    // externalDependencies
    if (!externalDependencies.isEmpty()) {
      Set<Path> result = new HashSet<Path>();
      for (String id : externalDependencies) {
        Artifact a = Artifact.parse(id);
        Optional<Path> found = findAnyJar(a, false);
        if (found.isPresent()) {
          result.add(found.get());
        } else {
          LOG.warning(
              String.format("Couldn't find jar for %s in %s or %s", a, mavenHome, gradleHome));
        }
      }
      return result;
    }

    // Maven
    if (Files.exists(workspaceRoot.resolve("pom.xml"))) {
      Set<Path> result = new HashSet<Path>();
      for (Artifact a : mvnDependencies()) {
        Optional<Path> found = findMavenJar(a, false);
        if (found.isPresent()) {
          result.add(found.get());
        } else {
          LOG.warning(String.format("Couldn't find jar for %s in %s", a, mavenHome));
        }
      }
      return result;
    }

    // Bazel
    if (Files.exists(workspaceRoot.resolve("WORKSPACE"))) {
      Set<Path> result = new HashSet<Path>();
      Path bazelGenFiles = workspaceRoot.resolve("bazel-genfiles");

      if (Files.exists(bazelGenFiles) && Files.isSymbolicLink(bazelGenFiles)) {
        LOG.info("Looking for bazel generated files in " + bazelGenFiles);
        Set<Path> jars = bazelJars(bazelGenFiles);
        LOG.info(String.format("Found %d generated-files directories", jars.size()));
        result.addAll(jars);
      }
      return result;
    }

    // Gradle
    if (hasGradleProject()) {
      return gradleDependencies().stream()
          .map(dep -> findGradleJar(dep, false))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }

  private void findBazelJavac(File bazelRoot, File workspaceRoot, Set<Path> acc) {
    // If _javac directory exists, search it for dirs with names like lib*_classes
    File javac = new File(bazelRoot, "_javac");
    if (javac.exists()) {
      PathMatcher match = FileSystems.getDefault().getPathMatcher("glob:**/lib*_classes");
      try {
        Files.walk(javac.toPath())
            .filter(match::matches)
            .filter(Files::isDirectory)
            .forEach(acc::add);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    // Recurse into all directories that mirror the structure of the workspace
    if (bazelRoot.isDirectory()) {
      String[] children = bazelRoot.list((__, name) -> new File(workspaceRoot, name).exists());
      for (String child : children) {
        File bazelChild = new File(bazelRoot, child);
        File workspaceChild = new File(workspaceRoot, child);
        findBazelJavac(bazelChild, workspaceChild, acc);
      }
    }
  }

  /**
   * Search bazel-bin for per-module output directories matching the pattern:
   *
   * <p>bazel-bin/path/to/module/_javac/rule/lib*_classes
   */
  private Set<Path> bazelOutputDirectories(Path bazelBin) {
    try {
      Path bazelBinTarget = Files.readSymbolicLink(bazelBin);
      LOG.info("Searching for bazel output directories in " + bazelBinTarget);

      Set<Path> dirs = new HashSet<Path>();
      findBazelJavac(bazelBinTarget.toFile(), workspaceRoot.toFile(), dirs);
      LOG.info(String.format("Found %d bazel output directories", dirs.size()));

      return dirs;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Search bazel-genfiles for jars */
  private Set<Path> bazelJars(Path bazelGenFiles) {
    try {
      Path target = Files.readSymbolicLink(bazelGenFiles);

      return Files.walk(target)
          .filter(file -> file.getFileName().toString().endsWith(".jar"))
          .collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Find source .jar files in local repository. */
  private Set<Path> buildDocPath() {
    // externalDependencies
    if (!externalDependencies.isEmpty()) {
      Set<Path> result = new HashSet<Path>();
      for (String id : externalDependencies) {
        Artifact a = Artifact.parse(id);
        Optional<Path> found = findAnyJar(a, true);
        if (found.isPresent()) {
          result.add(found.get());
        } else {
          LOG.warning(
              String.format("Couldn't find doc jar for %s in %s or %s", a, mavenHome, gradleHome));
        }
      }
      return result;
    }

    // Maven
    if (Files.exists(workspaceRoot.resolve("pom.xml"))) {
      Set<Path> result = new HashSet<>();
      for (Artifact a : mvnDependencies()) {
        findMavenJar(a, true).ifPresent(result::add);
      }
      return result;
    }

    // Gradle
    if (hasGradleProject()) {
      return gradleDependencies().stream()
          .map(dep -> findGradleJar(dep, true))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());
    }

    // TODO Bazel

    return Collections.emptySet();
  }

  private boolean hasGradleProject() {
    return Files.exists(workspaceRoot.resolve("build.gradle"));
  }

  private Optional<Path> findAnyJar(Artifact artifact, boolean source) {
    Optional<Path> maven = findMavenJar(artifact, source);
    if (maven.isPresent()) {
      return maven;
    } else {
      return findGradleJar(artifact, source);
    }
  }

  private Optional<Path> findMavenJar(Artifact artifact, boolean source) {
    Path jar =
        mavenHome
            .resolve("repository")
            .resolve(artifact.groupId.replace('.', File.separatorChar))
            .resolve(artifact.artifactId)
            .resolve(artifact.version)
            .resolve(fileName(artifact, source));

    if (Files.exists(jar)) {
      return Optional.of(jar);
    } else {
      return Optional.empty();
    }
  }

  private static String toGlobPathPart(Path path) {
    Path absolutePath = path.toAbsolutePath();
    String root = Strings.nullToEmpty(absolutePath.getRoot().toString()).replace('\\', '/');

    return root
        + StreamSupport.stream(absolutePath.spliterator(), false)
            .map(p -> p.getFileName().toString())
            .collect(Collectors.joining("/"));
  }

  private Optional<Path> findGradleJar(Artifact artifact, boolean source) {
    // Search for
    // caches/modules-*/files-*/groupId/artifactId/version/*/artifactId-version[-sources].jar
    Path base = gradleHome.resolve("caches");
    String pattern =
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
                fileName(artifact, source));
    PathMatcher match = FileSystems.getDefault().getPathMatcher(pattern);

    try {
      return Files.walk(base, 7).filter(match::matches).findFirst();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String fileName(Artifact artifact, boolean source) {
    return artifact.artifactId + '-' + artifact.version + (source ? "-sources" : "") + ".jar";
  }

  private static List<Artifact> dependencyList(Path pomXml) {
    Objects.requireNonNull(pomXml, "pom.xml path is null");

    try {
      // Tell maven to output deps to a temporary file
      Path outputFile = Files.createTempFile("deps", ".txt");

      // TODO consider using mvn dependency:copy-dependencies instead
      String cmd =
          String.format(
              "%s dependency:list -DincludeScope=test -DoutputFile=%s",
              getMvnCommand(), outputFile);
      File workingDirectory = pomXml.toAbsolutePath().getParent().toFile();
      int result = Runtime.getRuntime().exec(cmd, null, workingDirectory).waitFor();

      if (result != 0) {
        throw new RuntimeException("`" + cmd + "` returned " + result);
      }

      return readDependencyList(outputFile);
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<Artifact> readDependencyList(Path outputFile) {
    Pattern artifact = Pattern.compile(".*:.*:.*:.*:.*");

    try (InputStream in = Files.newInputStream(outputFile)) {
      return new BufferedReader(new InputStreamReader(in))
          .lines()
          .map(String::trim)
          .filter(line -> artifact.matcher(line).matches())
          .map(Artifact::parse)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Collection<Artifact> mvnDependencies() {
    Path pomXml = workspaceRoot.resolve("pom.xml");

    if (Files.exists(pomXml)) {
      return dependencyList(pomXml);
    }

    return Collections.emptyList();
  }

  private Collection<Artifact> gradleDependencies() {
    String gradleBinary = getGradleBinary().toString();

    try {
      // Find all subprojects
      Pattern projectPattern = Pattern.compile("project '(.*)'$");
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

  private Path getGradleBinary() {
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
    String gradlePath = findExecutableOnPath("gradle");
    if (gradlePath != null && Files.exists(Paths.get(gradlePath))) {
      return Paths.get(gradlePath);
    }

    throw new RuntimeException("Could not find gradle binary.");
  }

  private static String getMvnCommand() {
    String mvnCommand = "mvn";
    if (File.separatorChar == '\\') {
      mvnCommand = findExecutableOnPath("mvn.cmd");
      if (mvnCommand == null) {
        mvnCommand = findExecutableOnPath("mvn.bat");
      }
    }
    return mvnCommand;
  }

  private static String findExecutableOnPath(String name) {
    for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
      File file = new File(dirname, name);
      if (file.isFile() && file.canExecute()) {
        return file.getAbsolutePath();
      }
    }
    return null;
  }
}
