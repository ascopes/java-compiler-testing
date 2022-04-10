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

package com.github.ascopes.jct.assertions;

import com.github.ascopes.jct.compilers.TraceDiagnostic;
import java.util.Locale;
import java.util.Objects;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.presentation.Representation;

/**
 * Representation of a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class DiagnosticRepresentation implements Representation {

  @Override
  public String toStringOf(Object object) {
    if (object == null) {
      return "null";
    }

    var diagnostic = (TraceDiagnostic<?>) object;

    var builder = new StringBuilder("[")
        .append(diagnostic.getKind())
        .append("] ");

    var code = diagnostic.getCode();
    if (code != null) {
      builder.append("<").append(code).append("> ");
    }

    builder
        .append("at line ")
        .append(diagnostic.getLineNumber())
        .append(", column ")
        .append(diagnostic.getColumnNumber());

    var source = diagnostic.getSource();

    if (source instanceof JavaFileObject) {
      builder.append(" in ").append(((JavaFileObject) source).getName());
    }

    builder.append(":");

    Objects
        .toString(diagnostic.getMessage(Locale.ROOT))
        .lines()
        .map("   "::concat)
        .forEach(line -> builder.append("\n").append(line));

    return builder.toString();
  }
}
