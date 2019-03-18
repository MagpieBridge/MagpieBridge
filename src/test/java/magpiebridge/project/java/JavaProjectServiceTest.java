package magpiebridge.project.java;

import magpiebridge.core.JavaProjectService;
import magpiebridge.projectservice.java.InferConfigGradle;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

  @Test
  public void testAndroidGradleProject() throws IOException, InterruptedException {
    Path root = Paths.get("src/test/resources/MyApplication/").toAbsolutePath();

    Path gradlePath;
    if (System.getProperty("os.name").startsWith("Windows")) {
      gradlePath = root.resolve("gradlew.bat");
    } else {
      gradlePath = root.resolve("gradlew");
    }
    // Build the project to download JARs to system
    System.out.println("Building app");
    InferConfigGradle.newProcessBuilderWithEnv(root)
        .directory(root.toFile())
        .command(gradlePath.toString(), ":app:assemble")
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor();
    System.out.println("Finished building app");

    JavaProjectService ps = new JavaProjectService();
    ps.setRootPath(root);
    assertEquals(112, ps.getClassPath().size());
    assertEquals(111, ps.getLibraryPath().size());
    assertTrue(
        ps.getClassPath()
            .contains(
                root.resolve("app").resolve("build").resolve("intermediates").resolve("javac")));
  }
}
