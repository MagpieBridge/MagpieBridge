package magpiebridge.projectservice.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.Test;

public class InferSourcePathTest {
  @Test
  public void test1() {
    Path root = Paths.get("src/test/resources/cryptoDemoTest/").toAbsolutePath();
    Set<Path> sourcePath = new InferSourcePath().sourcePath(root);
    Path expected = Paths.get("src/test/resources/cryptoDemoTest/src").toAbsolutePath();
    assertEquals(expected.toString(), sourcePath.iterator().next().toString());
  }

  @Test
  public void testCurrentProject() {
    Path root = Paths.get("src").toAbsolutePath();
    Set<Path> sourcePath = new InferSourcePath().sourcePath(root);
    Path expected1 = Paths.get("src/main/java").toAbsolutePath();
    boolean found = false;
    for (Path p : sourcePath) {
      if (expected1.equals(p)) {
        found = true;
      }
    }
    assertTrue(found);
    found = false;
    Path expected2 = Paths.get("src/test/java").toAbsolutePath();
    for (Path p : sourcePath) {
      if (expected2.equals(p)) {
        found = true;
      }
    }
    assertTrue(found);
  }

  @Test
  public void testMavenProject() {
    InferSourcePath infer = new InferSourcePath();
    Path root = Paths.get("src/test/resources/DemoProjectMaven/").toAbsolutePath();
    Set<Path> sourcePath = infer.sourcePath(root);
    assertEquals(sourcePath.size(), 1);
    Path expected = Paths.get("src/test/resources/DemoProjectMaven/src").toAbsolutePath();
    assertEquals(expected.toString(), sourcePath.iterator().next().toString());
    assertTrue(infer.getPackageNames().contains("server"));
    assertTrue(infer.getPackageNames().contains("demo"));
    Set<String> classNames = infer.getClassFullQualifiedNames();
    assertTrue(classNames.contains("demo.SignatureExample"));
    assertTrue(classNames.contains("demo.FileOutputExample"));
    assertTrue(classNames.contains("demo.SecUtils"));
    assertTrue(classNames.contains("server.ServerMain"));
    assertTrue(classNames.contains("server.Server"));
    assertTrue(classNames.contains("server.User"));
  }

  @Test
  public void testGradleProject() {
    Path root = Paths.get("src/test/resources/DemoProjectGradle/").toAbsolutePath();
    Set<Path> sourcePath = new InferSourcePath().sourcePath(root);
    assertEquals(sourcePath.size(), 1);
    Path expected =
        Paths.get("src/test/resources/DemoProjectGradle/src/main/java/").toAbsolutePath();
    assertEquals(expected.toString(), sourcePath.iterator().next().toString());
  }

  @Test
  public void testEclipseJavaProject1() {
    Path root = Paths.get("src/test/resources/StandardJCATasks/Task1").toAbsolutePath();
    Set<Path> sourcePath = new InferSourcePath().sourcePath(root);
    assertEquals(sourcePath.size(), 1);
    Path expected = Paths.get("src/test/resources/StandardJCATasks/Task1/src").toAbsolutePath();
    assertEquals(expected.toString(), sourcePath.iterator().next().toString());
  }

  @Test
  public void testEclipseJavaProject2() {
    Path root = Paths.get("src/test/resources/StandardJCATasks/Task3").toAbsolutePath();
    Set<Path> sourcePath = new InferSourcePath().sourcePath(root);
    assertEquals(sourcePath.size(), 1);
    Path expected = Paths.get("src/test/resources/StandardJCATasks/Task3/src").toAbsolutePath();
    assertEquals(expected.toString(), sourcePath.iterator().next().toString());
  }
}
