package magpiebridge.core;

import com.google.gson.JsonObject;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

/**
 * Default {@link ConfusionHandler} supported by the {@link MagpieServer}.
 *
 * @author Linghui Luo
 */
public class DefaultConfusionHandler extends ConfusionHandler {

  @Override
  public void recordConfusion(String uri, JsonObject diagnostic) {
    server.forwardMessageToClient(
        new MessageParams(MessageType.Info, "Thank you for your feedback!"));
  }
}
