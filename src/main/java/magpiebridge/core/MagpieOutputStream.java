package magpiebridge.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MagpieOutputStream extends OutputStream {

  private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

  @Override
  public void write(int b) throws IOException {
    this.bytes.write(b);
  }
}
