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
package io.github.ascopes.jct.testing.unit.containers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.containers.ModuleHandle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link ModuleHandle} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ModuleHandle tests")
class ModuleHandleTest {

  @DisplayName("tryExtract throws an IllegalArgumentException for names leading with a '/'")
  @ValueSource(strings = {
      "/",
      "/foo",
      "/HelloWorld",
      "/java.base/java.lang.Integer"
  })
  @ParameterizedTest(name = "tryExtract throws an IllegalArgumentException for \"{0}\"")
  void tryExtractThrowsIllegalArgumentExceptionForLeadingSlash(String original) {
    // Then
    assertThatThrownBy(() -> ModuleHandle.tryExtract(original))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Absolute paths are not supported (got '%s')", original);
  }

  @DisplayName("tryExtract returns empty if no module prefix is found")
  @ValueSource(strings = {
      "",
      "foo",
      "HelloWorld",
      "org.example.HelloWorld"
  })
  @ParameterizedTest(name = "tryExtract returns null for \"{0}\"")
  void tryExtractReturnsNullIfNoModuleHandleIsFound(String original) {
    // When
    var prefix = ModuleHandle.tryExtract(original);

    // Then
    assertThat(prefix).isNull();
  }

  @DisplayName("tryExtract stores the original input in the result")
  @Test
  void tryExtractStoresTheOriginalInputInTheResult() {
    // Given
    var original = "net.bytebuddy/net.bytebuddy.ByteBuddy";

    // When
    var prefix = ModuleHandle.tryExtract(original);

    // Then
    assertThat(prefix)
        .isNotNull()
        .extracting(ModuleHandle::getOriginal)
        .describedAs("ModuleHandle#getOriginal")
        .asString()
        .isEqualTo(original);
  }

  @DisplayName("tryExtract gets the expected module name for valid modules")
  @CsvSource({
      "java.base/, java.base",
      "java.base/java.lang.Integer, java.base",
      "java.instrument/sun.instrument.TransformerManager, java.instrument",
      "net.bytebuddy/net.bytebuddy.ByteBuddy, net.bytebuddy",
      "java.base/module-info, java.base",
      "foo/org.example.something.Some$Nested$Clazz, foo",
  })
  @ParameterizedTest(name = "tryExtract(\"{0}\") extracts the moduleName as \"{1}\"")
  void tryExtractExtractsTheModuleName(String original, String expected) {
    // Then
    assertThat(ModuleHandle.tryExtract(original))
        .isNotNull()
        .extracting(ModuleHandle::getModuleName)
        .describedAs("ModuleHandle#getModuleName")
        .asString()
        .isEqualTo(expected);
  }

  @DisplayName("tryExtract gets the expected rest of the name for valid modules")
  @CsvSource({
      "java.base/, ''",
      "java.base/java.lang.Integer, java.lang.Integer",
      "java.instrument/sun.instrument.TransformerManager, sun.instrument.TransformerManager",
      "net.bytebuddy/net.bytebuddy.ByteBuddy, net.bytebuddy.ByteBuddy",
      "java.base/module-info, module-info",
      "foo/org.example.something.Some$Nested$Clazz, org.example.something.Some$Nested$Clazz",
  })
  @ParameterizedTest(name = "tryExtract(\"{0}\") extracts the rest as \"{1}\"")
  void tryExtractExtractsTheRest(String original, String expected) {
    // Then
    assertThat(ModuleHandle.tryExtract(original))
        .isNotNull()
        .extracting(ModuleHandle::getRest)
        .describedAs("ModuleHandle#getRest")
        .asString()
        .isEqualTo(expected);
  }
}
