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

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link StackTraceRepresentation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("StackTraceRepresentation tests")
class StackTraceRepresentationTest {

  @DisplayName("toStringOf(null) returns \"null\"")
  @Test
  void toStringOfNullReturnsNull() {
    // Given
    var repr = StackTraceRepresentation.getInstance();

    // When
    var result = repr.toStringOf(null);

    // Then
    assertThat(result).isEqualTo("null");
  }

  @DisplayName("toStringOf(List) returns the stacktrace")
  @Test
  void toStringOfStackTraceRendersCorrectly() {
    // Given
    var repr = StackTraceRepresentation.getInstance();
    var stackTrace = List.of(
        new StackTraceElement("foo.bar.Baz", "bork", "Baz.java", 123),
        new StackTraceElement("eggs.Spam", "huh", "Spam.java", 456),
        new StackTraceElement(
            "someClassLoader",
            "my.cool.modulething",
            "1.2.3-RELEASE",
            "aaa.bbb.CCC$DDD",
            "blahblahblah",
            "CCC$DDD.java",
            1_366
        )
    );

    // When
    var result = repr.toStringOf(stackTrace);

    // Then
    assertThat(result.lines())
        .containsExactly(
            "Stacktrace:",
            "\tat foo.bar.Baz.bork(Baz.java:123)",
            "\tat eggs.Spam.huh(Spam.java:456)",
            "\tat someClassLoader/my.cool.modulething@1.2.3-RELEASE/"
                + "aaa.bbb.CCC$DDD.blahblahblah(CCC$DDD.java:1366)"
        );
  }
}
