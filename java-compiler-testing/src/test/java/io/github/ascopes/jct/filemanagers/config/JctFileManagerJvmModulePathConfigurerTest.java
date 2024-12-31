/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.filemanagers.config;

import static io.github.ascopes.jct.fixtures.Fixtures.somePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.util.List;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerJvmModulePathConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerJvmModulePathConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerJvmModulePathConfigurerTest {

  @Mock
  JctCompiler compiler;

  @Mock
  JctFileManagerImpl fileManager;

  @InjectMocks
  JctFileManagerJvmModulePathConfigurer configurer;

  @DisplayName(".configure(...) will configure the file manager with the JVM modulepath")
  @Test
  void configureAddsTheModulePathToTheFileManager() {
    // Given
    try (var specialLocationUtilsStatic = mockStatic(SpecialLocationUtils.class)) {
      var paths = List.of(
          somePath(),
          somePath(),
          somePath(),
          somePath()
      );

      specialLocationUtilsStatic.when(SpecialLocationUtils::currentModulePathLocations)
          .thenReturn(paths);

      // When
      configurer.configure(fileManager);

      // Then
      var classPathCaptor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);

      verify(fileManager, times(paths.size()))
          .addPath(eq(StandardLocation.CLASS_PATH), classPathCaptor.capture());

      var modulePathCaptor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);

      verify(fileManager, times(paths.size()))
          .addPath(eq(StandardLocation.MODULE_PATH), modulePathCaptor.capture());

      assertSoftly(softly -> {
        softly.assertThat(classPathCaptor.getAllValues())
            .map(WrappingDirectoryImpl::getPath)
            .containsExactlyElementsOf(paths);

        softly.assertThat(modulePathCaptor.getAllValues())
            .map(WrappingDirectoryImpl::getPath)
            .containsExactlyElementsOf(paths);
      });
    }
  }

  @DisplayName(".configure(...) returns the input file manager")
  @Test
  void configureReturnsTheInputFileManager() {
    // When
    var result = configurer.configure(fileManager);

    // Then
    assertThat(result).isSameAs(fileManager);
  }

  @DisplayName(".isEnabled() returns the expected result")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when JctCompiler.isInheritModulePath() returns {0}")
  void isEnabledReturnsTheExpectedResult(boolean inheritModulePath) {
    // Given
    when(compiler.isInheritModulePath()).thenReturn(inheritModulePath);

    // Then
    assertThat(configurer.isEnabled()).isEqualTo(inheritModulePath);
  }
}
