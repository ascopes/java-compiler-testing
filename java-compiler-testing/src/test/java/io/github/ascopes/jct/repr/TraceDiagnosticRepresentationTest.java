/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import java.util.Locale;
import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * {@link TraceDiagnosticRepresentation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnosticRepresentation tests")
@SuppressWarnings("DataFlowIssue")
class TraceDiagnosticRepresentationTest {

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

  @DisplayName("toStringOf(TraceDiagnostic) returns the expected message when no code is provided "
      + "with no source")
  @EnumSource(value = Kind.class)
  @ParameterizedTest(name = "for kind {0}")
  void toStringOfTraceDiagnosticReturnsTheExpectedMessageWhenNoCodeIsProvidedWithNoSource(
      Kind kind
  ) {
    // Given
    TraceDiagnostic<?> diagnostic = mock();
    when(diagnostic.getCode()).thenReturn(null);
    when(diagnostic.getSource()).thenReturn(null);
    when(diagnostic.getKind()).thenReturn(kind);
    when(diagnostic.getMessage(Locale.ROOT)).thenReturn("ping pong");

    var repr = TraceDiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diagnostic);

    // Then
    assertThat(result).isEqualTo(String.join(
        "\n",
        "[%s]",
        "",
        "ping pong"
    ), kind);
  }

  @DisplayName("toStringOf(TraceDiagnostic) returns the expected message when no code is provided "
      + "with a source")
  @EnumSource(value = Kind.class)
  @ParameterizedTest(name = "for kind {0}")
  void toStringOfTraceDiagnosticReturnsTheExpectedMessageWhenNoCodeIsProvidedWithSource(Kind kind) {
    // Given
    PathFileObject fileObject = mock();
    when(fileObject.getName()).thenReturn("path/to/file.java");

    TraceDiagnostic<PathFileObject> diagnostic = mock();
    when(diagnostic.getCode()).thenReturn(null);
    when(diagnostic.getSource()).thenReturn(fileObject);
    when(diagnostic.getKind()).thenReturn(kind);
    when(diagnostic.getLineNumber()).thenReturn(69L);
    when(diagnostic.getColumnNumber()).thenReturn(420L);
    when(diagnostic.getMessage(Locale.ROOT)).thenReturn("ping pong");

    var repr = TraceDiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diagnostic);

    // Then
    assertThat(result).isEqualTo(String.join(
        "\n",
        "[%s] in path/to/file.java (line 69, col 420)",
        "",
        "ping pong"
    ), kind);
  }

  @DisplayName("toStringOf(TraceDiagnostic) returns the expected message when a code is provided "
      + "with no source")
  @EnumSource(value = Kind.class)
  @ParameterizedTest(name = "for kind {0}")
  void toStringOfTraceDiagnosticReturnsTheExpectedMessageWhenCodeIsProvidedWithNoSource(Kind kind) {
    // Given
    TraceDiagnostic<?> diagnostic = mock();
    when(diagnostic.getCode()).thenReturn("it.is.broken");
    when(diagnostic.getSource()).thenReturn(null);
    when(diagnostic.getKind()).thenReturn(kind);
    when(diagnostic.getMessage(Locale.ROOT)).thenReturn("ping pong");

    var repr = TraceDiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diagnostic);

    // Then
    assertThat(result).isEqualTo(String.join(
        "\n",
        "[%s] it.is.broken",
        "",
        "ping pong"
    ), kind);
  }

  @DisplayName("toStringOf(TraceDiagnostic) returns the expected message when a code is provided "
      + "with a source")
  @EnumSource(value = Kind.class)
  @ParameterizedTest(name = "for kind {0}")
  void toStringOfTraceDiagnosticReturnsTheExpectedMessageWhenCodeIsProvidedWithSource(Kind kind) {
    // Given
    PathFileObject fileObject = mock();
    when(fileObject.getName()).thenReturn("path/to/file.java");

    TraceDiagnostic<PathFileObject> diagnostic = mock();
    when(diagnostic.getCode()).thenReturn("it.is.broken");
    when(diagnostic.getSource()).thenReturn(fileObject);
    when(diagnostic.getKind()).thenReturn(kind);
    when(diagnostic.getLineNumber()).thenReturn(69L);
    when(diagnostic.getColumnNumber()).thenReturn(420L);
    when(diagnostic.getMessage(Locale.ROOT)).thenReturn("ping pong");

    var repr = TraceDiagnosticRepresentation.getInstance();

    // When
    var result = repr.toStringOf(diagnostic);

    // Then
    assertThat(result).isEqualTo(String.join(
        "\n",
        "[%s] it.is.broken in path/to/file.java (line 69, col 420)",
        "",
        "ping pong"
    ), kind);
  }
}
