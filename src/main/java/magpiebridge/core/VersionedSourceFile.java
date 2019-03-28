package magpiebridge.core;

/**
 * This class represents a versioned source file.
 *
 * @author Linghui Luo
 */
public class VersionedSourceFile {
  private final String text;
  private final int version;

  public VersionedSourceFile(String text, int version) {
    this.text = text;
    this.version = version;
  }

  public String getText() {
    return text;
  }

  public int getVersion() {
    return version;
  }
}
