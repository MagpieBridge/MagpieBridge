package magpiebridge.project.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import magpiebridge.core.JavaProjectService;
import org.junit.Ignore;

public class JavaProjectServiceTest {

  @Ignore
  public void testMavenProject() {
    // ignore it because CI times out
    JavaProjectService ps = new JavaProjectService();
    Path root = Paths.get("src/test/resources/DemoProject/").toAbsolutePath();
    ps.setRootPath(root);
    assertNotEquals(0, ps.getClassPath().size());
    assertNotEquals(0, ps.getLibraryPath().size());
    assertEquals(2, ps.getClassPath().size() - ps.getLibraryPath().size());
  }
}
