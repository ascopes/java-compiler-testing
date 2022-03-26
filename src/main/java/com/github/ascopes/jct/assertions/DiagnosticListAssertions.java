package com.github.ascopes.jct.assertions;

import com.github.ascopes.jct.diagnostics.DiagnosticWithTrace;
import java.util.List;
import javax.tools.JavaFileObject;
import org.assertj.core.api.FactoryBasedNavigableListAssert;

/**
 * Assertions to apply to a list of diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
//@formatter:off
public final class DiagnosticListAssertions
    extends FactoryBasedNavigableListAssert<
        DiagnosticListAssertions,
        List<DiagnosticWithTrace<? extends JavaFileObject>>,
        DiagnosticWithTrace<? extends JavaFileObject>,
        DiagnosticAssertions
    > {
//@formatter:on

  /**
   * Initialize these assertions.
   *
   * @param diagnostics the list of assertions to assert on.
   */
  private DiagnosticListAssertions(
      List<DiagnosticWithTrace<? extends JavaFileObject>> diagnostics) {
    super(diagnostics, DiagnosticListAssertions.class, DiagnosticAssertions::assertThat);
  }

  /**
   * Create a new set of assertions for a list of diagnostics.
   *
   * @param diagnostics the list of diagnostics to assert on.
   * @return the assertions.
   */
  public static DiagnosticListAssertions assertThat(
      List<DiagnosticWithTrace<? extends JavaFileObject>> diagnostics
  ) {
    return new DiagnosticListAssertions(diagnostics);
  }
}