package magpiebridge.util;

import java.util.function.Function;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;

/**
 * MapgieMessageLogger allows you to log messages between server and client in format you want to
 * define.
 *
 * @author Linghui Luo
 */
public interface MagpieMessageLogger {
  public Function<MessageConsumer, MessageConsumer> getWrapper();

  public void cleanUp();
}
