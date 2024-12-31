/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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

import io.github.ascopes.jct.fixtures.Fixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * {@link TraceDiagnosticListRepresentation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnosticListRepresentation tests")
@SuppressWarnings("ResultOfMethodCallIgnored")
class TraceDiagnosticListRepresentationTest {

  @DisplayName("toStringOf(null) returns \"null\"")
  @Test
  void toStringOfNullReturnsNull() {
    // Given
    var listRepr = TraceDiagnosticListRepresentation.getInstance();

    // When
    var result = listRepr.toStringOf(null);

    // Then
    assertThat(result).isEqualTo("null");
  }

  @DisplayName("toStringOf(List) outputs diagnostics in a list")
  @Test
  void toStringOfListOutputsDiagnosticsInList() {
    // Given
    try (var diagnosticReprMock = Mockito.mockStatic(TraceDiagnosticRepresentation.class)) {
      var diagnostic1 = Fixtures.someTraceDiagnostic();
      var diagnostic2 = Fixtures.someTraceDiagnostic();
      var diagnostic3 = Fixtures.someTraceDiagnostic();
      var diagnosticList = List.of(diagnostic1, diagnostic2, diagnostic3);

      var mockRepr = mock(TraceDiagnosticRepresentation.class);
      diagnosticReprMock.when(TraceDiagnosticRepresentation::getInstance).thenReturn(mockRepr);

      when(mockRepr.toStringOf(diagnostic1)).thenReturn("<<diagnostic1>>\n<content>");
      when(mockRepr.toStringOf(diagnostic2)).thenReturn("<<diagnostic2>>\n<content>");
      when(mockRepr.toStringOf(diagnostic3)).thenReturn("<<diagnostic3>>\n<content>");

      var listRepr = TraceDiagnosticListRepresentation.getInstance();

      // When
      var result = listRepr.toStringOf(diagnosticList);

      // Then
      assertThat(result.lines())
          .containsExactly(
              "",
              " - <<diagnostic1>>",
              "   <content>",
              "",
              " - <<diagnostic2>>",
              "   <content>",
              "",
              " - <<diagnostic3>>",
              "   <content>"
          );
    }
  }
}
