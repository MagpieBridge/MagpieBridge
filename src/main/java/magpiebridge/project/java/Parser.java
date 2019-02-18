package magpiebridge.project.java;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;

/**
 * 
 * 
 * @author George Fraser
 * @see https://github.com/georgewfraser/java-language-server.git
 * 
 *      Modified by Linghui Luo 18.02.2019
 */
public class Parser {

	private static final Logger LOG = Logger.getLogger("main");
	private JavaCompiler compiler;
	private StandardJavaFileManager fileManager;

	public Parser() {
		// need to set java.home to JDK, otherwise ToolProvider.getSystemJavaCompiler
		// returns null.
		System.setProperty("java.home", System.getenv("JAVA_HOME"));
		compiler = ToolProvider.getSystemJavaCompiler();
		fileManager = compiler.getStandardFileManager(__ -> {
		}, null, Charset.defaultCharset());
	}

	public JavacTask parseTask(JavaFileObject file) {
		return (JavacTask) compiler.getTask(null, fileManager, err -> LOG.warning(err.getMessage(Locale.getDefault())),
				Collections.emptyList(), null, Collections.singletonList(file));
	}

	public JavacTask parseTask(Path source) {
		JavaFileObject file = fileManager.getJavaFileObjectsFromFiles(Collections.singleton(source.toFile())).iterator()
				.next();
		return parseTask(file);
	}

	public CompilationUnitTree parse(JavaFileObject file) {
		try {
			return parseTask(file).parse().iterator().next();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public CompilationUnitTree parse(Path source) {
		try {
			return parseTask(source).parse().iterator().next();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
