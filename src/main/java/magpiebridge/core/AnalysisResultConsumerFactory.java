package magpiebridge.core;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import magpiebridge.command.CodeActionCommand;
import magpiebridge.command.CodeActionGenerator;
import magpiebridge.core.analysis.configuration.MagpieHttpServer;
import magpiebridge.util.SourceCodePositionUtils;
import magpiebridge.util.URIUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * This factory class create{@link Consumer} for different kinds of {@link AnalysisResult}.
 *
 * @author Linghui Luo
 */
public class AnalysisResultConsumerFactory {

  protected final MagpieServer server;

  public AnalysisResultConsumerFactory(MagpieServer server) {
    this.server = server;
  }

  /**
   * Creates the diagnostic consumer.
   *
   * @param publishDiags map URI to the list of diagnostics to be published for the client.
   * @param diagList the list of diagnostics stored on the server
   * @param source the source
   * @return the consumer
   */
  public Consumer<AnalysisResult> createDiagnosticConsumer(
      Map<String, List<Diagnostic>> publishDiags, List<Diagnostic> diagList, String source) {
    Consumer<AnalysisResult> consumer =
        result -> {
          Diagnostic d = new Diagnostic();
          d.setMessage(result.toString(false));
          d.setRange(SourceCodePositionUtils.getLocationFrom(result.position()).getRange());
          d.setSource(source);
          d.setCode(result.code());
          List<DiagnosticRelatedInformation> relatedList = new ArrayList<>();
          if (result.related() != null)
            for (Pair<Position, String> related : result.related()) {
              DiagnosticRelatedInformation di = new DiagnosticRelatedInformation();
              di.setLocation(SourceCodePositionUtils.getLocationFrom(related.fst));
              di.setMessage(related.snd);
              relatedList.add(di);
            }
          d.setRelatedInformation(relatedList);
          d.setSeverity(result.severity());
          if (!diagList.contains(d)) {
            diagList.add(d);
          }
          String serverUri = result.position().getURL().toString();
          String clientUri = server.getClientUri(serverUri);
          try {
            URL url = new URL(URLDecoder.decode(clientUri, "UTF-8"));
            /*
             * If a AnalysisResult is type Diagnostic and has data in flows
             * then we will start the data flow server
             */
            if (result.related() != null) {
              String httpserverUrl = "";
              server.initAnalysisConfiguration();
              if (server.config.useMagpieHTTPServer()) {
                // Getting the newly created data flow server path to show the flow graph page
                httpserverUrl = MagpieHttpServer.createAndStartDataFlowHttpServer(server, result);
              }
              /*
               * The message with link to view flow graph
               */
              String title = "View flow diagram: " + httpserverUrl;
              CodeAction dataFlowView =
                  CodeActionGenerator.generateCommandAction(
                      title, httpserverUrl, d, CodeActionCommand.openURLFromMB.name());
              server.addCodeAction(url, d.getRange(), dataFlowView);
            }
            if (result.repair() != null) {
              // add code action (quickfix) related to analysis result
              Position fixPos = result.repair().fst;
              if (fixPos != null) {
                String replace = result.repair().snd;
                Range range = SourceCodePositionUtils.getLocationFrom(fixPos).getRange();
                CodeAction fix =
                    CodeActionGenerator.replace(
                        "Fix: replace it with " + replace, range, replace, clientUri, d);
                server.addCodeAction(url, d.getRange(), fix);
              }
            } else if (result.command() != null) {
              result
                  .command()
                  .forEach(
                      (cmd) -> {
                        if (server.clientConfig.getTextDocument().getCodeLens() != null) {
                          CodeLens codeLens = new CodeLens();
                          codeLens.setCommand(cmd);
                          codeLens.setRange(d.getRange());
                          server.addCodeLens(url, codeLens);
                        } else {
                          if (server.clientConfig.getTextDocument().getCodeAction() != null) {
                            CodeAction codeAction = new CodeAction();
                            codeAction.setCommand(cmd);
                            codeAction.setTitle(cmd.getTitle());
                            codeAction.setDiagnostics(Collections.singletonList(d));
                            codeAction.setKind(CodeActionKind.Source);
                            server.addCodeAction(url, d.getRange(), codeAction);
                          }
                        }
                      });
            }
            if (server.config.supportWarningSuppression()) {
              // support warning suppression.
              String title = "Suppress this warning";
              CodeAction suppressWarning =
                  CodeActionGenerator.generateCommandAction(
                      title, clientUri, d, CodeActionCommand.suppressWarningFromMB.name());
              server.addCodeAction(url, d.getRange(), suppressWarning);
            }

            if (server.config.reportFalsePositive()) {
              // report false positive
              String title = "Report false alarm";
              CodeAction reportFalsePositive =
                  CodeActionGenerator.generateCommandAction(
                      title, clientUri, d, CodeActionCommand.reportFPFromMB.name());
              server.addCodeAction(url, d.getRange(), reportFalsePositive);
            }
            if (server.config.reportConfusion()) {
              // report confusion about the warning message
              String title = "I don't understand this warning";
              CodeAction reportConfusion =
                  CodeActionGenerator.generateCommandAction(
                      title, clientUri, d, CodeActionCommand.reportConfusionFromMB.name());
              server.addCodeAction(url, d.getRange(), reportConfusion);
            }
          } catch (MalformedURLException | UnsupportedEncodingException e) {
            MagpieServer.ExceptionLogger.log(e);
            e.printStackTrace();
          }
          if (clientUri != null) {
            publishDiags.put(clientUri, diagList);
          }
        };
    return consumer;
  }

  /**
   * Creates the hover consumer.
   *
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createHoverConsumer() {
    Consumer<AnalysisResult> consumer =
        result -> {
          try {
            String serverUri = result.position().getURL().toString();
            String clientUri = server.getClientUri(serverUri);
            URL clientURL = new URL(clientUri);
            Position pos = URIUtils.replaceURL(result.position(), clientURL);
            Hover hover = new Hover();

            if (server.clientConfig != null
                && server.clientConfig.getTextDocument().getHover().getContentFormat() != null
                && server
                    .clientConfig
                    .getTextDocument()
                    .getHover()
                    .getContentFormat()
                    .contains(MarkupKind.MARKDOWN)) {
              MarkupContent content = new MarkupContent(MarkupKind.MARKDOWN, result.toString(true));
              hover.setContents(content);
            } else {
              List<Either<String, MarkedString>> contents = new ArrayList<>();
              for (String str : result.toString(false).split("\n")) {
                Either<String, MarkedString> content = Either.forLeft(str);
                contents.add(content);
              }
              hover.setContents(contents);
            }
            hover.setRange(SourceCodePositionUtils.getLocationFrom(pos).getRange());
            NavigableMap<Position, Hover> hoverMap = new TreeMap<>();
            if (server.hovers.containsKey(clientURL)) {
              hoverMap = server.hovers.get(clientURL);
            }
            hoverMap.put(pos, hover);
            server.hovers.put(clientURL, hoverMap);
          } catch (MalformedURLException e) {
            MagpieServer.ExceptionLogger.log(e);
            e.printStackTrace();
          }
        };
    return consumer;
  }

  /**
   * Creates the code lens consumer.
   *
   * @return the consumer
   */
  protected Consumer<AnalysisResult> createCodeLensConsumer() {
    Consumer<AnalysisResult> consumer =
        result -> {
          try {
            String serverUri = result.position().getURL().toString();
            String clientUri = server.getClientUri(serverUri);
            URL clientURL = new URL(clientUri);
            CodeLens codeLens = new CodeLens();
            if (result.repair() != null) {
              Location loc = SourceCodePositionUtils.getLocationFrom(result.repair().fst);
              codeLens.setCommand(new Command("fix", CodeActionCommand.fixFromMB.name()));
              codeLens
                  .getCommand()
                  .setArguments(Arrays.asList(clientUri, loc.getRange(), result.repair().snd));
            } else {
              codeLens.setCommand(result.command().iterator().next());
            }
            codeLens.setRange(
                SourceCodePositionUtils.getLocationFrom(result.position()).getRange());
            List<CodeLens> list = new ArrayList<>();
            if (server.codeLenses.containsKey(clientURL)) {
              list = server.codeLenses.get(clientURL);
            }
            list.add(codeLens);
            server.codeLenses.put(clientURL, list);
          } catch (MalformedURLException e) {
            MagpieServer.ExceptionLogger.log(e);
            e.printStackTrace();
          }
        };
    return consumer;
  }
}
