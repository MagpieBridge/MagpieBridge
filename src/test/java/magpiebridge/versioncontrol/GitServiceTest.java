package magpiebridge.versioncontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.junit.Test;

public class GitServiceTest {
  @Test
  public void testGetRemoteHost() throws GitAPIException, IOException {
    GitService gitService = new GitService();
    gitService.setRootPath(Paths.get("").toAbsolutePath()); // the MagpieBridge project dir
    List<URIish> hosts = gitService.getRemoteHosts();
    assertEquals(1, hosts.size());
    assertEquals("github.com", hosts.get(0).getHost());
    assertEquals("MagpieBridge", hosts.get(0).getHumanishName());
    assertTrue(gitService.getOriginRemoteHost().equals(hosts.get(0)));
  }
}
