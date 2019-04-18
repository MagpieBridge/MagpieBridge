package magpiebridge.core;

import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.util.io.TemporaryFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

/**
 * This class manages all source files of a given language sent from client to server.
 *
 * @author Linghui Luo
 */
public class SourceFileManager {

  /** The language. */
  private String language;
  /** Client-side URI mapped to versioned source file. */
  private Map<URI, VersionedSourceFile> versionedFiles;
  /** Client-side URI mapped to source file module. */
  private Map<URI, Module> sourceFileModules;
  /** Server-side URI string mapped to client-side URI string. */
  private Map<String, String> serverClientUri;

  /**
   * Instantiates a new source file manager.
   *
   * @param language the language
   * @param serverClientUri the server client uri
   */
  public SourceFileManager(String language, Map<String, String> serverClientUri) {
    this.language = language;
    this.versionedFiles = new HashMap<>();
    this.sourceFileModules = new HashMap<>();
    this.serverClientUri = serverClientUri;
  }

  /**
   * Add the opened file to versionedFiles and generate source file module for it.
   *
   * @param params the params
   */
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem doc = params.getTextDocument();
    if (doc.getLanguageId().equals(language)) {
      String uri = doc.getUri();
      VersionedSourceFile sourceFile = new VersionedSourceFile(doc.getText(), doc.getVersion());
      URI clientUri = URI.create(uri);
      this.versionedFiles.put(clientUri, sourceFile);
      generateSourceFileModule(clientUri, sourceFile);
    }
  }

  /**
   * Update the changed file and generate source file module for updated file.
   *
   * @param params the params
   */
  public void didChange(DidChangeTextDocumentParams params) {
    VersionedTextDocumentIdentifier doc = params.getTextDocument();
    String uri = doc.getUri();
    VersionedSourceFile existFile = versionedFiles.get(URI.create(uri));
    int newVersion = doc.getVersion();
    if (newVersion > existFile.getVersion()) {
      String existText = existFile.getText();
      String newText = existText;
      for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
        if (change.getRange() == null) {
          // the nextText should be the full context of the file.
          newText = change.getText();
        } else {
          newText = replaceText(newText, change);
        }
      }
      VersionedSourceFile newFile = new VersionedSourceFile(newText, newVersion);
      URI clientUri = URI.create(uri);
      this.versionedFiles.put(clientUri, newFile);
      generateSourceFileModule(clientUri, newFile);
    }
  }

  /**
   * Replace the old text according to the change.
   *
   * @param text the text
   * @param change the change
   * @return the string
   */
  private String replaceText(String text, TextDocumentContentChangeEvent change) {
    try {
      Range range = change.getRange();
      BufferedReader reader = new BufferedReader(new StringReader(text));
      StringWriter writer = new StringWriter();
      int line = 0;
      while (line < range.getStart().getLine()) {
        writer.write(reader.readLine() + '\n');
        line++;
      }
      for (int character = 0; character < range.getStart().getCharacter(); character++)
        writer.write(reader.read());
      // write the changed text
      writer.write(change.getText());
      // skip the old text
      reader.skip(change.getRangeLength());
      int next = reader.read();
      while (next != -1) {
        writer.write(next);
        next = reader.read();
      }
      return writer.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Generate source file module from a versioned source file.
   *
   * @param clientUri the client uri
   * @param versionedFile the versioned file
   */
  public void generateSourceFileModule(URI clientUri, VersionedSourceFile versionedFile) {
    SourceFileModule sourceFile = null;
    try {
      File file = File.createTempFile("temp", getFileSuffix());
      file.deleteOnExit();
      String text = versionedFile.getText();
      TemporaryFile.stringToFile(file, text);
      String[] strs = clientUri.toString().split("/");
      String className = strs[strs.length - 1];
      sourceFile = new SourceFileModule(file, className, null);
      this.sourceFileModules.put(clientUri, sourceFile);
      URI serverUri = Paths.get(file.toURI()).toUri();
      // store the mapping from server-side URI to client-side URI.
      this.serverClientUri.put(serverUri.toString(), clientUri.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the file suffix.
   *
   * @return the file suffix
   */
  private String getFileSuffix() {
    if (language.equals("java")) {
      return ".java";
    } else if (language.equals("python")) {
      return ".py";
    } else if (language.equals("javascript")) {
      return ".js";
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Gets the versioned files.
   *
   * @return the versioned files
   */
  public Map<URI, VersionedSourceFile> getVersionedFiles() {
    return versionedFiles;
  }

  /**
   * Gets the source file modules.
   *
   * @return the source file modules
   */
  public Map<URI, Module> getSourceFileModules() {
    return sourceFileModules;
  }
}
