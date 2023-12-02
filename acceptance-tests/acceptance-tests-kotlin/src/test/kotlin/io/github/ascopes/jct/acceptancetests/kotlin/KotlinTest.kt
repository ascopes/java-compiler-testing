/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.acceptancetests.kotlin

import io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.jupiter.api.BeforeEach

class KotlinTest {

  @BeforeEach
  fun setUp() {
    // Workaround for https://youtrack.jetbrains.com/issue/IDEA-317391
    assumeThat(System.getProperty("mvnArgLinePropagated", "false"))
        .withFailMessage("Your IDE has not propagated the <argLine/> in the pom.xml")
        .isEqualTo("true")
  }

  @JavacCompilerTest
  fun `I can compile a 'Hello, World!' application`(compiler: JctCompiler) {
    Workspaces.newWorkspace().use { workspace ->
      // Given
      workspace
          .createSourcePathPackage()
          .createFile("org", "example", "HelloWorld.java")
          .withContents(
            """
            package org.example;
            
            public class HelloWorld {
              public static void main(String[] args) {
                System.out.println("Hello, World!");
              }
            }
            """
          )

      // When
      val compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation).isSuccessful
    }
  }
}
