package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.compilers.Compilation;
import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic.Kind;
import org.assertj.core.api.AbstractAssert;

public final class CompilationAssert extends AbstractAssert<CompilationAssert, Compilation> {

  private static final Set<Kind> WARNING_DIAGNOSTIC_KINDS = Stream
      .of(Kind.WARNING, Kind.MANDATORY_WARNING)
      .collect(Collectors.toUnmodifiableSet());

  private static final Set<Kind> ERROR_DIAGNOSTIC_KINDS = Stream
      .of(Kind.ERROR)
      .collect(Collectors.toUnmodifiableSet());

  private static final Set<Kind> WARNING_AND_ERROR_DIAGNOSTIC_KINDS = Stream
      .of(WARNING_DIAGNOSTIC_KINDS, ERROR_DIAGNOSTIC_KINDS)
      .flatMap(Set::stream)
      .collect(Collectors.toUnmodifiableSet());

  /**
   * Initialize this compilation assertion.
   *
   * @param value    the value to assert on.
   */
  public CompilationAssert(Compilation value) {
    super(value, CompilationAssert.class);
  }

  /**
   * Assert that the compilation was successful.
   *
   * @return this assertion object.
   */
  public CompilationAssert isSuccessful() {
    if (actual.isFailure()) {
      // If we have error diagnostics, add them to the error message to provide helpful debugging
      // information. If we are treating warnings as errors, then we want to include those in this
      // as well.
      Predicate<TraceDiagnostic<?>> isErrorDiagnostic = actual.isFailOnWarnings()
          ? diag -> WARNING_AND_ERROR_DIAGNOSTIC_KINDS.contains(diag.getKind())
          : diag -> ERROR_DIAGNOSTIC_KINDS.contains(diag.getKind());

      var diagnostics = actual
          .getDiagnostics()
          .stream()
          .filter(isErrorDiagnostic)
          .collect(Collectors.toUnmodifiableList());

      failWithDiagnostics(diagnostics, "Expected a successful compilation, but it failed.");
    }

    return myself;
  }

  /**
   * Assert that the compilation was successful and had no warnings.
   *
   * <p>If warnings were treated as errors by the compiler, then this is identical to calling
   * {@link #isSuccessful()}.
   *
   * @return this assertion object.
   */
  public CompilationAssert isSuccessfulWithoutWarnings() {
    isSuccessful();
    diagnostics().hasNoErrorsOrWarnings();
    return myself;
  }

  /**
   * Assert that the compilation was a failure.
   *
   * @return this assertion object.
   */
  public CompilationAssert isFailure() {
    if (actual.isSuccessful()) {
      var warnings = actual
          .getDiagnostics()
          .stream()
          .filter(kind -> WARNING_DIAGNOSTIC_KINDS.contains(kind.getKind()))
          .collect(Collectors.toUnmodifiableList());

      failWithDiagnostics(warnings, "Expected compilation to fail, but it succeeded.");
    }

    return myself;
  }

  /**
   * Get assertions for diagnostics.
   *
   * @return assertions for the diagnostics.
   */
  public DiagnosticListAssert diagnostics() {
    return new DiagnosticListAssert(actual.getDiagnostics());
  }

  private void failWithDiagnostics(
      List<? extends TraceDiagnostic<?>> diagnostics,
      String message,
      Object... args
  ) {
    if (diagnostics.isEmpty()) {
      failWithMessage(message, args);
    } else {
      var fullMessage = String.join(
          "\n\n",
          args.length > 0
              ? String.format(message, args)
              : message,
          "Diagnostics:",
          DiagnosticListRepresentation.getInstance().toStringOf(diagnostics)
      );

      failWithMessage(fullMessage);
    }
  }
}
