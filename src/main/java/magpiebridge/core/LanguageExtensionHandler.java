package magpiebridge.core;

import java.util.Set;

public interface LanguageExtensionHandler {
    String getLanguageForExtension(String extension);
    Set<String> getExtensionsForLanguage(String language);
}
