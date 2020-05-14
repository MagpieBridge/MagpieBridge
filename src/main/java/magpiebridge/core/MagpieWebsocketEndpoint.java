package magpiebridge.core;

import java.util.Collection;
import java.util.concurrent.Executors;
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder;
import org.eclipse.lsp4j.websocket.WebSocketEndpoint;

public class MagpieWebsocketEndpoint extends WebSocketEndpoint<MagpieLanguageClient> {
  private final MagpieServer server;

  public MagpieWebsocketEndpoint(MagpieServer server) {
    this.server = server;
  }

  @Override
  protected void configure(Builder<MagpieLanguageClient> builder) {
    builder
        .setLocalService(server)
        .setRemoteInterface(MagpieLanguageClient.class)
        .setExecutorService(Executors.newCachedThreadPool());
  }

  @Override
  protected void connect(Collection<Object> localServices, MagpieLanguageClient remoteProxy) {
    super.connect(localServices, remoteProxy);
    server.connect(remoteProxy);
  }
}
