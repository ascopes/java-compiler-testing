/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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
package io.github.ascopes.jct.tests.unit.repr;

import static io.github.ascopes.jct.tests.helpers.Fixtures.oneOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.repr.TraceDiagnosticRepresentation;
import io.github.ascopes.jct.tests.helpers.GenericMock;
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
 * {@link TraceDiagnosticRepresentation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnosticRepresentation tests")
@SuppressWarnings("NullableProblems")
class TraceDiagnosticRepresentationTest {

  @TempDir
  Path tempDir;

  @DisplayName("toStringOf(null) returns \"null\"")
  @Test
  void toStringOfNullReturnsNull() {
    // Given
    var repr = TraceDiagnosticRepresentation.getInstance();

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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

  @SuppressWarnings("resource")
  @DisplayName(
      "toStringOf(TraceDiagnostic) renders the diagnostic when file contents are present "
          + "via getCharContent"
  )
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenFileContentsArePresentViaGetCharContent()
      throws IOException {
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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

    verify(file).getCharContent(true);
    verify(file, never()).openInputStream();
  }

  @SuppressWarnings("resource")
  @DisplayName(
      "toStringOf(TraceDiagnostic) renders the diagnostic when file contents are present "
          + "via openInputStream but getCharContent raises an exception"
  )
  @Test
  void toStringOfTraceDiagnosticRendersTheDiagnosticWhenFileContentsArePresentViaOpenInputStream()
      throws IOException {

    // Given
    var file = someFileObject();
    when(file.getCharContent(anyBoolean())).thenThrow(new RuntimeException("error"));

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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

    verify(file).getCharContent(true);
    verify(file).openInputStream();
  }

  @SuppressWarnings("resource")
  @DisplayName(
      "toStringOf(TraceDiagnostic) renders the diagnostic when file contents are present "
          + "via openInputStream but getCharContent returns null"
  )
  @Test
  void toStringOfTraceDiagnosticRendersDiagnosticWhenGetCharContentReturnsNullViaOpenInputStream()
      throws IOException {

    // Given
    var file = someFileObject();
    when(file.getCharContent(anyBoolean())).thenReturn(null);

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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

    verify(file).getCharContent(true);
    verify(file).openInputStream();
  }

  @SuppressWarnings("resource")
  @DisplayName(
      "toStringOf(TraceDiagnostic) does not render the diagnostic when file contents are not "
          + "present"
  )
  @Test
  void toStringOfTraceDiagnosticDoesNotRenderTheDiagnosticWhenFileContentsAreNotPresent()
      throws IOException {

    // Given
    var file = someFileObject();
    when(file.getCharContent(anyBoolean())).thenThrow(new RuntimeException("error"));
    when(file.openInputStream()).thenThrow(new RuntimeException("error"));

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

    var repr = TraceDiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diag);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "[" + kind.toString() + "] " + file.getName() + " (at line 6, col 16)",
            "",
            "    Entrypoint must be a void method."
        );

    verify(file).getCharContent(true);
    verify(file).openInputStream();
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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

  @DisplayName(
      "toStringOf(TraceDiagnostic) renders snippets with erroneous end positions correctly"
  )
  @Test
  void toStringOfRendersMultilineSnippetsCorrectlyWithErroneousEndPositions() {
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
    when(diag.getEndPosition()).thenReturn(100_000_000L);
    when(diag.getSource()).thenReturn(file);
    when(diag.getMessage(any())).thenReturn("Entrypoint must be a void method.");

    var repr = TraceDiagnosticRepresentation.getInstance();

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
            "       + ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "     8 |     System.out.print(\"What is your name? \");",
            "       + ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "     9 |     var name = scanner.nextLine();",
            "       + ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "    10 |     System.out.printf(\"Hello, %s!\", name);",
            "       + ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "    11 |   }",
            "       + ^^^",
            "    12 | }",
            "       + ^",
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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

    var repr = TraceDiagnosticRepresentation.getInstance();

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

  @SuppressWarnings("SameParameterValue")
  JavaFileObject someFileObject(String fileName, String... lines) {
    var path = tempDir.resolve(fileName);

    try (var writer = Files.newBufferedWriter(path)) {
      for (var line : lines) {
        writer.write(line);
        writer.write('\n');
      }

      var fileObject = mock(JavaFileObject.class, withSettings().strictness(Strictness.LENIENT));
      when(fileObject.getCharContent(anyBoolean())).then(ctx -> Files.readString(path));
      when(fileObject.openInputStream()).then(ctx -> Files.newInputStream(path));
      when(fileObject.toUri()).thenReturn(path.toUri());
      when(fileObject.getName()).thenReturn(path.toString());

      return fileObject;
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
