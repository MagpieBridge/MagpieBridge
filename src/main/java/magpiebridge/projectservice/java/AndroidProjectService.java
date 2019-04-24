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
      for (File f : apkDir.listFiles()) {
        if (f.isFile() && f.getName().endsWith(".apk")) {
          apkPath = Optional.of(Paths.get(f.getAbsolutePath()));
          break;
        }
      }
    }

    return this.apkPath;
  }
}
