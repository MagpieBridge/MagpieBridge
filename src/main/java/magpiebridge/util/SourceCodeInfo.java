/*
 * @author Linghui Luo
 */
package magpiebridge.util;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import org.eclipse.lsp4j.Range;

/**
 * The class SourceCodeInfo store information about a line of code.
 *
 * @author Linghui Luo
 */
public class SourceCodeInfo {
  public Range range; // the range of the code in the file
  public String code; // line of source code
  public URL url;

  public SourceCodeInfo(Range range, String code) {
    this.range = range;
    this.code = code;
  }

  public SourceCodeInfo(URL url, Range range, String code) {
    this.url = url;
    this.range = range;
    this.code = code;
  }

  public SourceCodeInfo() {}

  public Position toPosition() {
    if (this.url != null && range != null && code != null) {
      return new Position() {

        @Override
        public int getFirstLine() {
          return range.getStart().getLine() + 1;
        }

        @Override
        public int getLastLine() {
          return range.getEnd().getLine() + 1;
        }

        @Override
        public int getFirstCol() {
          return range.getStart().getCharacter();
        }

        @Override
        public int getLastCol() {
          return range.getEnd().getCharacter();
        }

        @Override
        public int getFirstOffset() {
          return 0;
        }

        @Override
        public int getLastOffset() {
          return 0;
        }

        @Override
        public int compareTo(SourcePosition o) {
          return 0;
        }

        @Override
        public URL getURL() {
          return url;
        }

        @Override
        public Reader getReader() throws IOException {
          return new InputStreamReader(url.openConnection().getInputStream());
        }
      };
    } else return null;
  }
}
