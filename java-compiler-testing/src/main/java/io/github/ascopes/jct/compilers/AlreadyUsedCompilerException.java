package io.github.ascopes.jct.compilers;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Exception that is thrown if there is an attempt to call compile() once it has been already called from that Compiler.
 *
 * @author Adolfo Trocolí Naranjo (github.com/adolfo-trocoli)
 * @since 0.0.1
 * @see CompilerException
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class AlreadyUsedCompilerException extends CompilerException {

  /**
   * Initialize the error.
   *
   * @param message the error message.
   */
  public AlreadyUsedCompilerException(String message) {
    super(message);
  }

  /**
   * Initialize the error.
   *
   * @param message the error message.
   * @param cause   the cause of the error.
   */
  public AlreadyUsedCompilerException(String message, Throwable cause) {
    super(message, cause);
  }
}
