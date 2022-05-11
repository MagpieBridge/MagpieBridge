package magpiebridge.projectservice.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import magpiebridge.core.MagpieServer;
import magpiebridge.kotlinParserWrapper.KotlinParserWrapper;

/**
 * Infer the source path from a given project root path. Instead using the Parser from Java JDK
 * tool.jar from the original version, we use com.github.javaparser.JavaParser here. Modified by
 * Code adapted from https://github.com/georgewfraser/java-language-server.git
 *
 * @author George Fraser
 * @author Linghui Luo
 */
public class InferSourcePath {

  private static final Logger LOG = Logger.getLogger("main");
  private Set<String> packageNames;
  private Set<String> classFullQualifiedNames;

  private HashMap<String, String> classToFileRelation = new HashMap<>();

  protected static Stream<Path> allJavaFiles(Path dir) {
    PathMatcher match = FileSystems.getDefault().getPathMatcher("glob:*.java");
    try {
      return Files.walk(dir).filter(java -> match.matches(java.getFileName()));
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
    }
    return null;
  }

  protected static Stream<Path> allKotlinFiles(Path dir) {
    PathMatcher match = FileSystems.getDefault().getPathMatcher("glob:*.kt");

    try {
      return Files.walk(dir).filter(kotlin -> match.matches(kotlin.getFileName()));
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
    }
    return null;
  }

  public Set<Path> sourcePath(Path workspaceRoot) {
    LOG.info("Searching for source roots in " + workspaceRoot);
    packageNames = new HashSet<String>();
    classFullQualifiedNames = new HashSet<String>();
    class SourcePaths implements Consumer<Path> {
      Map<Path, Integer> sourceRoots = new HashMap<>();

      Optional<Path> infer(Path java) {
        JavaParser javaParser = new JavaParser();
        Optional<CompilationUnit> result = null;
        try {
          result = javaParser.parse(java).getResult();
        } catch (IOException e) {
          MagpieServer.ExceptionLogger.log(e);
          e.printStackTrace();
        }
        String packageName = "";
        if (result.isPresent()) {
          CompilationUnit cu = result.get();
          if (cu.getPackageDeclaration().isPresent()) {
            packageName = cu.getPackageDeclaration().get().getNameAsString();
            packageNames.add(packageName);
            classFullQualifiedNames.add(packageName + "." + cu.getPrimaryTypeName().get());

            classToFileRelation.put(
                packageName + "." + cu.getPrimaryTypeName().get(),
                java.toAbsolutePath().toString());
          } else {
            classFullQualifiedNames.add(cu.getPrimaryTypeName().get());

            classToFileRelation.put(
                java.toAbsolutePath().toString(), cu.getPrimaryTypeName().get());
          }
        }
        if (packageName.length() == 0) {
          return Optional.of(java.getParent());
        }
        String packagePath = packageName.replace('.', File.separatorChar);
        Path dir = java.getParent();
        if (!dir.endsWith(packagePath)) {
          LOG.warning("Java source file " + java + " is not in " + packagePath);
          return Optional.empty();
        } else {
          int up = Paths.get(packagePath).getNameCount();
          Path truncate = dir;
          for (int i = 0; i < up; i++) {
            truncate = truncate.getParent();
          }
          return Optional.of(truncate);
        }
      }

      @Override
      public void accept(Path java) {
        if (java.getFileName().toString().equals("module-info.java")) {
          return;
        }

        infer(java)
            .ifPresent(
                root -> {
                  int count = sourceRoots.getOrDefault(root, 0);
                  if (!root.startsWith(
                      workspaceRoot
                          + File.separator
                          + "target")) // filter generated java files of maven projects.
                  sourceRoots.put(root, count + 1);
                });
      }
    }
    SourcePaths checker = new SourcePaths();
    Stream<Path> javaFiles = allJavaFiles(workspaceRoot);
    if (javaFiles != null) javaFiles.forEach(checker);
    return checker.sourceRoots.keySet();
  }

  public Set<Path> kotlinSourcePath(Path workspaceRoot) {
    LOG.info("Searching for source roots in " + workspaceRoot);
    packageNames = new HashSet<String>();
    classFullQualifiedNames = new HashSet<String>();
    class SourcePaths implements Consumer<Path> {
      Map<Path, Integer> sourceRoots = new HashMap<>();

      Optional<Path> infer(Path kotlin) {
        KotlinParserWrapper kotlinParserWrapper = new KotlinParserWrapper();

        Optional<Path> returnValue = kotlinParserWrapper.parse(kotlin.toAbsolutePath().toString());

        packageNames.addAll(kotlinParserWrapper.getPackageNames());
        classFullQualifiedNames.addAll(kotlinParserWrapper.getClassFullQualifiedNames());
        classToFileRelation.putAll(kotlinParserWrapper.getClassToFileRelation());

        return returnValue;
      }

      @Override
      public void accept(Path kotlin) {
        if (kotlin.getFileName().toString().equals("module-info.java")) {
          return;
        }

        infer(kotlin)
            .ifPresent(
                root -> {
                  int count = sourceRoots.getOrDefault(root, 0);
                  if (!root.startsWith(workspaceRoot + File.separator + "target")
                      && !root.startsWith(
                          workspaceRoot
                              + File.separator
                              + "bin")) // filter generated java files of maven projects.
                  sourceRoots.put(root, count + 1);
                });
      }
    }
    SourcePaths checker = new SourcePaths();
    Stream<Path> kotlinFiles = allKotlinFiles(workspaceRoot);
    if (kotlinFiles != null) kotlinFiles.forEach(checker);
    return checker.sourceRoots.keySet();
  }

  public Set<String> getPackageNames() {
    return this.packageNames;
  }

  public Set<String> getClassFullQualifiedNames() {
    return this.classFullQualifiedNames;
  }

  public HashMap<String, String> getClassToFileRelation() {
    return this.classToFileRelation;
  }
}
