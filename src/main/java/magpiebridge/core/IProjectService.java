package magpiebridge.core;

import java.nio.file.Path;
import magpiebridge.projectservice.java.JavaProjectService;

/**
 * The Interface IProjectService defines service which resolves project scope. An implementation of
 * this interface should resolve project path information such as source code path and library code
 * path from the project rootPath. See an implementation in {@link JavaProjectService}.
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
   * Return the project type (e.g. Maven, Gradle, Eclipse) in the root path which is resolved by
   * this service. See example in {@link JavaProjectService}.
   *
   * @return project type in the root path
   */
  public String getProjectType();
}
