/*
 * @author Linghui Luo
 */
package magpiebridge.util;

import org.eclipse.lsp4j.Range;

/** The Class SourceCodeInfo. */
public class SourceCodeInfo {
  public Range range; // the range of the code in the file
  public String code; // line of source code

  public SourceCodeInfo(Range range, String code) {
    this.range = range;
    this.code = code;
  }

  public SourceCodeInfo() {}
}
