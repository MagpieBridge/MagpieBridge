package magpiebridge.util;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * SourceCodeReader provides methods to get source code at a given {@link Position}.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class SourceCodeReader {

  /**
   * Gets the lines of code at the give position without comment.
   *
   * @param p the position where to get the code
   * @return the lines
   */
  public static List<String> getLines(Position p) {
    return getLines(p, false);
  }

  /**
   * Gets the lines of code at the give position.
   *
   * @param p the position where to get the code
   * @param includeComment if comment should be removed.
   * @return the lines
   */
  public static List<String> getLines(Position p, boolean includeComment) {
    List<String> lines = new ArrayList<>();

    String url = p.getURL().toString();
    if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
      // take care of url in windows
      if (!url.startsWith("file:///")) {
        url = url.replace("file://", "file:///");
      }
    }
    try {
      File file = new File(new URL(url).getFile());
      if (file.exists() && file.isFile()) {
        try (FileReader freader = new FileReader(file);
            BufferedReader reader = new BufferedReader(freader)) {
          String currentLine = null;
          int line = 0;
          do {
            currentLine = reader.readLine();
            if (currentLine == null) {
              return lines;
            }
            line++;
          } while (p.getFirstLine() > line);

          // first line
          lines.add(removeComment(currentLine.substring(p.getFirstCol()), includeComment));

          while (p.getLastLine() < line) {
            currentLine = reader.readLine();
            line++;
            if (p.getLastLine() == line) {
              lines.add(removeComment(currentLine.substring(0, p.getLastCol()), includeComment));
            } else {
              lines.add(removeComment(currentLine, includeComment));
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return lines;
  }

  /**
   * Removes the comment in a line.
   *
   * @param line the line
   * @param includeComment tells if code comment should be included in the line.
   * @return the string
   */
  public static String removeComment(String line, boolean includeComment) {
    if (includeComment) return line;
    if (line.contains("//")) {
      line = line.split("(\\s)*//")[0];
    }
    return line;
  }

  /**
   * Gets the lines of code at the give position with/without comment.
   *
   * @param p the position where to get the code
   * @param includeComment if comment should be removed.
   * @return the lines in string
   * @throws Exception the exception
   */
  public static String getLinesInString(Position p, boolean includeComment) throws Exception {
    List<String> lines = getLines(p, includeComment);
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      if (i == lines.size() - 1) {
        result.append(lines.get(i));
      } else {
        result.append(lines.get(i)).append('\n');
      }
    }
    return result.toString();
  }
  /**
   * Gets the lines of code at the give position without comment.
   *
   * @param p the position where to get the code
   * @return the lines in string
   * @throws Exception the exception
   */
  public static String getLinesInString(Position p) throws Exception {
    return getLinesInString(p, false);
  }
}
