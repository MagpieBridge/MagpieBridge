package magpiebridge.project.java;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class InferSourcePathTest {
	@Test
	public void test1() {
		Path root = Paths.get("./src/test/resources/cryptoDemoTest/").toAbsolutePath();
		Set<Path> sourcePath = InferSourcePath.sourcePath(root);
		Path expected = Paths.get("./src/test/resources/cryptoDemoTest/src").toAbsolutePath();
		assertEquals(expected.toString(), sourcePath.iterator().next().toString());
	}
	
	@Ignore
	public void test2() {
		Path root = Paths.get("").toAbsolutePath();
		System.out.println(root);
		Set<Path> sourcePath = InferSourcePath.sourcePath(root);
		for (Path p : sourcePath) {
			System.out.println(p.toString());
		}
	}
	
}
