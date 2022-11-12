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
package io.github.ascopes.jct.testing.helpers;

import static io.github.ascopes.jct.testing.helpers.GenericMock.mockRaw;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Commonly used mock fixtures.
 *
 * @author Ashley Scopes
 */
public final class Fixtures {

  private Fixtures() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Get a diagnostic mock.
   *
   * @return the mock.
   */
  public static Diagnostic<JavaFileObject> someDiagnostic() {
    return mockRaw(Diagnostic.class)
        .<Diagnostic<JavaFileObject>>upcastedTo()
        .build();
  }

  /**
   * Get a tracee diagnostic mock.
   *
   * @return the mock.
   */
  public static TraceDiagnostic<JavaFileObject> someTraceDiagnostic() {
    return mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();
  }

  /**
   * Get a stack trace element list mock.
   *
   * @return the mock.
   */
  public static List<StackTraceElement> someStackTraceList() {
    return mockRaw(List.class)
        .<List<StackTraceElement>>upcastedTo()
        .build();
  }
}
