package magpiebridge.core;

import java.nio.file.Path;
import magpiebridge.projectservice.java.JavaProjectType;

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

  /**
   * Return the {@link JavaProjectType} of the project in the root path which is resolved by this
   * service.
   *
   * @return project type in the root path
   */
  public String getProjectType();
}
