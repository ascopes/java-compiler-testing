package com.github.ascopes.jct.compilers;

/**
 * Error that is thrown if the compiler fails to run and throws an unhandled exception.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class CompilerException extends RuntimeException {

  /**
   * Initialize the error.
   *
   * @param message the error message.
   */
  public CompilerException(String message) {
    super(message);
  }

  /**
   * Initialize the error.
   *
   * @param message the error message.
   * @param cause   the cause of the error.
   */
  public CompilerException(String message, Throwable cause) {
    super(message, cause);
  }
}
