/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.presentation.Representation;

/**
 * Representation of a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class DiagnosticRepresentation implements Representation {

  private static final DiagnosticRepresentation INSTANCE
      = new DiagnosticRepresentation();

  /**
   * Get an instance of this diagnostic representation.
   *
   * @return the instance.
   */
  public static DiagnosticRepresentation getInstance() {
    return INSTANCE;
  }

  private static final int ADDITIONAL_CONTEXT_LINES = 2;
  private static final String PADDING = " ".repeat(4);

  private DiagnosticRepresentation() {
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
        .append("] ");

    var code = diagnostic.getCode();
    if (code != null) {
      builder.append(code);
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

    IoExceptionUtils.uncheckedIo(() -> {
      var snippet = extractSnippet(diagnostic);

      if (snippet != null) {
        snippet.prettyPrintTo(builder);
        builder.append('\n');
      }

      builder
          .append(PADDING)
          .append(diagnostic.getMessage(Locale.ROOT));
    });

    return builder.toString();
  }

  @Nullable
  @SuppressWarnings("ConstantConditions")
  private Snippet extractSnippet(
      Diagnostic<? extends JavaFileObject> diagnostic
  ) throws IOException {
    var source = diagnostic.getSource();

    var noSnippet = source == null
        || diagnostic.getStartPosition() == NOPOS
        || diagnostic.getEndPosition() == NOPOS;

    if (noSnippet) {
      // No info available about position, so don't bother extracting anything.
      return null;
    }

    // ECJ throws a NullPointerException in some cases if we use .getCharContent, so read this
    // manually instead.
    var contentBytes = new ByteArrayOutputStream();
    try (var input = diagnostic.getSource().openInputStream()) {
      input.transferTo(contentBytes);
    }

    var content = contentBytes.toString(StandardCharsets.UTF_8);

    var startLine = Math.max(1, (int) diagnostic.getLineNumber() - ADDITIONAL_CONTEXT_LINES);
    var lineStartOffset = StringUtils.indexOfLine(content, startLine);
    var lineEndOffset = StringUtils.indexOfEndOfLine(content, (int) diagnostic.getEndPosition());

    // Advance to include the additional lines of context
    var endOfSnippet = lineEndOffset;
    for (var i = 0; i < ADDITIONAL_CONTEXT_LINES; ++i) {
      endOfSnippet = StringUtils.indexOfEndOfLine(content, endOfSnippet + 1);
    }

    return new Snippet(
        content.substring(lineStartOffset, endOfSnippet),
        Math.max(1, diagnostic.getLineNumber() - ADDITIONAL_CONTEXT_LINES),
        diagnostic.getStartPosition() - lineStartOffset,
        lineEndOffset - lineStartOffset
    );
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
      lineNumberWidth = (int) Math.ceil(Math.log10(startLine)) + 1;
    }

    public void prettyPrintTo(StringBuilder builder) {
      var lineIndex = 0;
      var isNewLine = true;
      var startOfLine = 0;

      for (var i = 0; i < text.length(); ++i) {
        if (isNewLine) {
          appendLineNumberPart(builder, lineIndex);
          isNewLine = false;
        }

        var nextChar = text.charAt(i);
        builder.append(nextChar);

        if (nextChar == '\n' || i == text.length() - 1) {
          if (nextChar != '\n') {
            // Ensure newline if we are adding the end-of-content line
            builder.append('\n');
          }
          appendPossibleUnderline(builder, startOfLine, i);
          ++lineIndex;
          startOfLine = i + 1;
          isNewLine = true;
        }
      }
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
      if (startOfLine > endOffset || endOfLine < startOffset) {
        return;
      }

      builder
          .append(PADDING)
          .append(" ".repeat(lineNumberWidth))
          .append(" + ");

      for (int i = startOfLine; i < endOfLine; ++i) {
        builder.append(startOffset <= i && i <= endOffset ? '^' : ' ');
      }

      builder.append('\n');
    }
  }
}
