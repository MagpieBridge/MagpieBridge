package magpiebridge.projectservice.java;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class FiveColonTest {
    /**
     * Test for the project that contains a library with a classifier.
     * In our test case "DemoProjectForFiveColon, we have a library javafx.base with the classifier win, in this case mvn dependency:list
     * produces output with 5 colon format. Then the jar filename format is [artifcatid]-[version]-[classifier].jar
     * e.g. javafx-base-17-ea+8-win.jar
     */
    @Test
    public void test1() {
        Path root = Paths.get("src/test/resources/DemoProjectForFiveColon").toAbsolutePath();

        InferConfig inferConfig = new InferConfig(root);

        ArrayList<Path> libPath = new ArrayList<Path>(inferConfig.libraryClassPath());
        Collections.sort(libPath);

        System.out.println(libPath);
        assertTrue(libPath.get(0).toString().endsWith("javafx-base-17-ea+8-linux.jar"));
        assertTrue(libPath.get(1).toString().endsWith("javafx-base-17-ea+8-mac.jar"));
        assertTrue(libPath.get(2).toString().endsWith("javafx-base-17-ea+8-win.jar"));
    }
}
