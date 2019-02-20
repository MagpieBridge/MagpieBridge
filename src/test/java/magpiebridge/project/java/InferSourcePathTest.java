package magpiebridge.project.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;

import magpiebridge.projectservice.java.InferSourcePath;

public class InferSourcePathTest {
	@Test
	public void test1() {
		Path root = Paths.get("src/test/resources/cryptoDemoTest/").toAbsolutePath();
		Set<Path> sourcePath = InferSourcePath.sourcePath(root);
		Path expected = Paths.get("src/test/resources/cryptoDemoTest/src").toAbsolutePath();
		assertEquals(expected.toString(), sourcePath.iterator().next().toString());
	}

	@Test
	public void test2() {
		Path root = Paths.get("src").toAbsolutePath();
		Set<Path> sourcePath = InferSourcePath.sourcePath(root);
		Path expected = Paths.get("src/main/java").toAbsolutePath();
		boolean found = false;
		for (Path p : sourcePath) {
			if (expected.equals(p)) {
				found = true;
			}
		}
		assertTrue(found);
	}

}
