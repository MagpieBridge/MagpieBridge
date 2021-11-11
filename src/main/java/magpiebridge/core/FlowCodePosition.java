package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/** This class defines the code position from SARIF file */
public class FlowCodePosition implements Position {

  private int firstLine;
  private int lastLine;
  private int firstCol;
  private int lastCol;
  private String methodName;
  private URL url;

  public FlowCodePosition(
      int firstLine, int firstCol, int lastLine, int lastCol, URL url, String methodName) {
    this.firstLine = firstLine;
    this.firstCol = firstCol;
    this.lastLine = lastLine;
    this.lastCol = lastCol;
    this.url = url;
    this.methodName = methodName;
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

  public String getMehodName() {
    return this.methodName;
  }

  @Override
  public Reader getReader() throws IOException {
    return null;
  }

  public String toString() {
    return "First Line: "
        + firstLine
        + ", First Col : "
        + firstCol
        + ", Last Line : "
        + lastLine
        + ", Last Col : "
        + lastCol
        + ", url : "
        + url.toString()
        + ", methodName : "
        + methodName;
  }
}
