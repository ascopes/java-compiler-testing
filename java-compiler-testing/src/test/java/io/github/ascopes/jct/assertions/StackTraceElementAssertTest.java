/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link StackTraceElementAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("StackTraceElementAssert tests")
class StackTraceElementAssertTest {

  @DisplayName("StackTraceElementAssert.fileName(...) tests")
  @Nested
  class FileNameTest {

    @DisplayName(".fileName() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::fileName)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".fileName() returns assertions on the file name")
    @Test
    void returnsAssertionsOnTheFileName() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getFileName()).thenReturn("some-file-name.java");

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.fileName().isEqualTo("some-file-name.java"))
          .doesNotThrowAnyException();

      assertThat(assertions.fileName().descriptionText())
          .isEqualTo("file name");
    }
  }

  @DisplayName("StackTraceElementAssert.lineNumber(...) tests")
  @Nested
  class LineNumberTest {

    @DisplayName(".lineNumber() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::lineNumber)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".lineNumber() returns assertions on the line number")
    @Test
    void returnsAssertionsOnTheLineNumber() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getLineNumber()).thenReturn(1_366_768);

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.lineNumber().isEqualTo(1_366_768))
          .doesNotThrowAnyException();

      assertThat(assertions.lineNumber().descriptionText())
          .isEqualTo("line number");
    }
  }

  @DisplayName("StackTraceElementAssert.moduleName(...) tests")
  @Nested
  class ModuleNameTest {

    @DisplayName(".moduleName() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::moduleName)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".moduleName() returns assertions on the module name")
    @Test
    void returnsAssertionsOnTheModuleName() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getModuleName()).thenReturn("org.example.potatofarmer");

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.moduleName().isEqualTo("org.example.potatofarmer"))
          .doesNotThrowAnyException();

      assertThat(assertions.moduleName().descriptionText())
          .isEqualTo("module name");
    }
  }

  @DisplayName("StackTraceElementAssert.moduleVersion(...) tests")
  @Nested
  class ModuleVersionTest {

    @DisplayName(".moduleVersion() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::moduleVersion)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".moduleVersion() returns assertions on the module version")
    @Test
    void returnsAssertionsOnTheModuleVersion() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getModuleVersion()).thenReturn("v1.2.3.4");

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.moduleVersion().isEqualTo("v1.2.3.4"))
          .doesNotThrowAnyException();

      assertThat(assertions.moduleVersion().descriptionText())
          .isEqualTo("module version");
    }
  }

  @DisplayName("StackTraceElementAssert.classLoaderName(...) tests")
  @Nested
  class ClassLoaderNameTest {

    @DisplayName(".classLoaderName() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::classLoaderName)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".classLoaderName() returns assertions on the class loader name")
    @Test
    void returnsAssertionsOnTheClassLoaderName() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getClassLoaderName()).thenReturn("rubbish classloader");

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.classLoaderName().isEqualTo("rubbish classloader"))
          .doesNotThrowAnyException();

      assertThat(assertions.classLoaderName().descriptionText())
          .isEqualTo("class loader name");
    }
  }

  @DisplayName("StackTraceElementAssert.className(...) tests")
  @Nested
  class ClassNameTest {

    @DisplayName(".className() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::className)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".className() returns assertions on the class name")
    @Test
    void returnsAssertionsOnTheClassName() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getClassName()).thenReturn("org.example.NuclearBomb");

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.className().isEqualTo("org.example.NuclearBomb"))
          .doesNotThrowAnyException();

      assertThat(assertions.className().descriptionText())
          .isEqualTo("class name");
    }
  }

  @DisplayName("StackTraceElementAssert.methodName(...) tests")
  @Nested
  class MethodNameTest {

    @DisplayName(".methodName() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::methodName)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".methodName() returns assertions on the method name")
    @Test
    void returnsAssertionsOnTheMethodName() {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.getMethodName()).thenReturn("explode");

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.methodName().isEqualTo("explode"))
          .doesNotThrowAnyException();

      assertThat(assertions.methodName().descriptionText())
          .isEqualTo("method name");
    }
  }

  @DisplayName("StackTraceElementAssert.nativeMethod(...) tests")
  @Nested
  class NativeMethodTest {

    @DisplayName(".nativeMethod() fails if the stack trace element is null")
    @Test
    void failsIfStackTraceElementIsNull() {
      // Given
      var assertions = new StackTraceElementAssert(null);

      // Then
      assertThatThrownBy(assertions::nativeMethod)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".nativeMethod() returns assertions on the native method")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "when isNativeMethod() returns {0}")
    void returnsAssertionsOnTheNativeMethod(boolean nativeMethod) {
      // Given
      var element = mock(StackTraceElement.class);
      when(element.isNativeMethod()).thenReturn(nativeMethod);

      var assertions = new StackTraceElementAssert(element);

      // Then
      assertThatCode(() -> assertions.nativeMethod().isEqualTo(nativeMethod))
          .doesNotThrowAnyException();

      assertThat(assertions.nativeMethod().descriptionText())
          .isEqualTo("native method");
    }
  }
}
