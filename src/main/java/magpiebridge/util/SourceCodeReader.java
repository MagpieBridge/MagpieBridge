package magpiebridge.util;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SourceCodeReader provides methods to get source code at a given {@link Position}.
 *
 * @author Julian Dolby and Linghui Luo
 */
public class SourceCodeReader {

  /**
   * Gets the lines of code at the give position.
   *
   * @param p the position where to get the code
   * @return the lines
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  public static List<String> getLines(Position p) throws Exception {
    List<String> lines = new ArrayList<>();
    try {
      File file = new File(p.getURL().getFile());
      if (file.exists() && file.isFile()) {
        BufferedReader reader = new BufferedReader(new FileReader(file));
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
        lines.add(currentLine.substring(p.getFirstCol()));

        while (p.getLastLine() < line) {
          currentLine = reader.readLine();
          line++;
          if (p.getLastLine() == line) {
            lines.add(currentLine.substring(0, p.getLastCol()));
          } else {
            lines.add(currentLine);
          }
        }
      }
    } catch (IOException e) {
      throw e;
    }
    return lines;
  }

  /**
   * Gets the lines of code at the give position.
   *
   * @param p the position where to get the code
   * @return the lines in string
   * @throws Exception the exception
   */
  public static String getLinesInString(Position p) throws Exception {
    List<String> lines = getLines(p);
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
}
