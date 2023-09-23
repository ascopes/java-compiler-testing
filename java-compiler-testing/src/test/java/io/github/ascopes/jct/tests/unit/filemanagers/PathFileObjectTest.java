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
package io.github.ascopes.jct.tests.unit.filemanagers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.filemanagers.PathFileObject;
import javax.tools.FileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PathFileObject} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PathFileObject tests")
class PathFileObjectTest {

  @DisplayName(".upcast(FileObject) returns the object when it is a PathFileObject")
  @Test
  void upcastReturnsObjectWhenPathFileObject() {
    // Given
    var fileObject = mock(PathFileObject.class);

    // When
    var upcast = PathFileObject.upcast(fileObject);

    // Then
    assertThat(upcast)
        .isPresent()
        .get()
        .isSameAs(fileObject);
  }

  @DisplayName(".upcast(FileObject) returns empty when it is not a PathFileObject")
  @Test
  void upcastReturnsEmptyWhenNotPathFileObject() {
    // Given
    var fileObject = mock(FileObject.class);

    // When
    var upcast = PathFileObject.upcast(fileObject);

    // Then
    assertThat(upcast).isEmpty();
  }
}
