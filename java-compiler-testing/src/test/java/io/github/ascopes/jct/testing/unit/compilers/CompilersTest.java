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

package io.github.ascopes.jct.testing.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockConstructionWithAnswer;

import io.github.ascopes.jct.compilers.Compilers;
import io.github.ascopes.jct.compilers.ecj.EcjCompiler;
import io.github.ascopes.jct.compilers.javac.JavacCompiler;
import io.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Compilers}.
 *
 * @author Ashley Scopes
 */
@DisplayName("Compilers tests")
class CompilersTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return Compilers.class;
  }

  @DisplayName("javac() returns a default Javac compiler")
  @Test
  void javacReturnsDefaultJavacCompiler() {
    var javacCompilerMock = mockConstructionWithAnswer(JavacCompiler.class, CALLS_REAL_METHODS);

    try (javacCompilerMock) {
      // When
      var compiler = Compilers.javac();

      // Then
      assertThat(javacCompilerMock.constructed())
          .singleElement()
          .isSameAs(compiler);
    }
  }

  @DisplayName("ecj() returns a default ECJ compiler")
  @Test
  void ecjReturnsDefaultEcjCompiler() {
    var ecjCompilerMock = mockConstructionWithAnswer(EcjCompiler.class, CALLS_REAL_METHODS);

    try (ecjCompilerMock) {
      // When
      var compiler = Compilers.ecj();

      // Then
      assertThat(ecjCompilerMock.constructed())
          .singleElement()
          .isSameAs(compiler);
    }
  }

}
