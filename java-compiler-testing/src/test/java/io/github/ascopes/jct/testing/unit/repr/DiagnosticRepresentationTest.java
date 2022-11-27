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
package io.github.ascopes.jct.testing.unit.repr;

import static io.github.ascopes.jct.testing.helpers.Fixtures.oneOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.repr.DiagnosticRepresentation;
import io.github.ascopes.jct.testing.helpers.GenericMock;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.quality.Strictness;

/**
 * {@link DiagnosticRepresentation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("DiagnosticRepresentation tests")
@SuppressWarnings("NullableProblems")
class DiagnosticRepresentationTest {

  @TempDir
  Path tempDir;

  @DisplayName("toStringOf(null) returns \"null\"")
  @Test
  void toStringOfNullReturnsNull() {
    // Given
    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(null);

    // Then
    assertThat(result).isEqualTo("null");
  }

  @DisplayName("toStringOf(TraceDiagnostic) renders the diagnostic when a code is present")
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenCodesArePresent() {
    // Given
    var kind = oneOf(Kind.values());

    var diag = GenericMock
        .mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();

    when(diag.getKind()).thenReturn(kind);
    when(diag.getCode()).thenReturn("you.done.messed.up");
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "] you.done.messed.up",
            "",
            "    Entrypoint must be a void method."
        );
  }

  @DisplayName("toStringOf(TraceDiagnostic) renders the diagnostic when file contents are present")
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenFileContentsArePresent() {
    // Given
    var file = someFileObject();

    var kind = oneOf(Kind.values());

    var diag = GenericMock
        .mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();

    when(diag.getKind()).thenReturn(kind);
    when(diag.getLineNumber()).thenReturn(6L);
    when(diag.getColumnNumber()).thenReturn(16L);
    when(diag.getStartPosition()).thenReturn(77L);
    when(diag.getEndPosition()).thenReturn(133L);
    when(diag.getSource()).thenReturn(file);
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "] " + file.getName() + " (at line 6, col 16)",
            "",
            "     4 | ",
            "     5 | public class HelloWorld {",
            "     6 |   public static int main(String[] args) throws Throwable {",
            "       +   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "     7 |     var scanner = new Scanner(System.in);",
            "     8 |     System.out.print(\"What is your name? \");",
            "",
            "    Entrypoint must be a void method."
        );
  }

  @DisplayName("toStringOf(TraceDiagnostic) renders multiline snippets correctly")
  @Test
  void toStringOfRendersMultilineSnippetsCorrectly() {
    // Given
    var file = someFileObject();

    var kind = oneOf(Kind.values());

    var diag = GenericMock
        .mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();

    when(diag.getKind()).thenReturn(kind);
    when(diag.getLineNumber()).thenReturn(6L);
    when(diag.getColumnNumber()).thenReturn(16L);
    when(diag.getStartPosition()).thenReturn(77L);
    when(diag.getEndPosition()).thenReturn(149L);
    when(diag.getSource()).thenReturn(file);
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "] " + file.getName() + " (at line 6, col 16)",
            "",
            "     4 | ",
            "     5 | public class HelloWorld {",
            "     6 |   public static int main(String[] args) throws Throwable {",
            "       +   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "     7 |     var scanner = new Scanner(System.in);",
            "       + ^^^^^^^^^^^^^^^",
            "     8 |     System.out.print(\"What is your name? \");",
            "     9 |     var name = scanner.nextLine();",
            "",
            "    Entrypoint must be a void method."
        );
  }

  @DisplayName("toStringOf(TraceDiagnostic) renders the diagnostic when source is not present")
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenSourceIsNotPresent() {
    // Given
    var kind = oneOf(Kind.values());

    var diag = GenericMock
        .mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();

    when(diag.getKind()).thenReturn(kind);
    when(diag.getLineNumber()).thenReturn(6L);
    when(diag.getColumnNumber()).thenReturn(16L);
    when(diag.getStartPosition()).thenReturn(77L);
    when(diag.getEndPosition()).thenReturn(132L);
    when(diag.getSource()).thenReturn(null);
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "]",
            "",
            "    Entrypoint must be a void method."
        );
  }

  @DisplayName("toStringOf(TraceDiagnostic) renders the diagnostic no start position is present")
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenStartPositionIsNotPresent() {
    // Given
    var kind = oneOf(Kind.values());
    var file = someFileObject();
    var diag = GenericMock
        .mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();

    when(diag.getKind()).thenReturn(kind);
    when(diag.getLineNumber()).thenReturn(6L);
    when(diag.getColumnNumber()).thenReturn(16L);
    when(diag.getStartPosition()).thenReturn(-1L);
    when(diag.getEndPosition()).thenReturn(132L);
    when(diag.getSource()).thenReturn(file);
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "] " + file.getName() + " (at line 6, col 16)",
            "",
            "    Entrypoint must be a void method."
        );
  }

  @DisplayName("toStringOf(TraceDiagnostic) renders the diagnostic no end position is present")
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenEndPositionIsNotPresent() {
    // Given
    var kind = oneOf(Kind.values());
    var file = someFileObject();
    var diag = GenericMock
        .mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();

    when(diag.getKind()).thenReturn(kind);
    when(diag.getLineNumber()).thenReturn(6L);
    when(diag.getColumnNumber()).thenReturn(16L);
    when(diag.getStartPosition()).thenReturn(123L);
    when(diag.getEndPosition()).thenReturn(-1L);
    when(diag.getSource()).thenReturn(file);
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = DiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "] " + file.getName() + " (at line 6, col 16)",
            "",
            "    Entrypoint must be a void method."
        );
  }

  JavaFileObject someFileObject() {
    return someFileObject(
        "HelloWorld.java",
        "package org.example;",
        "",
        "import java.util.Scanner;",
        "",
        "public class HelloWorld {",
        "  public static int main(String[] args) throws Throwable {",
        "    var scanner = new Scanner(System.in);",
        "    System.out.print(\"What is your name? \");",
        "    var name = scanner.nextLine();",
        "    System.out.printf(\"Hello, %s!\", name);",
        "  }",
        "}"
    );
  }

  JavaFileObject someFileObject(String fileName, String... lines) {
    var path = tempDir.resolve(fileName);

    try (var writer = Files.newBufferedWriter(path)) {
      for (var line : lines) {
        writer.write(line);
        writer.write('\n');
      }

      var fileObject = mock(JavaFileObject.class, withSettings().strictness(Strictness.LENIENT));
      when(fileObject.openInputStream()).then(ctx -> Files.newInputStream(path));
      when(fileObject.toUri()).thenReturn(path.toUri());
      when(fileObject.getName()).thenReturn(path.toString());

      return fileObject;
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
