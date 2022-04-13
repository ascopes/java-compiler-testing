/*
 * Copyright (C) 2022 Ashley Scopes
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

package com.github.ascopes.jct.testing.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.ascopes.jct.compilers.CompilerException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CompilerException} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("CompilerException tests")
class CompilerExceptionTest {
  @DisplayName("new CompilerException(String) sets the message")
  @Test
  void newCompilerExceptionWithStringSetsTheMessage() {
    // Given
    var message = UUID.randomUUID().toString();

    // When
    var exception = new CompilerException(message);

    // Then
    assertThat(exception).hasMessage(message);
  }

  @DisplayName("new CompilerException(String, Throwable) sets the message and cause")
  @Test
  void newCompilerExceptionWithStringSetsTheMessageAndCause() {
    // Given
    var message = UUID.randomUUID().toString();
    var cause = new IllegalArgumentException("bish bash bosh").fillInStackTrace();

    // When
    var exception = new CompilerException(message, cause);

    // Then
    assertThat(exception).hasMessage(message).hasCause(cause);
  }
}
