/*
 * @author Linghui Luo
 */
package magpiebridge.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import magpiebridge.core.MagpieServer;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * The Class SourceCodePositionFinder.
 *
 * @author Linghui Luo
 */
public class SourceCodePositionFinder {

  /**
   * Find line of code from given source file at given lineNumber.
   *
   * @param sourceFile the source file
   * @param lineNumber the line number
   * @param commentStart the inline comment start in the source language. e.g. "//" for java, and
   *     "#" for python
   * @return the source code info
   */
  public static Optional<SourceCodeInfo> findCode(
      File sourceFile, int lineNumber, String commentStart) {
    try {
      Optional<String> lineOp = Files.lines(sourceFile.toPath()).skip(lineNumber - 1).findFirst();
      if (lineOp.isPresent()) {
        String line = lineOp.get();
        int column = 0;
        line = line.split(commentStart)[0];
        for (char c : line.toCharArray()) {
          if (c != ' ') {
            break;
          }
          column++;
        }
        SourceCodeInfo info = new SourceCodeInfo();
        info.code = line.trim();
        info.range =
            new Range(
                new Position(lineNumber - 1, column),
                new Position(lineNumber - 1, column + info.code.length()));
        info.url = new URL("file://" + sourceFile.getAbsolutePath());
        return Optional.of(info);
      }
    } catch (IOException e) {
      MagpieServer.ExceptionLogger.log(e);
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * Find line of code from given javaFile at given lineNumber.
   *
   * @param javaFile the java file
   * @param lineNumber the line number
   * @return the source code info, it can return null if there are less lines in the file as given
   *     line number.
   */
  @Deprecated
  public static SourceCodeInfo findCode(File javaFile, int lineNumber) {
    return findCode(javaFile, lineNumber, "//").get();
  }

  /**
   * Find a java File with given className in the directory recursively.
   *
   * @param dir the directory where to find
   * @param className the className
   * @return the file
   */
  public static File find(File dir, String className) {
    Collection<File> files = FileUtils.listFiles(dir, null, true);
    for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
      File file = (File) iterator.next();
      String fileName = file.getName();
      if (fileName.endsWith(".java") && file.getName().equals(className + ".java")) return file;
    }
    MagpieServer.ExceptionLogger.log(
        "Couldn't find " + className + " in directory " + dir.toString());
    return null;
  }
}
