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
package io.github.ascopes.jct.tests.unit.filemanagers.config;

import static io.github.ascopes.jct.tests.helpers.Fixtures.somePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmClassPathModuleConfigurer;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.util.List;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerJvmClassPathModuleConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerJvmClassPathModuleConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerJvmClassPathModuleConfigurerTest {

  @Mock(strictness = Strictness.LENIENT)
  JctCompiler compiler;

  @Mock
  JctFileManagerImpl fileManager;

  @Mock
  MockedStatic<SpecialLocationUtils> specialLocationUtilsStatic;

  @InjectMocks
  JctFileManagerJvmClassPathModuleConfigurer configurer;

  @DisplayName(
      ".configure(...) will configure the file manager with the modules from the JVM classpath"
  )
  @Test
  void configureAddsTheClassPathToTheFileManagerModulePath() {
    // Given
    var paths = List.of(
        somePath(),
        somePath(),
        somePath(),
        somePath(),
        somePath()
    );

    specialLocationUtilsStatic.when(SpecialLocationUtils::currentClassPathLocations)
        .thenReturn(paths);

    // When
    configurer.configure(fileManager);

    // Then
    var captor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);

    verify(fileManager, times(paths.size()))
        .addPath(eq(StandardLocation.MODULE_PATH), captor.capture());

    assertThat(captor.getAllValues())
        .map(WrappingDirectoryImpl::getPath)
        .containsExactlyElementsOf(paths);
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
  @CsvSource({
      " true, true ,  true",
      " true, false, false",
      "false,  true, false",
      "false, false, false",
  })
  @ParameterizedTest
  void isEnabledReturnsTheExpectedResult(
      boolean inheritClassPath,
      boolean fixModulePathMismatch,
      boolean expectedResult
  ) {
    // Given
    when(compiler.isInheritClassPath()).thenReturn(inheritClassPath);
    when(compiler.isFixJvmModulePathMismatch()).thenReturn(fixModulePathMismatch);

    // Then
    assertThat(configurer.isEnabled()).isEqualTo(expectedResult);
  }
}
