package com.github.ascopes.jct.assertions;

import com.github.ascopes.jct.compilations.Compilation;
import com.github.ascopes.jct.diagnostics.DiagnosticWithTrace;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ListAssert;

/**
 * Assertions to apply to compilation output.
 *
 * @param <C> the implementation of the compilation.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class CompilationAssertions<C extends Compilation>
    extends AbstractObjectAssert<CompilationAssertions<C>, C> {

  /**
   * Initialize this set of assertions.
   *
   * @param compilation the compilation type.
   */
  protected CompilationAssertions(C compilation) {
    super(compilation, CompilationAssertions.class);
  }

  /**
   * Assert that the compilation succeeded.
   *
   * @return this object.
   */
  public CompilationAssertions<C> isSuccessful() {
    if (actual.isSuccessful()) {
      return myself;
    }

    Predicate<Kind> filter = actual.isWarningsAsErrors()
        ? kind -> kind == Kind.ERROR || kind == Kind.MANDATORY_WARNING || kind == Kind.WARNING
        : kind -> kind == Kind.ERROR;

    var errors = actual
        .getDiagnostics()
        .stream()
        .filter(it -> filter.test(it.getKind()))
        .collect(Collectors.toList());

    if (errors.isEmpty()) {
      throw failureWithActualExpected(
          "succeeded",
          "failed",
          "Expected successful compilation but it failed without any error diagnostics"
      );
    }

    throw failureWithActualExpected(
        "succeeded",
        "failed",
        "Expected successful compilation but it failed\nDiagnostics: %s",
        errors
    );
  }

  /**
   * Assert that the compilation succeeded without warnings or errors.
   *
   * @return this object.
   */
  public CompilationAssertions<C> isSuccessfulWithoutWarnings() {
    isSuccessful();
    warnings().withFailMessage("Expected no warnings").isEmpty();
    mandatoryWarnings().withFailMessage("Expected no mandatory warnings").isEmpty();
    return myself;
  }

  /**
   * Assert that the compilation failed.
   *
   * @return this object.
   */
  public CompilationAssertions<C> isAFailure() {
    if (actual.isFailure()) {
      return myself;
    }

    throw failureWithActualExpected(
        "failed",
        "succeeded",
        "Expected failed compilation but it succeeded"
    );
  }

  /**
   * Get assertions to perform on the collection of diagnostics that were returned.
   *
   * @return the assertions to perform across the diagnostics.
   */
  public DiagnosticListAssertions diagnostics() {
    return DiagnosticListAssertions.assertThat(actual.getDiagnostics());
  }

  /**
   * Get assertions to perform on all diagnostics that match the given predicate
   *
   * @param predicate the predicate to filter diagnostics by.
   * @return the assertions to perform across the filtered diagnostics.
   */
  public DiagnosticListAssertions diagnostics(
      Predicate<? super DiagnosticWithTrace<? extends JavaFileObject>> predicate
  ) {
    return actual
        .getDiagnostics()
        .stream()
        .filter(predicate)
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            DiagnosticListAssertions::assertThat
        ));
  }

  /**
   * Get assertions to perform on all error diagnostics.
   *
   * @return the assertions to perform across the error diagnostics.
   */
  public DiagnosticListAssertions errors() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.ERROR);
  }

  /**
   * Get assertions to perform on all warning diagnostics.
   *
   * @return the assertions to perform across the warning diagnostics.
   */
  public DiagnosticListAssertions warnings() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.WARNING);
  }

  /**
   * Get assertions to perform on all mandatory warning diagnostics.
   *
   * @return the assertions to perform across the warning diagnostics.
   */
  public DiagnosticListAssertions mandatoryWarnings() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.MANDATORY_WARNING);
  }

  /**
   * Get assertions to perform on all note diagnostics.
   *
   * @return the assertions to perform across the note diagnostics.
   */
  public DiagnosticListAssertions notes() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.NOTE);
  }

  /**
   * Get assertions to perform on all {@link Kind#OTHER}-kinded diagnostics.
   *
   * @return the assertions to perform across the {@link Kind#OTHER}-kinded diagnostics.
   */
  public DiagnosticListAssertions otherDiagnostics() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.OTHER);
  }

  /**
   * Get assertions to perform on the compiler log output.
   *
   * @return the assertions to perform on the compiler log output.
   */
  public ListAssert<String> outputLines() {
    return new ListAssert<>(actual.getOutputLines());
  }

  public PathLocationRepositoryAssertions files() {
    return PathLocationRepositoryAssertions.assertThat(actual.getFileRepository());
  }

  /**
   * Create a new assertion object.
   *
   * @param compilation the compilation to assert on.
   * @param <C>         the compilation type.
   * @return the compilation assertions to use.
   */
  public static <C extends Compilation> CompilationAssertions<C> assertThat(
      C compilation
  ) {
    return new CompilationAssertions<>(compilation);
  }
}
