package com.github.ascopes.jct.compilations;

import com.github.ascopes.jct.diagnostics.DiagnosticWithTrace;
import com.github.ascopes.jct.paths.PathLocationRepository;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;

/**
 * Interface representing the result of a compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface Compilation {

  /**
   * Determine if warnings were treated as errors.
   *
   * @return {@code true} if warnings were treated as errors, or {@code false} otherwise.
   */
  boolean isWarningsAsErrors();

  /**
   * Determine if the compilation was successful or not.
   *
   * @return {@code true} if successful, or {@code false} if not successful.
   */
  boolean isSuccessful();

  /**
   * Determine if the compilation was a failure or not.
   *
   * @return {@code true} if not successful, or {@code false} if successful.
   */
  default boolean isFailure() {
    return !isSuccessful();
  }

  /**
   * Get the lines of output produced by the compiler, if any were captured.
   *
   * <p>This is separate to diagnostics.
   *
   * @return the lines of output.
   */
  List<String> getOutputLines();

  /**
   * Get the compilation units used in the compilation.
   *
   * @return the compilation units.
   */
  Set<? extends JavaFileObject> getCompilationUnits();

  /**
   * Get the diagnostics that were reported by the compilation.
   *
   * @return the diagnostics
   */
  List<DiagnosticWithTrace<? extends JavaFileObject>> getDiagnostics();

  /**
   * Get the location repository that was used to store files.
   *
   * @return the location repository.
   */
  PathLocationRepository getFileRepository();
}
