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

package com.github.ascopes.jct.testing.unit.compilers;

import static com.github.ascopes.jct.compilers.Compilers.ecj;
import static com.github.ascopes.jct.compilers.Compilers.javac;
import static com.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;

import com.github.ascopes.jct.compilers.Compilers;
import com.github.ascopes.jct.compilers.ecj.EcjCompiler;
import com.github.ascopes.jct.compilers.javac.JavacCompiler;
import com.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import java.util.function.Supplier;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

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
    try (var staticMock = mockStatic(Compilers.class, Answers.CALLS_REAL_METHODS)) {
      // When
      var compiler = javac();

      // Then
      assertThat(compiler).isInstanceOf(JavacCompiler.class);
      staticMock.verify(() -> javac(givesSameTypeAs(ToolProvider::getSystemJavaCompiler)));
    }
  }

  @DisplayName("javac(Supplier<JavaCompiler>) returns the expected Javac compiler")
  @Test
  void javacReturnsExpectedJavacCompiler() {
    try (var compilerMock = mockStatic(JavacCompiler.class, Answers.CALLS_REAL_METHODS)) {
      // When
      var jsr199Compiler = stub(JavaCompiler.class);
      var compiler = javac(() -> jsr199Compiler);

      // Then
      assertThat(compiler).isInstanceOf(JavacCompiler.class);
      compilerMock.verify(() -> JavacCompiler.using(givesSameValueAs(jsr199Compiler)));
    }
  }

  @DisplayName("ecj() returns a default ECJ compiler")
  @Test
  void ecjReturnsDefaultEcjCompiler() {
    try (var staticMock = mockStatic(Compilers.class, Answers.CALLS_REAL_METHODS)) {
      // When
      var compiler = ecj();

      // Then
      assertThat(compiler).isInstanceOf(EcjCompiler.class);
      staticMock.verify(() -> ecj(givesSameTypeAs(EclipseCompiler::new)));
    }
  }

  @DisplayName("ecj(Supplier<JavaCompiler>) returns the expected ECJ compiler")
  @Test
  void ecjReturnsExpectedEcjCompiler() {
    try (var compilerMock = mockStatic(EcjCompiler.class, Answers.CALLS_REAL_METHODS)) {
      // When
      var jsr199Compiler = stub(JavaCompiler.class);
      var compiler = ecj(() -> jsr199Compiler);

      // Then
      assertThat(compiler).isInstanceOf(EcjCompiler.class);
      compilerMock.verify(() -> EcjCompiler.using(givesSameValueAs(jsr199Compiler)));
    }
  }

  private static <T> Supplier<T> givesSameTypeAs(Supplier<T> supplier) {
    return argThat(actualSupplier -> {
      assertThat(actualSupplier)
          .isNotNull()
          .extracting(Supplier::get)
          .isNotNull()
          .extracting(Object::getClass)
          .isEqualTo(supplier.get().getClass());
      return true;
    });
  }

  private static <T> Supplier<T> givesSameValueAs(T value) {
    return argThat(actualSupplier -> {
      assertThat(actualSupplier)
          .isNotNull()
          .extracting(Supplier::get)
          .isNotNull()
          .isEqualTo(value);
      return true;
    });
  }
}
