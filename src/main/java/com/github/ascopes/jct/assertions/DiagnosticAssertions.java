package com.github.ascopes.jct.assertions;

import com.github.ascopes.jct.diagnostics.DiagnosticWithTrace;
import javax.tools.JavaFileObject;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.InstantAssert;

/**
 * Assertions to apply to a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class DiagnosticAssertions
    extends
    AbstractObjectAssert<DiagnosticAssertions, DiagnosticWithTrace<? extends JavaFileObject>> {

  /**
   * Initialize this set of assertions.
   *
   * @param diagnosticWithTrace the diagnostic to assert upon.
   */
  private DiagnosticAssertions(DiagnosticWithTrace<? extends JavaFileObject> diagnosticWithTrace) {
    super(diagnosticWithTrace, DiagnosticAssertions.class);
  }

  public InstantAssert timestamp() {
    return new InstantAssert(actual.getTimestamp());
  }

  /**
   * Create a new set of assertions for a specific diagnostic.
   *
   * @param diagnostic the diagnostic to assert on.
   * @param <S>        the type of object within the diagnostic.
   * @return the assertions.
   */
  public static <S> DiagnosticAssertions assertThat(
      DiagnosticWithTrace<? extends JavaFileObject> diagnostic
  ) {
    return new DiagnosticAssertions(diagnostic);
  }
}

