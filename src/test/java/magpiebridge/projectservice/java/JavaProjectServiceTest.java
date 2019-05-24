package magpiebridge.projectservice.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Ignore;
import org.junit.Test;

public class JavaProjectServiceTest {

  @Test
  public void testMavenProject() {
    // ignore it because CI times out
    JavaProjectService ps = new JavaProjectService();
    Path root = Paths.get("src/test/resources/DemoProjectMaven/").toAbsolutePath();
    ps.setRootPath(root);
    assertEquals(10, ps.getClassPath().size());
    assertEquals(8, ps.getLibraryPath().size());
    assertEquals(2, ps.getClassPath().size() - ps.getLibraryPath().size());
    Set<String> classNames = ps.getSourceClassFullQualifiedNames();
    assertTrue(classNames.contains("demo.SignatureExample"));
    assertTrue(classNames.contains("demo.FileOutputExample"));
    assertTrue(classNames.contains("demo.SecUtils"));
    assertTrue(classNames.contains("server.ServerMain"));
    assertTrue(classNames.contains("server.Server"));
  }

  @Test
  public void testGradleProject() throws IOException, InterruptedException {
    Path root = Paths.get("src/test/resources/DemoProjectGradle/").toAbsolutePath();

    Path gradlePath;
    if (System.getProperty("os.name").startsWith("Windows")) {
      gradlePath = root.resolve("gradlew.bat");
    } else {
      gradlePath = root.resolve("gradlew");
    }
    // Build the project to download JARs to system
    new ProcessBuilder()
        .directory(root.toFile())
        .command(gradlePath.toString(), "assemble")
        .start()
        .waitFor();

    JavaProjectService ps = new JavaProjectService();
    ps.setRootPath(root);
    assertEquals(9, ps.getClassPath().size());
    assertEquals(8, ps.getLibraryPath().size());
    assertTrue(ps.getClassPath().contains(root.resolve("build").resolve("classes")));
  }

  @Ignore
  public void testAndroidGradleProjectLocalRepo() throws IOException, InterruptedException {
    // This tests the old Android project format that does not use the public repo
    // maven.google.com, but instead a local one inside the Android SDK installation

    Path root = Paths.get("src/test/resources/MyApplicationLocalRepo/").toAbsolutePath();

    Path gradlePath;
    if (System.getProperty("os.name").startsWith("Windows")) {
      gradlePath = root.resolve("gradlew.bat");
    } else {
      gradlePath = root.resolve("gradlew");
    }
    // Build the project to download JARs to system
    System.out.println("Building app");
    int exitCode =
        InferConfigGradle.newProcessBuilderWithEnv(root)
            .directory(root.toFile())
            .command(gradlePath.toString(), ":app:compileReleaseSources")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor();
    if (exitCode != 0) {
      System.err.println("Warning: Assembling was not successful.");
    }
    System.out.println("Finished building app");

    JavaProjectService ps = new JavaProjectService();
    ps.setRootPath(root);
    assertEquals(5, ps.getClassPath().size());
    assertEquals(4, ps.getLibraryPath().size());

    // Ensure that a library was found not in the global Gradle cache, but inside the Android SDK
    // directory
    Pattern localSdkExtrasRepoPathPartPattern =
        Pattern.compile("extras([/\\\\])m2repository([/\\\\])com([/\\\\])android");
    assertTrue(
        ps.getLibraryPath().stream()
            .anyMatch(path -> localSdkExtrasRepoPathPartPattern.matcher(path.toString()).find()));
    assertTrue(
        ps.getClassPath()
            .contains(
                root.resolve("app").resolve("build").resolve("intermediates").resolve("classes")));
  }
}
