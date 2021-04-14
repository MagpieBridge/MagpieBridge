/*
 * @author Linghui Luo
 */
package magpiebridge.projectservice.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import magpiebridge.core.MagpieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class infers the project configuration. Code adapted from
 * https://github.com/georgewfraser/java-language-server.git
 *
 * @author George Fraser
 * @author Linghui Luo
 * @author Christian Brüggemann
 */
public class InferConfig {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Root of the workspace that is currently open in VSCode */
  private final Path workspaceRoot;
  /** External dependencies specified manually by the user */
  private final Collection<String> externalDependencies;
  /** Location of the maven repository, usually ~/.m2 */
  private final Path mavenHome;
  /** Location of the gradle cache, usually ~/.gradle */
  private final Path gradleHome;

  private Set<Path> cachedBuildClassPath;

  private JavaProjectType projectType;

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

  public Set<Path> classPath() {
    HashSet<Path> result = new HashSet<>();
    result.addAll(buildClassPath());
    Set<Path> workspaceClassPath = workspaceClassPath();
    result.addAll(workspaceClassPath);
    return result;
  }

  public Set<Path> libraryClassPath() {
    return buildClassPath();
  }

  /**
   * Find directories that contain java .class files in the workspace, for example files generated
   * by maven in target/classes
   *
   * @return the class path.
   */
  public Set<Path> workspaceClassPath() {
    // externalDependencies
    if (!externalDependencies.isEmpty()) {
      return Collections.emptySet();
    }

    // Maven
    if (Files.exists(workspaceRoot.resolve("pom.xml"))) {
      this.projectType = JavaProjectType.Maven;
      try {
        return Files.walk(workspaceRoot)
            .flatMap(this::mavenOutputDirectory)
            .collect(Collectors.toSet());
      } catch (IOException e) {
        MagpieServer.ExceptionLogger.log(
            "Couldn't infer Maven project configuration, because Maven is not installed. Install Maven, or add its location to the system PATH variable.");
        MagpieServer.ExceptionLogger.log(e);
      }
    }

    // Bazel
    if (Files.exists(workspaceRoot.resolve("WORKSPACE"))) {
      Path bazelBin = workspaceRoot.resolve("bazel-bin");

      if (Files.exists(bazelBin) && Files.isSymbolicLink(bazelBin)) {
        this.projectType = JavaProjectType.Bazel;
        return bazelOutputDirectories(bazelBin);
      }
    }

    // Gradle
    if (InferConfigGradle.hasGradleProject(workspaceRoot)) {
      this.projectType = JavaProjectType.Gradle;
      return InferConfigGradle.workspaceClassPath(workspaceRoot);
    }

    // Eclipse Java Project
    Path classPath = workspaceRoot.resolve(workspaceRoot.resolve(".classpath"));
    if (Files.exists(classPath)) {
      this.projectType = JavaProjectType.EclipseJava;
      return parseClassPathFile(classPath);
    }

    return Collections.emptySet();
  }

  /**
   * Parses the class path file.
   *
   * @param classPath the class path
   * @return the sets the
   */
  public Set<Path> parseClassPathFile(Path classPath) {
    Set<Path> path = new HashSet<Path>();
    try {
      File file = classPath.toFile();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(file);
      NodeList list = doc.getElementsByTagName("classpathentry");
      for (int i = 0; i < list.getLength(); i++) {
        Node node = list.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          String kind = element.getAttribute("kind");
          String p = element.getAttribute("path");
          if (kind.equals("output")) {
            String str = workspaceRoot.toAbsolutePath() + File.separator + p;
            Path cp = Paths.get(str).toAbsolutePath();
            if (Files.exists(cp)) path.add(cp);
          }
        }
      }
    } catch (Exception e) {
      MagpieServer.ExceptionLogger.log(e);
    }
    return path;
  }

  /**
   * Recognize build root files like pom.xml and return compiler output directories
   *
   * @param file the file
   * @return the stream
   */
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
    if (cachedBuildClassPath != null) {
      return cachedBuildClassPath;
    }

    // externalDependencies
    if (!externalDependencies.isEmpty()) {
      Set<Path> result = new HashSet<Path>();
      for (String id : externalDependencies) {
        Artifact a = Artifact.parse(id);
        if (a != null) {
          Optional<Path> found = findAnyJar(a, false);
          if (found.isPresent()) {
            result.add(found.get());
          } else {
            logger.warn(
                String.format("Couldn't find jar for %s in %s or %s", a, mavenHome, gradleHome));
          }
        }
      }
      cachedBuildClassPath = result;
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
          logger.warn(String.format("Couldn't find jar for %s in %s", a, mavenHome));
        }
      }
      cachedBuildClassPath = result;
      return result;
    }

    // Bazel
    if (Files.exists(workspaceRoot.resolve("WORKSPACE"))) {
      Set<Path> result = new HashSet<Path>();
      Path bazelGenFiles = workspaceRoot.resolve("bazel-genfiles");
      if (Files.exists(bazelGenFiles) && Files.isSymbolicLink(bazelGenFiles)) {
        Set<Path> jars = bazelJars(bazelGenFiles);
        result.addAll(jars);
      }
      cachedBuildClassPath = result;
      return result;
    }

    // Gradle
    if (InferConfigGradle.hasGradleProject(workspaceRoot)) {
      Set<Path> result = InferConfigGradle.gradleBuildClassPath(workspaceRoot, gradleHome);
      cachedBuildClassPath = result;
      return result;
    }

    cachedBuildClassPath = Collections.emptySet();
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
        MagpieServer.ExceptionLogger.log(e);
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
      Set<Path> dirs = new HashSet<Path>();
      findBazelJavac(bazelBinTarget.toFile(), workspaceRoot.toFile(), dirs);
      return dirs;
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
    }
    return Collections.emptySet();
  }

  /** Search bazel-genfiles for jars */
  private Set<Path> bazelJars(Path bazelGenFiles) {
    try {
      Path target = Files.readSymbolicLink(bazelGenFiles);

      return Files.walk(target)
          .filter(file -> file.getFileName().toString().endsWith(".jar"))
          .collect(Collectors.toSet());
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
    }
    return Collections.emptySet();
  }

  private Optional<Path> findAnyJar(Artifact artifact, boolean source) {
    Optional<Path> maven = findMavenJar(artifact, source);
    if (maven.isPresent()) {
      return maven;
    } else {
      return InferConfigGradle.findGradleJar(gradleHome, artifact, source, workspaceRoot);
    }
  }

  private Optional<Path> findMavenJar(Artifact artifact, boolean source) {
    Path jar =
        mavenHome
            .resolve("repository")
            .resolve(
                artifact.groupId.replace('.', File.separatorChar).replace(':', File.separatorChar))
            .resolve(artifact.artifactId)
            .resolve(artifact.version)
            .resolve(fileNameJar(artifact, source));
    if (Files.exists(jar)) {
      return Optional.of(jar);
    } else {
      return Optional.empty();
    }
  }

  static String fileNameJar(Artifact artifact, boolean source) {
    return artifact.artifactId + '-' + artifact.version + (source ? "-sources" : "") + ".jar";
  }

  static String fileNameJarOrAar(Artifact artifact, boolean source) {
    return artifact.artifactId + '-' + artifact.version + (source ? "-sources" : "") + ".{aar,jar}";
  }

  /**
   * This method runs the command <code>mvn dependency:list</code> to get all dependencies.
   *
   * @return the collection
   */
  private Collection<Artifact> mvnDependencies() {
    Path pomXml = workspaceRoot.resolve("pom.xml");
    try {
      if (Files.exists(pomXml)) {
        Objects.requireNonNull(pomXml, "pom.xml path is null");
        Process dependencyListing =
            new ProcessBuilder()
                .directory(workspaceRoot.toFile())
                .command(getMvnCommand(), "dependency:list")
                .start();
        Pattern dependencyPattern = Pattern.compile(".*\\s{2}(.*):(.*):(.*):(.*):(.*)");
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(dependencyListing.getInputStream()));
        Set<Artifact> dependencies = new LinkedHashSet<>();
        reader
            .lines()
            .map(dependencyPattern::matcher)
            .filter(Matcher::find)
            .map(matcher -> new Artifact(matcher.group(1), matcher.group(2), matcher.group(4)))
            .forEach(dependencies::add);
        reader.close();
        return dependencies;
      }
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(
          "Couldn't infer Maven project configuration while trying to run Maven. Either install Maven or add its location to the system PATH variable.");
    }
    return Collections.emptyList();
  }

  private static String getMvnCommand() {
    String mvnCommand = "mvn";
    if (File.separatorChar == '\\') {
      // handle windows
      mvnCommand = findExecutableOnPath("mvn.cmd");
      if (mvnCommand == null) {
        mvnCommand = findExecutableOnPath("mvn.bat");
      }
    }
    return mvnCommand;
  }

  static String findExecutableOnPath(String name) {
    for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
      File file = new File(dirname, name);
      if (file.isFile() && file.canExecute()) {
        return file.getAbsolutePath();
      }
    }
    return null;
  }

  public JavaProjectType getProjectType() {
    if (this.projectType == null) {
      // Maven
      if (Files.exists(workspaceRoot.resolve("pom.xml"))) {
        this.projectType = JavaProjectType.Maven;
        return this.projectType;
      }

      // Gradle
      if (InferConfigGradle.hasGradleProject(workspaceRoot)) {
        this.projectType = JavaProjectType.Gradle;
        return this.projectType;
      }

      // Bazel
      if (Files.exists(workspaceRoot.resolve("WORKSPACE"))) {
        Path bazelBin = workspaceRoot.resolve("bazel-bin");
        if (Files.exists(bazelBin) && Files.isSymbolicLink(bazelBin)) {
          this.projectType = JavaProjectType.Bazel;
          return this.projectType;
        }
      }
      // Eclipse Java Project
      Path classPath = workspaceRoot.resolve(workspaceRoot.resolve(".classpath"));
      if (Files.exists(classPath)) {
        this.projectType = JavaProjectType.EclipseJava;
        return this.projectType;
      }
    }
    return this.projectType;
  }
}
