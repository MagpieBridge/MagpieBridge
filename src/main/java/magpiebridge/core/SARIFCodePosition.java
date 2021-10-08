package magpiebridge.core.analysis.configuration;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/** This class defines the code position from SARIF file */
public class SARIFCodePosition implements Position {

  private int firstLine;
  private int lastLine;
  private int firstCol;
  private int lastCol;
  private URL url;

  public SARIFCodePosition(int firstLine, int firstCol, int lastLine, int lastCol, URL url) {
    this.firstLine = firstLine;
    this.firstCol = firstCol;
    this.lastLine = lastLine;
    this.lastCol = lastCol;
    this.url = url;
  }

  @Override
  public int getFirstLine() {
    return this.firstLine;
  }

  @Override
  public int getLastLine() {
    return this.lastLine;
  }

  @Override
  public int getFirstCol() {
    return this.firstCol;
  }

  @Override
  public int getLastCol() {
    return this.lastCol;
  }

  @Override
  public int getFirstOffset() {
    return 0;
  }

  @Override
  public int getLastOffset() {
    return 0;
  }

  @Override
  public int compareTo(SourcePosition arg0) {
    return 0;
  }

  @Override
  public URL getURL() {
    return this.url;
  }

  @Override
  public Reader getReader() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public String toString() {
    return "First Line: "
        + firstLine
        + ", First Col :"
        + firstCol
        + ", Last Line :"
        + lastLine
        + ", Last Col :"
        + lastCol
        + ", url : "
        + url.toString();
  }
}
