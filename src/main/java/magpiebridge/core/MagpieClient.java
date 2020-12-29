package magpiebridge.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Customized Language Client interface which extends LSP with new notifications.
 *
 * @author Linghui Luo
 * @author Julian Dolby
 */
public interface MagpieClient extends LanguageClient {

  @JsonNotification("magpiebridge/showHTML")
  void showHTML(String content);

  @JsonRequest("magpiebridge/showInputBox")
  CompletableFuture<Map<String, String>> showInputBox(List<String> messages);
}
