package magpiebridge.project.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * 
 * Infer the source path from a given project root path.
 * 
 * @author George Fraser
 * @see https://github.com/georgewfraser/java-language-server.git
 * 
 *      Modified by Linghui Luo 18.02.2019
 */
public class InferSourcePath {

	private static final Logger LOG = Logger.getLogger("main");

	protected static Stream<Path> allJavaFiles(Path dir) {
		PathMatcher match = FileSystems.getDefault().getPathMatcher("glob:*.java");

		try {
			return Files.walk(dir).filter(java -> match.matches(java.getFileName()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Set<Path> sourcePath(Path workspaceRoot) {
		LOG.info("Searching for source roots in " + workspaceRoot);

		class SourcePaths implements Consumer<Path> {
			int certaintyThreshold = 10;
			Map<Path, Integer> sourceRoots = new HashMap<>();

			boolean alreadyKnown(Path java) {
				for (Path root : sourceRoots.keySet()) {
					if (java.startsWith(root) && sourceRoots.get(root) > certaintyThreshold)
						return true;
				}
				return false;
			}

			Optional<Path> infer(Path java) {
				Parser parser = new Parser();
				String packageName = Objects.toString(parser.parse(java).getPackageName(), "");
				String packagePath = packageName.replace('.', File.separatorChar);
				Path dir = java.getParent();
				if (!dir.endsWith(packagePath)) {
					LOG.warning("Java source file " + java + " is not in " + packagePath);
					return Optional.empty();
				} else {
					int up = Paths.get(packagePath).getNameCount();
					Path truncate = dir;
					for (int i = 0; i < up; i++)
						truncate = truncate.getParent();
					return Optional.of(truncate);
				}
			}

			@Override
			public void accept(Path java) {
				if (java.getFileName().toString().equals("module-info.java"))
					return;

				if (!alreadyKnown(java)) {
					infer(java).ifPresent(root -> {
						int count = sourceRoots.getOrDefault(root, 0);
						sourceRoots.put(root, count + 1);
					});
				}
			}
		}
		SourcePaths checker = new SourcePaths();
		allJavaFiles(workspaceRoot).forEach(checker);
		return checker.sourceRoots.keySet();
	}

}
