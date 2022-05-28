package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import javax.tools.JavaFileObject;
import org.assertj.core.api.AbstractAssert;

public final class DiagnosticAssert
    extends AbstractAssert<DiagnosticAssert, TraceDiagnostic<? extends JavaFileObject>> {

  /**
   * Initialize this assertion type.
   *
   * @param value    the value to assert on.
   */
  public DiagnosticAssert(TraceDiagnostic<? extends JavaFileObject> value) {
    super(value, DiagnosticAssert.class);
  }
}
