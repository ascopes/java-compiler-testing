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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someAbsolutePath;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelativePath;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.assertions.PathFileObjectAssert;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link PathFileObjectAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PathFileObjectAssert tests")
class PathFileObjectAssertTest {

  @DisplayName("PathFileObjectAssert#relativePath tests")
  @Nested
  class RelativePathTest {

    @DisplayName(".relativePath() fails if the path file object is null")
    @Test
    void relativePathFailsIfThePathFileObjectIsNull() {
      // Given
      var assertions = new PathFileObjectAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::relativePath);
    }

    @DisplayName(".relativePath() returns assertions on the relative path")
    @Test
    void relativePathReturnsAssertionsOnTheRelativePath() {
      // Given
      var path = someRelativePath();
      var pathFileObject = mock(PathFileObject.class);
      when(pathFileObject.getRelativePath()).thenReturn(path);
      var assertions = new PathFileObjectAssert(pathFileObject);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.relativePath().isSameAs(path));
    }
  }

  @DisplayName("PathFileObjectAssert#absolutePath tests")
  @Nested
  class AbsolutePathTest {

    @DisplayName(".absolutePath() fails if the path file object is null")
    @Test
    void absolutePathFailsIfThePathFileObjectIsNull() {
      // Given
      var assertions = new PathFileObjectAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::absolutePath);
    }

    @DisplayName(".absolutePath() returns assertions on the absolute path")
    @Test
    void absolutePathReturnsAssertionsOnTheAbsolutePath() {
      // Given
      var path = someAbsolutePath();
      var pathFileObject = mock(PathFileObject.class);
      when(pathFileObject.getAbsolutePath()).thenReturn(path);
      var assertions = new PathFileObjectAssert(pathFileObject);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.absolutePath().isSameAs(path));
    }
  }
}
