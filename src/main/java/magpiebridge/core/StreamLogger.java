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
  private Logger LOGGER_IN = Logger.getLogger("IN");
  private Logger LOGGER_OUT = Logger.getLogger("OUT");

  public StreamLogger() {
    try {
      // The SimpleFormatter uses String.format(format, date, source, logger, level, message,
      // thrown);
      System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %5$s %n");
      File temp1 = File.createTempFile("magpie_in", ".log");
      File temp2 = File.createTempFile("magpie_out", ".log");
      FileHandler fh1 = null;
      FileHandler fh2 = null;
      try {
        fh1 = new FileHandler(temp1.getAbsolutePath(), true);
        fh2 = new FileHandler(temp2.getAbsolutePath(), true);
      } catch (Exception e) {
        e.printStackTrace();
      }
      fh1.setFormatter(new SimpleFormatter());
      LOGGER_IN.addHandler(fh1);
      LOGGER_IN.setLevel(Level.INFO);

      fh2.setFormatter(new SimpleFormatter());
      LOGGER_OUT.addHandler(fh2);
      LOGGER_OUT.setLevel(Level.INFO);
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
                    LOGGER_IN.log(Level.INFO, "[IN ] " + line);
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
                    LOGGER_OUT.log(Level.INFO, "[OUT] " + line);
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
