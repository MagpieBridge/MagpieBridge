package magpiebridge.util;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class SourceCodePositionFinderTest {
  @Test
  public void testFindCode() {
    File file =
        new File(
            "src/test/resources/cryptoDemoTest/src/cryptoDemoTest/ConstraintErrorExample.java");
    int lineNumber = 15;
    String code = "Cipher instance = Cipher.getInstance(\"AES/ECB/PKCS5Padding\");";
    SourceCodeInfo info = SourceCodePositionFinder.findCode(file, lineNumber);
    Assert.assertEquals(code, info.code);
    Assert.assertEquals(info, SourceCodePositionFinder.findCode(file, lineNumber, "//").get());

    file = new File("src/test/resources/DemoProjectNpm/src/index.js");
    lineNumber = 10;
    code = "document.body.appendChild(component());";
    info = SourceCodePositionFinder.findCode(file, lineNumber, "//").get();
    Assert.assertEquals(code, info.code);

    file = new File("src/test/resources/helloworld.py");
    lineNumber = 1;
    code = "print(\"hello world\")";
    info = SourceCodePositionFinder.findCode(file, lineNumber, "#").get();
    Assert.assertEquals(code, info.code);
  }
}
