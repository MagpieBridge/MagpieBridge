package magpiebridge.projectservice.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerConfiguration;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.Test;

public class MagpieServerTest {
  @Test
  public void testProjectService() {
    MagpieServer server = new MagpieServer(new ServerConfiguration());
    String lang = "java";
    server.addProjectService(lang, new JavaProjectService());
    assertTrue(server.getProjectService(lang).isPresent());
    InitializeParams param = new InitializeParams();
    param.setRootUri(
        Paths.get("src/test/resources/CogniCryptDemoExample/").toAbsolutePath().toUri().toString());
    server.initialize(param);
    server.initialized(null);
    DidOpenTextDocumentParams p = new DidOpenTextDocumentParams();
    TextDocumentItem i = new TextDocumentItem();
    i.setUri(
        "file:///E:/MagpieBridge/src/test/resources/CogniCryptDemoExample/src/main/java/example/ConstraintErrorExample.java");
    i.setLanguageId("java");
    i.setText(
        "package example;\\n\\nimport java.security.NoSuchAlgorithmException;\\n\\nimport javax.crypto.Cipher;\\nimport javax.crypto.NoSuchPaddingException;\\n\\n/**\\n * This code contains a misuse example CogniCrypt_SAST of a Cipher object. \\n * CogniCrypt_SAST reports that the string argument to Cipher.getInstance(\\\"AES/ECB/PKCS5Padding\\\") does not correspond the CrySL specification. \\n *\\n */\\npublic class ConstraintErrorExample {\\n\\tpublic static void main(String...args) throws NoSuchAlgorithmException, NoSuchPaddingException {\\n\\t\\tCipher instance = Cipher.getInstance(\\\"AES/ECB/PKCS5Padding\\\");\\n\\t}\\n}\\n\"}}");
    p.setTextDocument(i);
    server.getTextDocumentService().didOpen(p);
    JavaProjectService service = (JavaProjectService) server.getProjectService(lang).get();
    Path expected =
        Paths.get("src/test/resources/CogniCryptDemoExample/src/main/java").toAbsolutePath();
    assertEquals(expected, service.getSourcePath().iterator().next());
  }
}
