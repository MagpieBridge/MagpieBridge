package magpiebridge.projectservice.java;

import static org.junit.Assert.assertEquals;

import com.ibm.wala.classLoader.SourceFileModule;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import magpiebridge.file.SourceFileManager;
import magpiebridge.file.VersionedSourceFile;
import org.junit.Test;

public class SourceFileManagerTest {

  @Test
  public void testGenerateSourceFileModule() {
    SourceFileManager m = new SourceFileManager("java", new HashMap<>());
    String uri =
        "file:///E:/MagpieBridge/src/test/resources/CogniCryptDemoExample/src/main/java/example/ConstraintErrorExample.java";
    String text =
        "package example;\\n\\nimport java.security.NoSuchAlgorithmException;\\n\\nimport javax.crypto.Cipher;\\nimport javax.crypto.NoSuchPaddingException;\\n\\n/**\\n * This code contains a misuse example CogniCrypt_SAST of a Cipher object. \\n * CogniCrypt_SAST reports that the string argument to Cipher.getInstance(\\\"AES/ECB/PKCS5Padding\\\") does not correspond the CrySL specification. \\n *\\n */\\npublic class ConstraintErrorExample {\\n\\tpublic static void main(String...args) throws NoSuchAlgorithmException, NoSuchPaddingException {\\n\\t\\tCipher instance = Cipher.getInstance(\\\"AES/ECB/PKCS5Padding\\\");\\n\\t}\\n}\\n\"}}";
    VersionedSourceFile f1 = new VersionedSourceFile(text, 0);
    m.generateSourceFileModule(URI.create(uri), f1);
    Map<URI, SourceFileModule> modules = m.getSourceFileModules();
    assertEquals(1, modules.size());
    VersionedSourceFile f2 = new VersionedSourceFile(text, 1);
    m.generateSourceFileModule(URI.create(uri), f2);
    modules = m.getSourceFileModules();
    assertEquals(1, modules.size());
  }

  @Test
  public void test() {
    try {
      String old =
          "file:///e%3A/Git/Github/magpie/crypto-lsp-demo/src/test/resources/DemoAllErrors/src/Demo.java";
      String url = URLDecoder.decode(old, "UTF-8");
      System.out.println(old);
      System.out.println(url);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
