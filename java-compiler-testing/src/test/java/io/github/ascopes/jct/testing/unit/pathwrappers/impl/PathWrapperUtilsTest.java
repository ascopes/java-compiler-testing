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
package io.github.ascopes.jct.testing.unit.pathwrappers.impl;

import static io.github.ascopes.jct.pathwrappers.impl.PathWrapperUtils.retrieveRequiredUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.jimfs.Jimfs;
import io.github.ascopes.jct.pathwrappers.impl.PathWrapperUtils;
import io.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PathWrapperUtils}.
 *
 * @author Ashley Scopes
 */
@DisplayName("PathWrapperUtils tests")
class PathWrapperUtilsTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return PathWrapperUtils.class;
  }

  @DisplayName("Supported paths can be dereferenced to URLs successfully")
  @Test
  void canDereferencePathsToUrls() throws IOException {
    // Given
    try (var fs = Jimfs.newFileSystem("PathWrapperUtils-test-fs")) {
      Files.createDirectories(fs.getPath("foo", "bar"));
      var file = Files.createFile(fs.getPath("foo", "bar", "baz.txt"));

      // When
      var url = retrieveRequiredUrl(file);

      // Then
      assertThat(url)
          .isNotNull()
          .isEqualTo(file.toUri().toURL())
          .asString()
          .startsWith(Jimfs.URI_SCHEME + "://");
    }
  }

  @DisplayName("Unsupported paths throw an IllegalArgumentException when using an unknown scheme")
  @Test
  void throwsIllegalArgumentExceptionIfPathUsesUnknownUriScheme() {
    // Given
    var uri = URI.create("some-crazy-random-scheme://foo/bar/baz.txt");
    var path = mock(Path.class);
    when(path.toUri()).thenReturn(uri);

    // Then
    assertThatThrownBy(() -> retrieveRequiredUrl(path))
        .isInstanceOf(IllegalArgumentException.class)
        .hasCauseInstanceOf(MalformedURLException.class);
  }
}
