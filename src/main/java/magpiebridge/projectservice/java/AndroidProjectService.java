/*
 * @author Linghui Luo
 */
package magpiebridge.projectservice.java;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/** The Class AndroidProjectService. */
public class AndroidProjectService extends JavaProjectService {

  /** The apk path. */
  private Optional<Path> apkPath;

  /** Instantiates a new android project service. */
  public AndroidProjectService() {
    super();
    this.apkPath = Optional.empty();
  }

  /**
   * Gets the apk path.
   *
   * @return the apk path
   */
  public Optional<Path> getApkPath() {
    if (this.getRootPath().isPresent()) {
      String root = this.getRootPath().get().toString();
      // TODO consider customized build path
      File apkDir = Paths.get(root, "app/build/outputs/apk/debug").toFile();
      Optional<File> file = searchAPKFile(apkDir);
      if (file.isPresent()) apkPath = Optional.of(Paths.get(file.get().getAbsolutePath()));
    }
    return this.apkPath;
  }

  /**
   * Search an apk file in the given directory .
   *
   * @param dir directory where to search apk
   * @return the apk file
   */
  private Optional<File> searchAPKFile(File dir) {
    if (dir.isFile() && dir.getName().endsWith(".apk")) {
      return Optional.of(dir);
    } else {
      if (dir.isDirectory()) {
        for (File f : dir.listFiles()) {
          Optional<File> op = searchAPKFile(f);
          if (op.isPresent()) return op;
        }
      }
      return Optional.empty();
    }
  }
}
