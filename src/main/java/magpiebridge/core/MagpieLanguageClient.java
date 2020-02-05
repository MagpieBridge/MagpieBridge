package magpiebridge.core;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageClient;

public interface MagpieLanguageClient extends LanguageClient {

  @JsonNotification("magpie/showHTML")
  void showHTML(MessageParams messageParams);
}
