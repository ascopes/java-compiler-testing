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
import java.util.regex.Pattern;
import javax.tools.FileObject;
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

  // Pattern that matches the toString output of java.lang.Object when it is not overridden.
  // We use this to deal with the fact some ECJ diagnostics do not provide a useful toString,
  // while javac provides a pretty toString including a nice little code snippet.
  private static final Pattern NO_TO_STRING = Pattern.compile(
      "^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*@[A-Za-z0-9]+$"
  );

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
      builder.append(code);
    }

    if (diagnostic.getSource() instanceof FileObject) {
      var name = ((FileObject) diagnostic.getSource()).getName();
      builder.append(" ").append(name);
    }

    builder
        .append(" (at line ")
        .append(diagnostic.getLineNumber())
        .append(", col ")
        .append(diagnostic.getColumnNumber())
        .append(")");

    var message = diagnostic.toString();

    if (NO_TO_STRING.matcher(message).matches()) {
      message = Objects.toString(diagnostic.getMessage(Locale.ROOT));
    }

    message
        .lines()
        .map("   "::concat)
        .forEach(line -> builder.append("\n").append(line));

    return builder.toString();
  }
}
