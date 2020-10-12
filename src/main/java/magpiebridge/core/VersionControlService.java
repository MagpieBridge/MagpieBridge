package magpiebridge.core;

import java.nio.file.Path;

public interface VersionControlService {
  /**
   * Sets the root path.
   *
   * @param rootPath the new root path
   */
  public void setRootPath(Path rootPath);
}
