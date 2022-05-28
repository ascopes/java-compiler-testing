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

package io.github.ascopes.jct.testing.helpers;

import org.opentest4j.TestAbortedException;

/**
 * Utilities for skipping common issues in tests.
 *
 * @author Ashley Scopes
 */
public final class Skipping {

  private Skipping() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Skip the test because ECJ fails to support modules correctly.
   */
  public static void becauseEcjFailsToSupportModulesCorrectly() {
    // FIXME(ascopes): Attempt to find the root cause of these issues.
    //
    // It appears to depend on passing the `--add-module` and `--module-source-paths` flags
    // on the command line, but I cannot seem to get this solution to work via the JSR-199 interface
    // itself.
    skip(
        "ECJ does not appear to currently support compiling nested module sources correctly from",
        "the JSR-199 API implementation. This appears to be down to how ECJ detects source modules",
        "as compiling the java-compiler-testing API itself using ECJ appears to create the same",
        "errors in IntelliJ IDEA.",
        "",
        "One can expect an error message such as the following if this test is executed:",
        "",
        "\tERROR in module-info.java (at line 3)",
        "\t\texports com.example;",
        "\t\t        ^^^^^^^^^^^",
        "\tThe package com.example does not exist or is empty"
    );
  }

  private static void skip(String... reasonLines) {
    var reason = "Test is skipped.\n\n" + String.join("\n", reasonLines);
    throw new TestAbortedException(reason.stripTrailing());
  }
}
