package magpiebridge.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link LanguageExtensionHandler} supported by {@link MagpieServer}.
 *
 * @author Julian Thome
 */
public class DefaultLanguageExtensionHandler implements LanguageExtensionHandler {
  @Override
  public String getLanguageForExtension(String extension) {
    switch (extension) {
      case ".java":
        return "java";
      case ".py":
        return "python";
      case ".js":
        return "javascript";
      case ".ts":
        return "typescript";
      case ".cpp":
      case ".h":
        return "c++";
      case ".c":
        return "c";
      case ".ll":
        return "ll";
      case ".go":
        return "go";
      default:
        MagpieServer.ExceptionLogger.log("Couldn't infer the language for extension " + extension);
        return null;
    }
  }

  @Override
  public Set<String> getExtensionsForLanguage(String language) {
    switch (language) {
      case "java":
        return new HashSet(Arrays.asList(new String[] {".java"}));
      case "python":
        return new HashSet(Arrays.asList(new String[] {".py"}));
      case "javascript":
        return new HashSet(Arrays.asList(new String[] {".js"}));
      case "typescript":
        return new HashSet(Arrays.asList(new String[] {".typescript"}));
      case "c++":
        return new HashSet(Arrays.asList(new String[] {".cpp", ".h"}));
      case "c":
        return new HashSet(Arrays.asList(new String[] {".c"}));
      case "ll":
        return new HashSet(Arrays.asList(new String[] {".ll"}));
      case "go":
        return new HashSet(Arrays.asList(new String[] {".go"}));
      default:
        MagpieServer.ExceptionLogger.log(
            "Couldn't infer the extensions for the language " + language);
        return new HashSet();
    }
  }
}
