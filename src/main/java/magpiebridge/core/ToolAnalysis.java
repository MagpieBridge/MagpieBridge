package magpiebridge.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface for analysis which runs CLI tools.
 *
 * @author Linghui Luo
 * @author Julian Dolby
 */
public interface ToolAnalysis extends Analysis<AnalysisConsumer> {

  /**
   * This methods starts a process which runs the command in the given directory. It should be
   * called in the analyze method.
   *
   * @param dirPath the directory where the command should be executed
   * @return he process which runs the command.
   * @throws IOException IOExeception thrown when executing the command.
   */
  default Process runCommand(File dirPath) throws IOException {
    String[] command = this.getCommand();
    Process process = new ProcessBuilder().directory(dirPath).command(command).start();
    return process;
  }

  /**
   * This methods starts a process which runs the command in the given directory and returns the
   * output of the tool after executing the command. It should be called in the analyze method.
   *
   * @param dirPath the directory where the command should be executed
   * @return the output of the tool after executing the command.
   * @throws IOException IOExeception thrown when executing the command.
   */
  default Stream<String> runCommandAndReturnOutput(File dirPath) throws IOException {
    Process process = runCommand(dirPath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    Stream<String> lines = reader.lines();
    reader.close();
    return lines;
  }

  /** @return the CLI command including the arguments to be executed */
  public String[] getCommand();

  /** @return the analysis results of the tool converted in format of {@link AnalysisResult} */
  public Collection<AnalysisResult> convertToolOutput();
}
