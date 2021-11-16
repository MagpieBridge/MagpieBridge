package magpiebridge.core.analysis.configuration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.sarif.AnalysisResultToSARIFConverter;
import magpiebridge.core.sarif.SARIFToAnalysisResultConverter;
import org.junit.Test;

public class SARIFToAnalysisResultConverterTest {
  @Test
  public void test1() throws IOException {
    String path = Paths.get("src/test/resources/sarif-req.txt").toAbsolutePath().toString();
    JsonObject sarifJson = (JsonObject) new JsonParser().parse(new FileReader(new File(path)));
    SARIFToAnalysisResultConverter converter = new SARIFToAnalysisResultConverter(sarifJson);
    List<AnalysisResult> results = converter.getAnalysisResults();

    AnalysisResultToSARIFConverter c2 = new AnalysisResultToSARIFConverter(results.get(0));
    JsonObject sarifJson2 = c2.makeSarif();
  }
}
