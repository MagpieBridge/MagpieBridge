package magpiebridge.util;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
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
   * @throws IOException happens by parsing file
   */
  public static List<String> getLines(Position p) throws IOException {
    return getLines(p, false);
  }

  /**
   * Gets the lines of code at the give position.
   *
   * @param p the position where to get the code
   * @param includeComment if comment should be removed.
   * @return the lines
   * @throws IOException happens by parsing file
   */
  public static List<String> getLines(Position p, boolean includeComment) throws IOException {
    List<String> lines = new ArrayList<>();

    File file = getFileWithPosition(p);
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
        if (p.getLastLine() == line) {
          // all code sits on one line; only add substring from first to last col
          lines.add(
              removeComment(
                  currentLine.substring(p.getFirstCol(), p.getLastCol()), includeComment));
        } else {
          // code spans multiple lines; add all the rest of current line after first col
          lines.add(removeComment(currentLine.substring(p.getFirstCol()), includeComment));
        }

        // keep iterating until last line is reached
        while (p.getLastLine() > line) {
          currentLine = reader.readLine();
          line++;
          if (p.getLastLine() == line && p.getLastCol() != -1) {
            lines.add(removeComment(currentLine.substring(0, p.getLastCol()), includeComment));
          } else {
            lines.add(removeComment(currentLine, includeComment));
          }
        }
      }
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
   * @throws IOException happens by parsing file
   */
  public static String getLinesInString(Position p, boolean includeComment) throws IOException {
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
   * @throws IOException happens by parsing file
   */
  public static String getLinesInString(Position p) throws IOException {
    return getLinesInString(p, false);
  }
  /**
   * Get the whole line of code.
   *
   * @param position the position where to get the code
   * @param includeComment if comment should be removed.
   * @return whole line as string
   * @throws IOException happens by parsing file
   */
  public static String getWholeCodeLineInString(Position position, boolean includeComment)
      throws IOException {
    String code = "";
    File file = getFileWithPosition(position);
    if (file.exists() && file.isFile()) {
      try (FileReader freader = new FileReader(file);
          BufferedReader reader = new BufferedReader(freader)) {

        String currentLine = null;
        int line = 0;
        do {
          currentLine = reader.readLine();
          if (currentLine == null) {
            return code;
          }
          line++;
        } while (position.getFirstLine() > line);

        code = removeComment(currentLine, includeComment);

      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return code;
  }
  /**
   * Get the file with the position
   *
   * @param position
   * @return
   * @throws MalformedURLException
   */
  public static File getFileWithPosition(Position position) throws MalformedURLException {
    String url = position.getURL().toString();
    if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
      // take care of url in windows
      if (!url.startsWith("file:///")) {
        url = url.replace("file://", "file:///");
      }
    }

    return new File(new URL(url).getFile());
  }
}
