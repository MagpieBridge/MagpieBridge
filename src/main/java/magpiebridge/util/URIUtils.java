package magpiebridge.util;

/** @author Linghui Luo */
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
}
