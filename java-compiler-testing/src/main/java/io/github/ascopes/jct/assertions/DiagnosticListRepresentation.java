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

package io.github.ascopes.jct.assertions;

import static java.util.stream.Collectors.joining;

import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import java.util.Collection;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.presentation.Representation;

/**
 * Representation of a collection of diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class DiagnosticListRepresentation implements Representation {

  private static final DiagnosticListRepresentation INSTANCE
      = new DiagnosticListRepresentation();

  /**
   * Get an instance of this diagnostic collection representation.
   *
   * @return the instance.
   */
  public static DiagnosticListRepresentation getInstance() {
    return INSTANCE;
  }

  private DiagnosticListRepresentation() {
    // Nothing to see here, move along now.
  }

  @Override
  public String toStringOf(Object object) {
    if (object == null) {
      return "null";
    }

    @SuppressWarnings("unchecked")
    var diagnostics = (Collection<? extends TraceDiagnostic<? extends JavaFileObject>>) object;

    return diagnostics
        .stream()
        .map(DiagnosticRepresentation.getInstance()::toStringOf)
        .map(" - "::concat)
        .collect(joining("\n\n"));
  }
}
