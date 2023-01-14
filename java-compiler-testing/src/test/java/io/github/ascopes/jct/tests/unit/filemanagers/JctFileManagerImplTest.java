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

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.workspaces.PathRoot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import javax.tools.JavaFileManager.Location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JctFileManager Tests")
class JctFileManagerImplTest {

  @Test
  @DisplayName("generates JctFileManager instance for a release")
  void testGettingJctFileManagerImplInstance() {
    assertThat(JctFileManagerImpl.forRelease("test")).isInstanceOf(JctFileManagerImpl.class);
  }

  @Test
  @DisplayName("null release is disallowed")
  void testIfNullPointerExceptionThrownIfReleaseNull() {
    assertThatThrownBy(() -> {
      JctFileManagerImpl.forRelease(null);
    }).isInstanceOf(NullPointerException.class)
        .hasMessage("release");
  }

  @Test
  @DisplayName("adds package location to JctFileManager")
  void testAddPathForPackageLocation() {
    var packageLocation = mock(Location.class);
    var pathRoot = mock(PathRoot.class);
    var path = mock(Path.class);

    // we mock path because it is needed by AbstractPackageContainerGroup
    given(pathRoot.getPath()).willReturn(path);

    var jctFileManager = JctFileManagerImpl.forRelease("test");
    jctFileManager.addPath(packageLocation, pathRoot);
    assertThat(jctFileManager.hasLocation(packageLocation)).isEqualTo(true);
  }

  @Test
  @DisplayName("adds output location to JctFileManager")
  void testAddPathForOutputLocation() {
    var outputLocation = mock(Location.class);
    var pathRoot = mock(PathRoot.class);
    var path = mock(Path.class);

    // we mock path because it is needed by AbstractPackageContainerGroup
    given(pathRoot.getPath()).willReturn(path);
    given(outputLocation.isOutputLocation()).willReturn(true);

    var jctFileManager = JctFileManagerImpl.forRelease("test");
    jctFileManager.addPath(outputLocation, pathRoot);
    assertThat(jctFileManager.hasLocation(outputLocation)).isEqualTo(true);
  }

}
