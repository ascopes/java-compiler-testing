/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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

import java.util.List;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.presentation.Representation;

/**
 * Representation of a {@link List list} of {@link StackTraceElement stack trace frames}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class StackTraceRepresentation implements Representation {

  private static final StackTraceRepresentation INSTANCE = new StackTraceRepresentation();

  /**
   * Get an instance of this stack trace representation.
   *
   * @return the instance.
   */
  public static StackTraceRepresentation getInstance() {
    return INSTANCE;
  }

  private StackTraceRepresentation() {
    // Nothing to see here, move along now!
  }

  @Override
  @SuppressWarnings("unchecked")
  public String toStringOf(@Nullable Object object) {
    if (object == null) {
      return "null";
    }

    var trace = (List<? extends StackTraceElement>) object;
    var builder = new StringBuilder("Stacktrace:");
    for (var frame : trace) {
      builder.append("\n\tat ").append(frame);
    }
    return builder.toString();
  }
}
