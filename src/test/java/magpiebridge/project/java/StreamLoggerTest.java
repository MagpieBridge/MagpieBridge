package magpiebridge.project.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import magpiebridge.core.StreamLogger;
import org.junit.Test;

public class StreamLoggerTest {

  @Test
  public void testStreamLogger() throws IOException {
    StreamLogger logger = new StreamLogger();
    OutputStream os = logger.log(System.out);

    PrintStream ps = new PrintStream(os);

    BufferedReader in = new BufferedReader(new InputStreamReader(logger.log(System.in)));
    while (true) {
      in.readLine();
      ps.println("Testline test test");
    }
  }
}
