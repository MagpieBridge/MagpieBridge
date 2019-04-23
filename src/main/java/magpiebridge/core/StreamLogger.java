package magpiebridge.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;

/** @author Linghui Luo */
public class StreamLogger {
  private Logger IN_LOGGER = Logger.getLogger("LOG");
  private Logger OUT_LOGGER = Logger.getLogger("LOG");

  public StreamLogger() {
    try {
      File temp = File.createTempFile("magpie", ".log");
      FileHandler fh = null;
      try {
        fh = new FileHandler(temp.getAbsolutePath(), true);
      } catch (Exception e) {
        e.printStackTrace();
      }
      fh.setFormatter(new SimpleFormatter());
      fh.setLevel(Level.INFO);
      IN_LOGGER.addHandler(fh);
      OUT_LOGGER.addHandler(fh);
      IN_LOGGER.setLevel(Level.INFO);
      OUT_LOGGER.setLevel(Level.INFO);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Log stream.
   *
   * @param is the is
   * @return the input stream
   */
  public InputStream log(InputStream is) {
    try {
      PipedInputStream pis = new PipedInputStream();
      PipedOutputStream pos = new PipedOutputStream(pis);
      Thread readerThread =
          new Thread() {
            @Override
            public void run() {
              BufferedReader reader = new BufferedReader(new InputStreamReader(pis));
              String line = null;
              try {
                while ((line = reader.readLine()) != null) {
                  if (!line.trim().isEmpty()) {
                    IN_LOGGER.log(Level.INFO, "\n" + line + "\n");
                  }
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          };
      readerThread.start();
      return new TeeInputStream(is, pos);
    } catch (IOException e) {
      return is;
    }
  }

  /**
   * Log stream.
   *
   * @param os the os
   * @return the output stream
   */
  public OutputStream log(OutputStream os) {
    try {
      PipedInputStream pis = new PipedInputStream();
      PipedOutputStream pos = new PipedOutputStream(pis);
      Thread readerThread =
          new Thread() {
            @Override
            public void run() {
              BufferedReader reader = new BufferedReader(new InputStreamReader(pis));
              String line = null;
              try {
                while ((line = reader.readLine()) != null) {
                  if (!line.trim().isEmpty()) {
                    OUT_LOGGER.log(Level.INFO, "\n" + line + "\n");
                  }
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          };
      readerThread.start();
      return new TeeOutputStream(os, pos);
    } catch (IOException e) {
      return os;
    }
  }
}
