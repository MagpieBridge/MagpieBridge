package magpiebridge.core;

import com.google.common.collect.ImmutableSet;

import java.util.*;

/**
 * User-configurable {@link LanguageExtensionHandler} supported by {@link MagpieServer}.
 *
 * @author Julian Thome & Juhan Oskar Hennoste
 */
public class ConfigurableLanguageExtensionHandler implements LanguageExtensionHandler {
  private final Map<String, Set<String>> languageToExtensions;
  private final Map<String, String> extensionToLanguage;

  private ConfigurableLanguageExtensionHandler() {
    this.languageToExtensions = new HashMap<>();
    this.extensionToLanguage = new HashMap<>();
  }

  public static ConfigurableLanguageExtensionHandler empty() {
    return new ConfigurableLanguageExtensionHandler();
  }

  public static ConfigurableLanguageExtensionHandler withDefaultMappings() {
    return new ConfigurableLanguageExtensionHandler()
            .setMapping("java", new String[]{".java"})
            .setMapping("python", new String[]{".py", ".ipynb"})
            .setMapping("javascript", new String[]{".js"})
            .setMapping("typescript", new String[]{".ts"})
            .setMapping("c++", new String[]{".cpp", ".h", ".hpp"})
            .setMapping("c", new String[]{".c"})
            .setMapping("ll", new String[]{".ll"})
            .setMapping("go", new String[]{".go"});
  }

  /**
   * Sets a mapping from a language to one or more file extensions. If a mapping already exists for this language it will be replaced.
   * Mapping the same file extension to multiple languages is not allowed.
   *
   * @param language   the analysis language (usually the language name in lowercase)
   * @param extensions the set of extensions that are associated with this language (extensions should start with a .)
   * @return the language extension handler
   * @throws IllegalArgumentException if one of the extensions doesn't start with a . or is already associated with another language
   */
  public ConfigurableLanguageExtensionHandler setMapping(String language, String[] extensions) {
    // Implementation note:
    // Some downstream uses of getExtensionsForLanguage treat the first extension as the primary extension for this language.
    // Because of this it is important to preserve the order of extensions.
    // This is why extensions are accepted as an array and are converted into an ImmutableSet, which is guaranteed to preserve order.

    for (String extension : extensions) {
      String existingLanguage = extensionToLanguage.get(extension);
      if (existingLanguage != null && !existingLanguage.equals(language)) {
        throw new IllegalArgumentException("Attempt to map extension '" + extension + "' to language '" + language + "', but it is already mapped to language '" + existingLanguage + "'");
      }
    }

    Set<String> existingExtensions = languageToExtensions.remove(language);
    if (existingExtensions != null) {
      for (String extension : existingExtensions) {
        extensionToLanguage.remove(extension);
      }
    }

    if (extensions.length > 0) {
      languageToExtensions.put(language, ImmutableSet.copyOf(extensions));
      for (String extension : extensions) {
        extensionToLanguage.put(extension, language);
      }
    }

    return this;
  }

  @Override
  public String getLanguageForExtension(String extension) {
    String language = extensionToLanguage.get(extension);
    if (language == null) {
      MagpieServer.ExceptionLogger.log("Couldn't infer the language for extension " + extension);
      return null;
    }
    return language;
  }

  @Override
  public Set<String> getExtensionsForLanguage(String language) {
    Set<String> extensions = languageToExtensions.get(language);
    if (extensions == null) {
      MagpieServer.ExceptionLogger.log("Couldn't infer the extensions for the language " + language);
      return ImmutableSet.of();
    }
    return extensions;
  }
}
