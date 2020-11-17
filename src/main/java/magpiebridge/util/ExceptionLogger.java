package magpiebridge.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import magpiebridge.core.MagpieServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.slf4j.LoggerFactory;

/**
 * Logs exceptions happened on the server. The logs (named with magpie_exceptions_*.log) are stored
 * in the temporary directory used by the JVM.
 *
 * @author Linghui Luo
 */
public class ExceptionLogger {
  private PrintWriter writer;
  private FileOutputStream logStream;
  private File log;
  private MagpieServer server;
  private boolean debug;

  public ExceptionLogger(MagpieServer server) {
    this.server = server;
    String tempDir = System.getProperty("java.io.tmpdir");
    String seperator = System.getProperty("file.separator");
    if (tempDir == null) {
      LoggerFactory.getLogger(getClass()).warn("System property java.io.tmpdir doesn't exist");
    } else if (!tempDir.endsWith(seperator)) {
      tempDir += seperator;
    }
    String suffix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log";
    log = new File(tempDir + "magpie_exceptions_" + suffix);
    try {
      logStream = new FileOutputStream(log);
      writer = new PrintWriter(logStream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    debug = false;
  }

  public void log(Exception e) {
    String msg = e.toString() + ":\n" + ExceptionUtils.getStackTrace(e);
    if (server != null) server.forwardMessageToClient(new MessageParams(MessageType.Warning, msg));
    String timeStamp = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:SS]").format(new Date());
    writer.println(timeStamp + msg);
    if (debug) e.printStackTrace();
  }

  public void log(String msg) {
    if (server != null) server.forwardMessageToClient(new MessageParams(MessageType.Warning, msg));
    String timeStamp = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:SS]").format(new Date());
    writer.println(timeStamp + msg);
    if (debug) System.err.println(msg);
  }

  public void cleanUp() {
    try {
      if (logStream != null) logStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (writer != null) writer.close();
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }
}
