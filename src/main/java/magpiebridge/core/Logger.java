package magpiebridge.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;

public class Logger {
  private File file;
  private BufferedWriter writer;

  public Logger() {
    try {
      file = File.createTempFile("logger", ".txt");
      writer = new BufferedWriter(new FileWriter(file));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void log(boolean isServer, String msg) {
    StringBuilder sb = new StringBuilder();
    sb.append("LOG-" + LocalTime.now());
    if (isServer) {
      sb.append(": Server sends ");
    } else {
      sb.append(": Client sends");
    }
    sb.append("\n");
    sb.append(msg);
    try {
      writer.write(sb.toString());
      writer.newLine();
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      assert false : e;
    }
  }

  public void logServerMsg(String msg) {
    log(true, msg);
  }

  public void logClientMsg(String msg) {
    log(false, msg);
  }

  public void logVerbose(String msg) {
    try {
      writer.write("VERBOSE-:" + msg);
      writer.newLine();
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      assert false : e;
    }
  }
}
