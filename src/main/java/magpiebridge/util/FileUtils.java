package magpiebridge.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class FileUtils {

  /**
   * Search files with given extension in the given directory.
   *
   * @param directory
   * @param extension
   * @return all file paths with the given extension
   * @throws IOException
   */
  public static Set<Path> collectFilePathWithExtension(Path directory, String extension)
      throws IOException {
    return Files.walk(directory)
        .filter(
            file -> StringUtils.endsWith(file.toString().toLowerCase(), extension.toLowerCase()))
        .collect(Collectors.toSet());
  }
}
