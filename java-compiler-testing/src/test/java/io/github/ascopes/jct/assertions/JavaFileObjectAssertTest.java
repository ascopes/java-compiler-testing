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
package io.github.ascopes.jct.assertions;

import static io.github.ascopes.jct.fixtures.Fixtures.someJavaFileObject;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link JavaFileObjectAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavaFileObjectAssert tests")
class JavaFileObjectAssertTest {

  @DisplayName("Assertions are performed on the JavaFileObject")
  @Test
  void assertionsArePerformedOnTheJavaFileObject() {
    // Given
    var javaFileObject = someJavaFileObject();

    // When
    var assertions = new JavaFileObjectAssert(javaFileObject);

    // Then
    assertions
        .isSameAs(javaFileObject);
    assertThat(assertions)
        .isInstanceOf(AbstractJavaFileObjectAssert.class);
  }
}
