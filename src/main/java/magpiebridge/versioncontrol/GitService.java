package magpiebridge.versioncontrol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import magpiebridge.core.VersionControlService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  This class performs git commands
 *   @author Linghui Luo
 */
public class GitService implements VersionControlService {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private Path rootPath;
  private Repository repository;
  private Git git;

  @Override
  public void setRootPath(Path rootPath) {
    this.rootPath = rootPath;
    File gitDir = new File(rootPath.toAbsolutePath() + File.separator + ".git");
    try {
      if (gitDir.exists()) {
        this.repository = new FileRepositoryBuilder().setGitDir(gitDir).build();
        this.git = new Git(repository);
      }
    } catch (IOException e) {
      logger.error(gitDir + " doesn't exist.");
      e.printStackTrace();
    }
  }

  public List<URIish> getRemoteHosts() throws GitAPIException {
    List<URIish> hosts = new ArrayList<>();
    List<RemoteConfig> remoteConfigList = git.remoteList().call();
    remoteConfigList.forEach(
        e ->
            e.getURIs()
                .forEach(
                    uri -> {
                      hosts.add(uri);
                    }));

    return hosts;
  }

  public URIish getOriginRemoteHost() throws GitAPIException {
    AtomicReference<URIish> ret = new AtomicReference<>();
    List<RemoteConfig> remoteConfigList = git.remoteList().call();
    remoteConfigList.forEach(
        e ->
            e.getURIs()
                .forEach(
                    uri -> {
                      if ("origin".equals(e.getName())) ret.set(uri);
                    }));

    return ret.get();
  }

  public Optional<RevCommit> getHeadCommit() throws GitAPIException, IOException {
    RevCommit mostRecentCommit = null;
    ObjectId head = git.getRepository().resolve("HEAD");
    if (head != null) {

      try (RevWalk walk = new RevWalk(git.getRepository())) {
        mostRecentCommit = walk.parseCommit(head);
      }
    }
    return Optional.ofNullable(mostRecentCommit);
  }

  public Optional<RevCommit> getClosestCommitBefore(Instant time) throws GitAPIException {
    Date date = Date.from(time);
    RevCommit commit = null;
    Iterable<RevCommit> logs = git.log().call();
    for (RevCommit log : logs) {
      int commitTime = log.getCommitTime();
      Date d = new Date(commitTime * 1000L);
      // d happened before date
      if (d.compareTo(date) < 0) {
        commit = log;
        break;
      }
    }
    return Optional.ofNullable(commit);
  }

  public IndexDiff getDiff(RevCommit commit) throws IOException {
    ObjectId commitId = commit.getId();
    WorkingTreeIterator workingTree = new FileTreeIterator(git.getRepository());
    IndexDiff diff = new IndexDiff(git.getRepository(), commitId, workingTree);
    diff.diff();
    return diff;
  }

  public String getBranch() throws IOException {
    return repository.getBranch();
  }

  public RepositoryState getRepositoryState() {
    return repository.getRepositoryState();
  }

  public Git getGit() {
    return git;
  }
}
