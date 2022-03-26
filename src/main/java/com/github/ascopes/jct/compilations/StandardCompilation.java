package com.github.ascopes.jct.compilations;

import com.github.ascopes.jct.diagnostics.DiagnosticWithTrace;
import com.github.ascopes.jct.paths.PathLocationRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.tools.JavaFileObject;


/**
 * Representation of the result of running a Javac compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class StandardCompilation implements Compilation {

  private final boolean wError;
  private final boolean success;
  private final List<String> outputLines;
  private final Set<? extends JavaFileObject> compilationUnits;
  private final List<DiagnosticWithTrace<? extends JavaFileObject>> diagnostics;
  private final PathLocationRepository repository;

  /**
   * Initialize the compilation result.
   *
   * @param wError           {@code true} if warnings were errors, {@code false} otherwise.
   * @param success          {@code true} if successful, {@code false} otherwise.
   * @param outputLines      the lines of output from the compiler.
   * @param compilationUnits the compilation units that were used.
   * @param diagnostics      the diagnostics that were reported.
   * @param repository       the file repository that was used.
   */
  public StandardCompilation(
      boolean wError,
      boolean success,
      List<String> outputLines,
      Set<? extends JavaFileObject> compilationUnits,
      List<DiagnosticWithTrace<? extends JavaFileObject>> diagnostics,
      PathLocationRepository repository
  ) {
    this.wError = wError;
    this.success = success;
    this.outputLines = Collections.unmodifiableList(Objects.requireNonNull(outputLines));
    this.compilationUnits = Collections.unmodifiableSet(Objects.requireNonNull(compilationUnits));
    this.diagnostics = Collections.unmodifiableList(Objects.requireNonNull(diagnostics));
    this.repository = Objects.requireNonNull(repository);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isWarningsAsErrors() {
    return wError;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSuccessful() {
    return success;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getOutputLines() {
    return outputLines;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<? extends JavaFileObject> getCompilationUnits() {
    return compilationUnits;
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  @Override
  public List<DiagnosticWithTrace<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PathLocationRepository getFileRepository() {
    return repository;
  }
}
