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

package io.github.ascopes.jct.testing.unit.compilers.ecj;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.compilers.ecj.EcjCompiler;
import io.github.ascopes.jct.compilers.ecj.EcjFlagBuilder;
import io.github.ascopes.jct.testing.helpers.MoreMocks;
import javax.tools.JavaCompiler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EcjCompiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("EcjCompiler tests")
class EcjCompilerTest {

  @DisplayName("compilers have the expected default name")
  @Test
  void compilersHaveTheExpectedDefaultName() {
    Assertions.assertThat(new EcjCompiler(MoreMocks.stub(JavaCompiler.class)).getName())
        .isEqualTo("Eclipse Compiler for Java");
  }

  @DisplayName("compilers have the expected JSR-199 compiler implementation")
  @Test
  void compilersHaveTheExpectedCompilerImplementation() {
    // Given
    var jsr199Compiler = MoreMocks.stub(JavaCompiler.class);

    // Then
    Assertions.assertThat(new EcjCompiler(jsr199Compiler).getJsr199Compiler())
        .isSameAs(jsr199Compiler);
  }

  @DisplayName("compilers have the expected flag builder")
  @Test
  void compilersHaveTheExpectedFlagBuilder() {
    assertThat(new EcjCompiler(MoreMocks.stub(JavaCompiler.class)).getFlagBuilder())
        .isInstanceOf(EcjFlagBuilder.class);
  }
}
