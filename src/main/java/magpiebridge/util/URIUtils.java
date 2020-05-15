package magpiebridge.util;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Utility class for handling URI.
 *
 * @author Linghui Luo
 */
public class URIUtils {
  /**
   * Check if URI is in the right format for the operating system.
   *
   * @param uri the uri
   * @return the string
   */
  public static String checkURI(String uri) {
    if (uri.startsWith("file")) {
      if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
        // take care of uri in windows
        if (!uri.startsWith("file:///")) {
          uri = uri.replace("file://", "file:///");
        }
        if (!uri.startsWith("file:///")) uri = uri.replace("file:/", "file:///");
      } else {
        if (!uri.startsWith("file://")) {
          String[] splits = uri.split(":");
          if (splits.length == 2) {
            String path = splits[1];
            if (path.matches("^(/[^/ ]*)+/?$")) {
              uri = "file://" + path;
            }
          }
        }
      }
    }
    return uri;
  }

  /**
   * Replace URL.
   *
   * @param pos the pos
   * @param url the url
   * @return the position
   */
  public static Position replaceURL(Position pos, URL url) {
    return new AbstractSourcePosition() {

      @Override
      public int getLastOffset() {
        return pos.getLastOffset();
      }

      @Override
      public int getLastLine() {
        return pos.getLastLine();
      }

      @Override
      public int getLastCol() {
        return pos.getLastCol();
      }

      @Override
      public int getFirstOffset() {
        return pos.getFirstOffset();
      }

      @Override
      public int getFirstLine() {
        return pos.getFirstLine();
      }

      @Override
      public int getFirstCol() {
        return pos.getFirstCol();
      }

      @Override
      public URL getURL() {
        return url;
      }

      @Override
      public Reader getReader() throws IOException {
        return new InputStreamReader(url.openConnection().getInputStream());
      }

      @Override
      public String toString() {
        return url + ":" + getFirstLine() + "," + getFirstCol();
      }
    };
  }
}
