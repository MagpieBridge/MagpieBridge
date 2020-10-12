package magpiebridge.core;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
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
}
