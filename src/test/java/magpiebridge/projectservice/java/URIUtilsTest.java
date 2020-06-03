package magpiebridge.projectservice.java;

import static org.junit.Assert.assertEquals;

import magpiebridge.util.URIUtils;
import org.junit.Test;

public class URIUtilsTest {

  @Test
  public void test() {
    String uriWithSpace =
        "file:///d:/Dropbox/UserStudy/tb-viewer/Running Example/src/main/java/Logger.java";
    String uriReplaced = URIUtils.checkURI(uriWithSpace);
    assertEquals(
        uriReplaced,
        "file:///d:/Dropbox/UserStudy/tb-viewer/Running%20Example/src/main/java/Logger.java");
  }
}
