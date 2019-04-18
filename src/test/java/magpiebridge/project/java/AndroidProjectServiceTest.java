package magpiebridge.project.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import magpiebridge.core.AndroidProjectService;
import magpiebridge.core.JavaProjectService;
import magpiebridge.projectservice.java.InferConfigGradle;
import org.junit.Test;

public class AndroidProjectServiceTest {

  @Test
  public void testApkPath() {
    Path root = Paths.get("src/test/resources/MyApplication/").toAbsolutePath();
    AndroidProjectService ps = new AndroidProjectService();
    ps.setRootPath(root);
    assertTrue(ps.getApkPath().isPresent());
    assertEquals(
        ps.getApkPath().get(),
        Paths.get("src/test/resources/MyApplication/app/build/outputs/apk/debug/app-debug.apk")
            .toAbsolutePath());
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
    int exitCode =
        InferConfigGradle.newProcessBuilderWithEnv(root)
            .directory(root.toFile())
            .command(gradlePath.toString(), ":app:assemble")
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
    assertEquals(113, ps.getClassPath().size());
    assertEquals(112, ps.getLibraryPath().size());
    assertTrue(
        ps.getClassPath()
            .contains(
                root.resolve("app").resolve("build").resolve("intermediates").resolve("javac")));
  }
}
