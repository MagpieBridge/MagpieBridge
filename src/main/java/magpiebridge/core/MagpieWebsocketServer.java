package magpiebridge.core;

import java.util.function.Supplier;

import org.eclipse.lsp4j.launch.websockets.LSPWebSocketServer;

public abstract class MagpieWebsocketServer extends LSPWebSocketServer<MagpieServer> {

	public MagpieWebsocketServer(Supplier<MagpieServer> newServer) {
		super(newServer, MagpieServer.class);
	}


}
