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
package io.github.ascopes.jct.acceptancetests.errorprone;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.Assumptions.assumeThat;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.junit.JctExtension;
import io.github.ascopes.jct.junit.Managed;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("Error-prone acceptance tests")
@ExtendWith(JctExtension.class)
class ErrorProneTest {

  // Use tempdirectories as ErrorProne isn't compatible with the URLs memoryfilesystem emits. This
  // is due to the path being left null in the implementation.
  @Managed(pathStrategy = PathStrategy.TEMP_DIRECTORIES)
  Workspace workspace;

  @BeforeAll
  static void workAroundIdea317391() {
    // Workaround for https://youtrack.jetbrains.com/issue/IDEA-317391
    assumeThat(System.getProperty("mvnArgLinePropagated", "false"))
        .withFailMessage("Your IDE has not propagated the <argLine/> in the pom.xml")
        .isEqualTo("true");
  }

  @DisplayName("Happy paths work as expected")
  @JavacCompilerTest
  void happyPathsWorkAsExpected(JctCompiler compiler) {
    // Given
    workspace
        .createSourcePathPackage()
        .createDirectory("org", "example")
        .copyContentsFrom("src", "test", "resources", "code", "nullness", "happy");

    // When
    var compilation = compiler
        .addCompilerOptions(
            "-Xplugin:ErrorProne",
            "-XDcompilePolicy=simple",
            "--should-stop=ifError=FLOW"
        )
        .compile(workspace);

    // Then
    assertThatCompilation(compilation).isSuccessfulWithoutWarnings();
  }

  @DisplayName("Sad paths fail as expected")
  @JavacCompilerTest
  void sadPathsFailAsExpected(JctCompiler compiler) {
    // Given
    workspace
        .createSourcePathPackage()
        .createDirectory("org", "example")
        .copyContentsFrom("src", "test", "resources", "code", "nullness", "sad");

    // When
    var compilation = compiler
        .addCompilerOptions(
            "-Xplugin:ErrorProne",
            "-XDcompilePolicy=simple",
            "--should-stop=ifError=FLOW"
        )
        .compile(workspace);

    // Then
    assertThatCompilation(compilation).isFailure()
        .diagnostics().errors().singleElement().message()
        .startsWith("[MustBeClosedChecker] This method returns a resource which must be managed "
                + "carefully, not just left for garbage collection.");
  }
}
