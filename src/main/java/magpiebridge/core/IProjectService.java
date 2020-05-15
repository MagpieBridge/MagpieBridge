package magpiebridge.core;

import java.nio.file.Path;

/**
 * The Interface IProjectService defines service which resolves project scope. An implementation of
 * this interface should resolve project path information such as source code path and library code
 * path from the project rootPath.
 *
 * @author Linghui Luo
 */
public interface IProjectService {

  /**
   * Sets the root path.
   *
   * @param rootPath the new root path
   */
  public void setRootPath(Path rootPath);

  public ProjectType getProjectType();
}
