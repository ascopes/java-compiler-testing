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

package com.github.ascopes.jct.testing.unit.compilers.javac;

import static com.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.ascopes.jct.compilers.javac.JavacCompiler;
import com.github.ascopes.jct.compilers.javac.JavacFlagBuilder;
import javax.tools.JavaCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link JavacCompiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavacCompiler tests")
class JavacCompilerTest {
  @DisplayName("compilers have the expected name")
  @Test
  void compilersHaveTheExpectedName() {
    assertThat(new JavacCompiler(stub(JavaCompiler.class)).getName())
        .isEqualTo("javac");
  }

  @DisplayName("compilers have the expected JSR-199 compiler implementation")
  @Test
  void compilersHaveTheExpectedCompilerImplementation() {
    // Given
    var jsr199Compiler = stub(JavaCompiler.class);

    // Then
    assertThat(new JavacCompiler(jsr199Compiler).getJsr199Compiler())
        .isSameAs(jsr199Compiler);
  }

  @DisplayName("compilers have the expected flag builder")
  @Test
  void compilersHaveTheExpectedFlagBuilder() {
    assertThat(new JavacCompiler(stub(JavaCompiler.class)).getFlagBuilder())
        .isInstanceOf(JavacFlagBuilder.class);
  }
}
