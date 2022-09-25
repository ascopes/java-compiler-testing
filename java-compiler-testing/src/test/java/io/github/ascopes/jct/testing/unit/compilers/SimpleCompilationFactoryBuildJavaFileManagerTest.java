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
package io.github.ascopes.jct.testing.unit.compilers;

import static io.github.ascopes.jct.testing.helpers.MoreMocks.stringLike;
import static io.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import io.github.ascopes.jct.compilers.SimpleCompilationFactory;
import io.github.ascopes.jct.jsr199.FileManager;
import io.github.ascopes.jct.paths.RamPath;
import java.util.Optional;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code buildJavaFileManager} in {@link SimpleCompilationFactory}.
 *
 * @author Ashley Scopes
 */
@DisplayName("SimpleCompilationFactory buildJavaFileManager tests")
class SimpleCompilationFactoryBuildJavaFileManagerTest extends
    AbstractSimpleCompilationFactoryTest {

  @DisplayName("The default release should be set if no release or target is provided")
  @Test
  void theDefaultReleaseShouldBeSetIfNoReleaseOrTargetIsProvided() {
    // Given
    var fileManager = stub(FileManager.class);
    given(fileManagerTemplate.createFileManager(any()))
        .willReturn(fileManager);
    given(compiler.getRelease())
        .willReturn(Optional.empty());
    given(compiler.getTarget())
        .willReturn(Optional.empty());

    given(compiler.getDefaultRelease()).willReturn("v1.2.3.4.5.6");

    // When
    var compilation = execute();

    // Then
    then(compiler).should(atLeastOnce()).getDefaultRelease();
    then(fileManagerTemplate).should().createFileManager("v1.2.3.4.5.6");
    assertThat(compilation.getFileManager())
        .isSameAs(fileManager);
  }

  @DisplayName("The target should be set if no release is provided")
  @Test
  void theTargetShouldBeSetIfNoReleaseIsProvided() {
    // Given
    var fileManager = stub(FileManager.class);
    given(fileManagerTemplate.createFileManager(any()))
        .willReturn(fileManager);
    given(compiler.getRelease())
        .willReturn(Optional.empty());
    given(compiler.getTarget())
        .willReturn(Optional.of("1.2.3.4"));

    // When
    var compilation = execute();

    // Then
    then(compiler).should(atLeastOnce()).getTarget();
    then(compiler).should(never()).getDefaultRelease();
    then(fileManagerTemplate).should().createFileManager("1.2.3.4");
    assertThat(compilation.getFileManager())
        .isSameAs(fileManager);
  }

  @DisplayName("The release should be set if the release is provided")
  @Test
  void theReleaseShouldBeSet() {
    // Given
    var fileManager = stub(FileManager.class);
    given(fileManagerTemplate.createFileManager(any()))
        .willReturn(fileManager);
    given(compiler.getRelease())
        .willReturn(Optional.of("3.4.5.6"));

    // When
    var compilation = execute();

    // Then
    then(compiler).should(atLeastOnce()).getRelease();
    then(compiler).should(never()).getDefaultRelease();
    then(fileManagerTemplate).should().createFileManager("3.4.5.6");
    assertThat(compilation.getFileManager())
        .isSameAs(fileManager);
  }

  @DisplayName("CLASS_OUTPUT is not added if already registered")
  @Test
  void classOutputIsNotAddedIfAlreadyRegistered() {
    // Given
    var fileManager = mock(FileManager.class);
    given(fileManagerTemplate.createFileManager(any()))
        .willReturn(fileManager);
    given(fileManager.hasLocation(StandardLocation.CLASS_OUTPUT))
        .willReturn(true);

    // When
    execute();

    // Then
    then(fileManager).should(never()).addPath(eq(StandardLocation.CLASS_OUTPUT), any());
  }

  @DisplayName("CLASS_OUTPUT is added if not registered")
  @Test
  void classOutputIsAddedIfNotRegistered() {
    // Given
    try (var ramPathMock = mockStatic(RamPath.class)) {
      var ramPath = stub(RamPath.class);
      ramPathMock
          .when(() -> RamPath.createPath(any(), anyBoolean()))
          .thenReturn(ramPath);

      var fileManager = mock(FileManager.class);
      given(fileManagerTemplate.createFileManager(any()))
          .willReturn(fileManager);
      given(fileManager.hasLocation(StandardLocation.CLASS_OUTPUT))
          .willReturn(false);

      // When
      execute();

      // Then
      ramPathMock.verify(() -> RamPath.createPath(
          stringLike("^classes-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
          eq(true)
      ));
      then(fileManager).should().addPath(StandardLocation.CLASS_OUTPUT, ramPath);
    }
  }
}
