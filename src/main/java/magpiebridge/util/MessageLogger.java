package magpiebridge.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.validation.ReflectiveMessageValidator;

/**
 * MessageLogger logs the incoming and outgoing message with time stamp. The logs are stored in the
 * temporary directory used by the JVM.
 *
 * @author Linghui Luo
 */
public final class MessageLogger implements MagpieMessageLogger {
  private PrintWriter writer;
  private FileOutputStream logStream;
  private File log;

  public MessageLogger() {
    String tempDir = System.getProperty("java.io.tmpdir");
    String seperator = System.getProperty("file.separator");
    if (!tempDir.endsWith(seperator)) {
      tempDir += seperator;
    }
    String suffix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log";
    log = new File(tempDir + "magpie_trace_" + suffix);
    try {
      logStream = new FileOutputStream(log);
      writer = new PrintWriter(logStream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the wrapper which logs the incoming and outgoing messages with time stamp and validates
   * the messages.
   *
   * @return the wrapper
   */
  public Function<MessageConsumer, MessageConsumer> getWrapper() {
    Function<MessageConsumer, MessageConsumer> wrapper =
        (MessageConsumer c) -> {
          MessageConsumer wrappedConsumer =
              message -> {
                String timeStamp =
                    new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:SS]").format(new Date());
                writer.println(timeStamp + message);
                writer.flush();
                new ReflectiveMessageValidator(c).consume(message);
              };
          return wrappedConsumer;
        };
    return wrapper;
  }

  public void cleanUp() {
    try {
      if (logStream != null) logStream.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (writer != null) writer.close();
  }
}
