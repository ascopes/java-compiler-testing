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
package io.github.ascopes.jct.testing.unit.compilers.impl;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.compilers.impl.JavacJctCompilerImpl;
import io.github.ascopes.jct.compilers.impl.JavacJctFlagBuilderImpl;
import io.github.ascopes.jct.testing.helpers.MoreMocks;
import javax.tools.JavaCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link JavacJctCompilerImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavacJctCompilerImpl tests")
class JavacJctCompilerImplTest {

  @DisplayName("compilers have the expected default name")
  @Test
  void compilersHaveTheExpectedDefaultName() {
    assertThat(new JavacJctCompilerImpl(MoreMocks.stub(JavaCompiler.class)).getName())
        .isEqualTo("JDK Compiler");
  }

  @DisplayName("compilers have the expected JSR-199 compiler implementation")
  @Test
  void compilersHaveTheExpectedCompilerImplementation() {
    // Given
    var jsr199Compiler = MoreMocks.stub(JavaCompiler.class);

    // Then
    assertThat(new JavacJctCompilerImpl(jsr199Compiler).getJsr199Compiler())
        .isSameAs(jsr199Compiler);
  }

  @DisplayName("compilers have the expected flag builder")
  @Test
  void compilersHaveTheExpectedFlagBuilder() {
    assertThat(new JavacJctCompilerImpl(MoreMocks.stub(JavaCompiler.class)).getFlagBuilder())
        .isInstanceOf(JavacJctFlagBuilderImpl.class);
  }

  @DisplayName("compilers have the -implicit:class flag set")
  @Test
  void compilersHaveTheImplicitClassFlagSet() {
    var compiler = new JavacJctCompilerImpl(MoreMocks.stub(JavaCompiler.class));
    assertThat(compiler.getCompilerOptions())
        .contains("-implicit:class");
  }
}
