package magpiebridge.util;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;
import com.ibm.wala.cast.tree.impl.LineNumberPosition;
import com.ibm.wala.cast.util.SourceBuffer;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

public class SourceCodePositionConverter {

  /**
   * Gets the location from given position.
   *
   * @param pos the pos
   * @return the location from
   */
  public static Location getLocationFrom(Position pos) {
    Location codeLocation = new Location();
    try {
      codeLocation.setUri(URIUtils.checkURI(pos.getURL().toURI().toString()));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    Range codeRange = new Range();
    Position detail = getPositionWithColumns(pos);
    codeRange.setEnd(getPositionFrom(detail.getLastLine(), detail.getLastCol()));
    codeRange.setStart(getPositionFrom(detail.getFirstLine(), detail.getFirstCol()));
    codeLocation.setRange(codeRange);
    return codeLocation;
  }

  /**
   * Gets the position from given line and column numbers.
   *
   * @param line the line
   * @param column the column
   * @return the position from
   */
  public static org.eclipse.lsp4j.Position getPositionFrom(int line, int column) {
    org.eclipse.lsp4j.Position codeStart = new org.eclipse.lsp4j.Position();
    codeStart.setLine(line - 1);
    codeStart.setCharacter(column);
    return codeStart;
  }

  public static Position getPositionWithColumns(Position p) {
    if (p.getFirstCol() >= 0) {
      return p;
    } else {
      Position firstLineP = new LineNumberPosition(p.getURL(), p.getURL(), p.getFirstLine());
      SourceBuffer firstLine = null;
      try {
        firstLine = new SourceBuffer(firstLineP);
      } catch (IOException e) {
        assert false : e;
      }
      String firstLineText = firstLine.toString();

      String pText = null;
      try {
        pText = new SourceBuffer(p).toString();
      } catch (IOException e) {
        assert false : e;
      }

      int lines = pText.split("\n").length;

      String pFirst = pText;
      if (pText.contains("\n")) {
        pFirst = pText.substring(0, pText.indexOf("\n"));
      }

      int endCol;
      int startCol = firstLineText.indexOf(pFirst);

      if (!pText.contains("\n")) {
        endCol = startCol + pText.length();
      } else {
        String lastLineText = pText.substring(pText.lastIndexOf("\n"), pText.length());
        endCol = lastLineText.length();
      }

      return new AbstractSourcePosition() {
        @Override
        public Reader getReader() throws IOException {
          return p.getReader();
        }

        @Override
        public URL getURL() {
          return p.getURL();
        }

        @Override
        public int getFirstCol() {
          return startCol;
        }

        @Override
        public int getFirstLine() {
          return p.getFirstLine();
        }

        @Override
        public int getFirstOffset() {
          return p.getFirstOffset();
        }

        @Override
        public int getLastCol() {
          return endCol;
        }

        @Override
        public int getLastLine() {
          if (p.getLastLine() >= 0) {
            return p.getLastLine();
          } else {
            return p.getFirstLine() + lines - 1;
          }
        }

        @Override
        public int getLastOffset() {
          return p.getLastOffset();
        }
      };
    }
  }
}
