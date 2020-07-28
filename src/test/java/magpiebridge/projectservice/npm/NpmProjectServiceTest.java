package magpiebridge.projectservice.npm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The NpmProjectServiceTest.
 *
 * @author Jonas
 */
public class NpmProjectServiceTest {

  private static final Path rootPath =
      Paths.get("src", "test", "resources", "DemoProjectNpm").toAbsolutePath();

  private static String npmCommand = "npm";

  @BeforeClass
  public static void setup() throws InterruptedException, IOException {
    assumeTrue("Did not find demo project", Files.exists(rootPath));
    boolean npmExists = false;
    try {
      if (System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH).contains("win")) {
        npmCommand = "npm.cmd";
      }
      int exitCode =
          new ProcessBuilder(npmCommand, "--version")
              .directory(rootPath.toFile())
              .inheritIO()
              .start()
              .waitFor();
      npmExists = exitCode == 0;
    } catch (IOException e) {
      npmExists = false;
    }
    assumeTrue("Npm is not installed", npmExists);

    // install the project to download packages to system
    int exitCode =
        new ProcessBuilder(npmCommand, "install")
            .directory(rootPath.toFile())
            .inheritIO()
            .start()
            .waitFor();
    assertEquals(0, exitCode);
  }

  @Test
  public void test() {
    Logger.getLogger(NpmProjectService.class.getName()).setLevel(Level.FINE);
    NpmProjectService npmProjectService = new NpmProjectService();
    npmProjectService.setRootPath(rootPath);
    assertEquals("npm", npmProjectService.getProjectType());
    assertTrue(npmProjectService.getDependencyPath().isPresent());
    assertTrue(npmProjectService.getPackageJson().isPresent());
    assertTrue(npmProjectService.getProjectPackage().isPresent());
    assertEquals(rootPath.resolve("node_modules"), npmProjectService.getDependencyPath().get());
    assertEquals(1, npmProjectService.getDirectDependencies().get().size());
    assertEquals(1, npmProjectService.getDependencies().size());
    assertTrue(npmProjectService.getDependency("lodash").isPresent());
    NpmPackage npmPackage = npmProjectService.getDependency("lodash").get();
    assertFalse(npmPackage.getDependencies().isPresent());
    assertTrue(npmPackage.getPath().isPresent());
    assertEquals(rootPath.resolve("node_modules").resolve("lodash"), npmPackage.getPath().get());
    assertEquals("4.17.19", npmPackage.getVersion());
  }
}
