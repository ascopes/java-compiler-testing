package io.github.ascopes.jct.testing.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.ascopes.jct.compilers.AlreadyUsedCompilerException;

/**
 * {@link CompilerException} tests.
 *
 * @author Adolfo Trocolí Naranjo (github.com/adolfo-trocoli)
 */
@DisplayName("AlreadyUsedCompilerException tests")
class AlreadyUsedCompilerExceptionTest {
	
  @DisplayName("new AlreadyUsedCompilerException(String) sets the message")
  @Test
  void newAlreadyUsedCompilerExceptionWithStringSetsTheMessage() {
    // Given
    var message = UUID.randomUUID().toString();

    // When
    var exception = new AlreadyUsedCompilerException(message);

    // Then
    assertThat(exception).hasMessage(message);
  }

  @DisplayName("new AlreadyUsedCompilerException(String, Throwable) sets the message and cause")
  @Test
  void newAlreadyUsedCompilerExceptionWithStringSetsTheMessageAndCause() {
    // Given
    var message = UUID.randomUUID().toString();
    var cause = new IllegalArgumentException("bish bash bosh").fillInStackTrace();

    // When
    var exception = new AlreadyUsedCompilerException(message, cause);

    // Then
    assertThat(exception).hasMessage(message).hasCause(cause);
  }
}
