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
package io.github.ascopes.jct.tests.unit.ex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.ex.JctCompilerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JctCompilerException}.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctCompilerException tests")
class JctCompilerExceptionTest {

  @DisplayName("The message is set when no cause is given")
  @Test
  void messageIsSetWhenNoCauseGiven() {
    // Given
    var message = "foo bar baz";

    // When
    var ex = new JctCompilerException(message);

    // Then
    assertThat(ex)
        .hasMessage("foo bar baz");
  }

  @DisplayName("The message is set when a cause is given")
  @Test
  void messageIsSetWhenCauseGiven() {
    // Given
    var message = "qux quxx quxxx";
    var cause = mock(Throwable.class);

    // When
    var ex = new JctCompilerException(message, cause);

    // Then
    assertThat(ex)
        .hasMessage("qux quxx quxxx");
  }

  @DisplayName("The cause is set when a cause is given")
  @Test
  void causeIsSetWhenCauseGiven() {
    // Given
    var message = "do ray me";
    var cause = mock(Throwable.class);

    // When
    var ex = new JctCompilerException(message, cause);

    // Then
    assertThat(ex)
        .hasCause(cause);
  }
}
