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
package io.github.ascopes.jct.tests.unit.assertions;

import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.assertions.ClassLoaderAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ClassLoaderAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ClassLoaderAssert tests")
class ClassLoaderAssertTest {
  @DisplayName("Assertions are performed on the classloader")
  @Test
  void assertionsArePerformedOnTheClassLoader() {
    // Given
    var classLoader = mock(ClassLoader.class);

    // When
    var assertions = new ClassLoaderAssert(classLoader);

    // Then
    assertions.isSameAs(classLoader);
  }
}
