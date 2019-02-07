package magpiebridge.core;

import java.util.function.Supplier;

import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.lsp4j.launch.websockets.LSPWebSocketServer;

@ServerEndpoint("/websocket")
public class MagpieWebSocketServer extends LSPWebSocketServer<MagpieServer>{

	public MagpieWebSocketServer(Supplier<MagpieServer> newServer, Class<MagpieServer> serverClass) {
			super(() -> { 
				return new MagpieServer(); }, MagpieServer.class);
	}
	
    @Override
	@OnError
    public void onError(Throwable e, Session session){
        e.printStackTrace();
    }
}
