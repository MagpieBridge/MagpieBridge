package magpiebridge.core;

import java.util.Set;

/**
 * Map languages to extensions and vice versa.
 *
 * @author Julian Thome
 */
public interface LanguageExtensionHandler {
  /**
   * Infer a language based on a file extension
   * @param extension file extension (starting with a '.')
   * @return The language that corresponds to the extension
   */
  String getLanguageForExtension(String extension);

  /**
   * Infer a set of extensions that corresponds to a language
   * @param language name of the language
   * @return Set of extensions (starting with '.') that correspond to the language
   */
  Set<String> getExtensionsForLanguage(String language);
}
