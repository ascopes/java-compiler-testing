/*
 * Copyright (C) 2022 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.repr;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import java.util.Locale;
import org.assertj.core.presentation.Representation;
import org.jspecify.annotations.Nullable;

/**
 * Simplified representation of a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1, rewritten in 4.0.0
 */
public final class TraceDiagnosticRepresentation implements Representation {

  private static final TraceDiagnosticRepresentation INSTANCE
      = new TraceDiagnosticRepresentation();

  /**
   * Get an instance of this diagnostic representation.
   *
   * @return the instance.
   */
  public static TraceDiagnosticRepresentation getInstance() {
    return INSTANCE;
  }

  private TraceDiagnosticRepresentation() {
    // Nothing to see here, move along now.
  }

  @Override
  public String toStringOf(@Nullable Object object) {
    if (object == null) {
      return "null";
    }

    var diagnostic = (TraceDiagnostic<?>) object;

    var builder = new StringBuilder("[")
        .append(diagnostic.getKind())
        .append("]");

    var code = diagnostic.getCode();
    if (code != null) {
      builder.append(' ')
          .append(code);
    }

    if (diagnostic.getSource() != null) {
      builder
          .append(" in ")
          .append(diagnostic.getSource().getName())
          .append(" (line ")
          .append(diagnostic.getLineNumber())
          .append(", col ")
          .append(diagnostic.getColumnNumber())
          .append(")");
    }

    return builder
        .append("\n")
        .append("\n")
        .append(diagnostic.getMessage(Locale.ROOT))
        .toString();
  }
}
