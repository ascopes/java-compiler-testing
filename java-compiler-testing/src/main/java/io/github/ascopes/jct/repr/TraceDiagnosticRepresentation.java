/*
 * Copyright (C) 2022 - 2023, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.repr;

import static javax.tools.Diagnostic.NOPOS;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.utils.IoExceptionUtils;
import io.github.ascopes.jct.utils.StringUtils;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.presentation.Representation;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class TraceDiagnosticRepresentation implements Representation {

  private static final TraceDiagnosticRepresentation INSTANCE
      = new TraceDiagnosticRepresentation();

  /**
   * Get an instance of this diagnostic representation.
   *
   * @return the instance.
   */
  public static TraceDiagnosticRepresentation getInstance() {
    return INSTANCE;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(TraceDiagnosticRepresentation.class);
  private static final int ADDITIONAL_CONTEXT_LINES = 2;
  private static final String PADDING = " ".repeat(4);

  private TraceDiagnosticRepresentation() {
    // Nothing to see here, move along now.
  }

  @Override
  public String toStringOf(@Nullable Object object) {
    if (object == null) {
      return "null";
    }

    var diagnostic = (TraceDiagnostic<?>) object;

    var builder = new StringBuilder("[")
        .append(diagnostic.getKind())
        .append("]");

    var code = diagnostic.getCode();
    if (code != null) {
      builder.append(' ')
          .append(code);
    }

    if (diagnostic.getSource() != null) {
      builder
          .append(' ')
          .append(diagnostic.getSource().getName())
          .append(" (at line ")
          .append(diagnostic.getLineNumber())
          .append(", col ")
          .append(diagnostic.getColumnNumber())
          .append(")");
    }

    builder.append("\n\n");
    
    var snippet = extractSnippet(diagnostic);

    if (snippet != null) {
      snippet.prettyPrintTo(builder);
      builder.append('\n');
    }

    var message = IoExceptionUtils
        .uncheckedIo(() -> diagnostic.getMessage(Locale.ROOT));

    return builder
        .append(PADDING)
        .append(message)
        .toString();
  }

  @Nullable
  private Snippet extractSnippet(Diagnostic<? extends JavaFileObject> diagnostic) {
    var source = diagnostic.getSource();

    var noSnippet = source == null
        || diagnostic.getStartPosition() == NOPOS
        || diagnostic.getEndPosition() == NOPOS;

    if (noSnippet) {
      // No info available about position, or no source available, so don't bother extracting
      // anything.
      return null;
    }

    var content = tryGetContents(diagnostic.getSource());

    if (content == null) {
      // Unable to read the file for whatever reason, so don't bother extracting anything.
      return null;
    }

    var startLine = Math.max(1, (int) diagnostic.getLineNumber() - ADDITIONAL_CONTEXT_LINES);
    var lineStartOffset = StringUtils.indexOfLine(content, startLine);
    var endOffset = (int) diagnostic.getEndPosition();

    // Advance to include the additional lines of context, and don't treat the current line as
    // being one of those lines if we span over one and a half lines.
    var endOfSnippet = endOffset >= content.length() || content.charAt(endOffset) == '\n'
        ? endOffset
        : StringUtils.indexOfEndOfLine(content, endOffset);

    for (var index = 0; index < ADDITIONAL_CONTEXT_LINES; ++index) {
      // For each additional context line we should add, get the end of the line.
      // We do this incrementally as we cannot compute in advance where the line
      // is going to end.
      endOfSnippet = StringUtils.indexOfEndOfLine(content, endOfSnippet + 1);
    }

    return new Snippet(
        content.substring(lineStartOffset, endOfSnippet),
        Math.max(1, diagnostic.getLineNumber() - ADDITIONAL_CONTEXT_LINES),
        diagnostic.getStartPosition() - lineStartOffset,
        endOffset - lineStartOffset
    );
  }

  @Nullable
  private static String tryGetContents(FileObject fileObject) {
    // We may not always be able to read the contents of a file object correctly. This may be down
    // to IO exceptions occurring on the disk, or it may be due to the components under-test
    // using custom FileObject implementations that do not play nicely with being accessed like
    // this (Manifold being an example here). To work around this, we operate on a best-effort
    // basis only. Some calls just do not work properly under certain compilers either
    // (getCharContent() can randomly fail under ECJ for example).

    var implName = fileObject.getClass().getName();
    var uri = fileObject.toUri();

    // Try to get the content via the getCharContent method.
    try {
      var content = fileObject.getCharContent(true);
      if (content != null) {
        return content.toString();
      }
    } catch (Exception ex) {
      LOGGER.debug("Failed to read char content for file object {} ({})", uri, implName, ex);
    }

    // If that fails, attempt to use an input stream to deal with this.
    try (var input = fileObject.openInputStream()) {
      var contentBytes = new ByteArrayOutputStream();
      input.transferTo(contentBytes);
      return contentBytes.toString(StandardCharsets.UTF_8);
    } catch (Exception ex) {
      LOGGER.debug("Failed to read input stream for file object {} ({})", uri, implName, ex);
    }

    // If we still cannot get anywhere, we should just give up.
    LOGGER.warn(
        "Failed to read content for file object {} ({}), cannot produce a snippet preview. "
            + "Enable debug logs to see the cause of this issue.",
        uri,
        implName
    );

    return null;
  }

  private static final class Snippet {

    private final String text;
    private final long startLine;
    private final long startOffset;
    private final long endOffset;
    private final int lineNumberWidth;

    private Snippet(String text, long startLine, long startOffset, long endOffset) {
      this.text = text;
      this.startLine = startLine;
      this.startOffset = startOffset;
      this.endOffset = endOffset;

      // Width of the line number part of the output.
      // Use log10 to determine the number of decimal digits
      // to use. This allows us to produce a consistent width
      // margin that will fit every line number. We only show a
      // few lines in the snippet usually, so adding 1 more
      // digit to this provides us safety if the snippet were
      // to start on line 99 and move into line 100.
      lineNumberWidth = getDigitCount(startLine) + 1;
    }

    public void prettyPrintTo(StringBuilder builder) {
      var lineIndex = 0;
      var isNewLine = true;
      var startOfLine = 0;

      for (var index = 0; index < text.length(); ++index) {
        if (isNewLine) {
          appendLineNumberPart(builder, lineIndex);
          isNewLine = false;
        }

        var nextChar = text.charAt(index);
        builder.append(nextChar);

        if (nextChar == '\n' || index == text.length() - 1) {
          if (nextChar != '\n') {
            // Ensure newline if we are adding the end-of-content line
            builder.append('\n');
          }

          appendPossibleUnderline(builder, startOfLine, index);
          ++lineIndex;
          startOfLine = index + 1;
          isNewLine = true;
        }
      }
    }

    private int getDigitCount(long number) {
      return (int) Math.ceil(Math.log10(number));
    }

    private void appendLineNumberPart(StringBuilder builder, int lineIndex) {
      var rawLineString = Long.toString(lineIndex + startLine);
      var paddedLineString = StringUtils.leftPad(rawLineString, lineNumberWidth, ' ');

      builder
          .append(PADDING)
          .append(paddedLineString)
          .append(" | ");
    }

    private void appendPossibleUnderline(StringBuilder builder, int startOfLine, int endOfLine) {
      // The idea here is to consider where abouts we are in the snippet we are formatting.
      // If we find that part of the marked position range in the diagnostic lies within the
      // current line, we will attempt to create an extra "underlining" line after the current
      // line.
      //
      // This involves marking the beginning and end of the diagnostic position range by using
      // the ^ caret character, and filling the middle in with the ~ tilde character.
      //
      // Each underline line will start with the line number gutter left empty, and the vertical
      // separator will use the + plus character rather than a vertical pipe | character.
      //
      // Output rendering is complicated a little further because the start position of the content
      // to underline may not be on the same line as the end position, so we would need to then
      // wrap around to multiple lines to achieve this.
      //
      // Expected output looks like this...
      //
      //  010 | public class App {
      //  011 |   public static void main(String[[] args) {
      //      +                           ^~~~~~~~^
      //  012 |     System.out.println("Hello, World!");
      //  013 |   }
      //
      // ...or when across multiple lines...
      //
      //    009 |
      //    010 |   public static void main(String[] args) {
      //        +                 ^~~~~~~~~~~~~~~~~~~~~~~~~~
      //    011 |     System.out.println("Hello, World!");
      //        + ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
      //    012 |     return false;
      //        + ~~~~~~~~~~~~~~~~^
      //    013 |   }
      //    014 | }

      // If we are after the snippet or before the snippet and neither the start nor the
      // end is on the current line we are considering, then we do not do anything.
      if (startOfLine > endOffset || endOfLine < startOffset) {
        return;
      }

      builder
          .append(PADDING)
          .append(" ".repeat(lineNumberWidth))
          .append(" + ");

      // Go to the end of the line or the end of the highlighted section, whichever
      // comes first.
      var endIndex = Math.min(endOfLine, endOffset);

      // If we are after the end of the snippet, we will not do anything.
      for (var index = startOfLine; index < endIndex; ++index) {
        // If we are before the start of the highlighted section, but still on the line it starts
        // at, then add padding space characters.
        if (index < startOffset) {
          builder.append(' ');
          continue;
        }

        // If we are on the start or end of the highlighted section, add a caret to mark it.
        if (index == startOffset || index == endOffset - 1) {
          builder.append('^');
          continue;
        }

        // Otherwise, we are in the middle of the highlighted section, so add the tilde.
        builder.append('~');
      }

      // Since getting this far implies we are adding an underline, we need an additional
      // line feed to finish.
      builder.append('\n');
    }
  }
}
