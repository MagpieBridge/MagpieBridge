package magpiebridge.projectservice.java;

import java.util.Objects;
/**
 * 
 * 
 * @author George Fraser
 * @see https://github.com/georgewfraser/java-language-server.git
 * 
 *  Modified by Linghui Luo 18.02.2019
 */
public class Artifact {
    protected final String groupId, artifactId, version;

    public Artifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public static Artifact parse(String id) {
        String[] parts = id.split(":");
        if (parts.length == 3) return new Artifact(parts[0], parts[1], parts[2]);
        else if (parts.length == 5) return new Artifact(parts[0], parts[1], parts[3]);
        else throw new IllegalArgumentException(id + " is not properly formatted artifact");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return Objects.equals(groupId, artifact.groupId)
                && Objects.equals(artifactId, artifact.artifactId)
                && Objects.equals(version, artifact.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }
}
