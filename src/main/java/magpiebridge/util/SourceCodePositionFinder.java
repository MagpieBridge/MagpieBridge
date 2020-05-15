/*
 * @author Linghui Luo
 */
package magpiebridge.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/** The Class SourceCodePositionFinder. */
public class SourceCodePositionFinder {

  /**
   * Find line of code from given javaFile at given lineNumber.
   *
   * @param javaFile the java file
   * @param lineNumber the line number
   * @return the source code info
   */
  public static SourceCodeInfo findCode(File javaFile, int lineNumber) {
    SourceCodeInfo info = new SourceCodeInfo();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(javaFile));
      String line;
      int i = 0;
      while ((line = reader.readLine()) != null) {
        i++;
        if (i == lineNumber) {
          int column = 0;
          line = line.split("//")[0];
          for (char c : line.toCharArray()) {
            if (c != ' ') {
              break;
            }
            column++;
          }
          info.code = line.trim();
          info.range =
              new Range(
                  new Position(lineNumber - 1, column),
                  new Position(lineNumber - 1, column + info.code.length()));
          info.url = new URL("file://" + javaFile.getAbsolutePath());
          break;
        }
      }
      reader.close();
      return info;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
    return null;
  }
}
