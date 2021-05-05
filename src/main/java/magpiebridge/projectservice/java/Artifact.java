package magpiebridge.projectservice.java;

import java.util.Objects;
import magpiebridge.core.MagpieServer;

/**
 * Code adapted from https://github.com/georgewfraser/java-language-server.git
 *
 * @author George Fraser
 * @author Linghui Luo
 */
public class Artifact {
  protected final String groupId, artifactId, version, classifier;

  public Artifact(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = "";
  }

  public Artifact(String groupId, String artifactId, String version, String classifier) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
  }

  public static Artifact parse(String id) {
    String[] parts = id.split(":");
    if (parts.length == 3) {
      return new Artifact(parts[0], parts[1], parts[2]);
    } else if (parts.length == 5) {
      return new Artifact(parts[0], parts[1], parts[3]);
    } else {
      MagpieServer.ExceptionLogger.log(
          new IllegalArgumentException(id + " is not properly formatted artifact"));
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Artifact artifact = (Artifact) o;
    return Objects.equals(groupId, artifact.groupId)
        && Objects.equals(artifactId, artifact.artifactId)
        && Objects.equals(version, artifact.version)
        && Objects.equals(classifier, artifact.classifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version, classifier);
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }
}
