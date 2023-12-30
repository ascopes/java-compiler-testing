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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmClassPathModuleConfigurer;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.utils.ModuleDiscoverer;
import io.github.ascopes.jct.utils.ModuleDiscoverer.ModuleCandidate;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import java.util.List;
import java.util.Set;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
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

  @InjectMocks
  JctFileManagerJvmClassPathModuleConfigurer configurer;

  @DisplayName(
      ".configure(...) will configure the file manager with the modules from the JVM classpath"
  )
  @Test
  void configureAddsTheClassPathToTheFileManagerModulePath() {
    // Given
    try (
        var specialLocationUtilsStatic = mockStatic(SpecialLocationUtils.class);
        var moduleDiscovererStatic = mockStatic(ModuleDiscoverer.class)
    ) {

      var path1 = somePath();
      var modulePath1a = somePath();
      var module1a = new ModuleCandidate("module1a", modulePath1a, mock());
      var modulePath1b = somePath();
      var module1b = new ModuleCandidate("module1b", modulePath1b, mock());

      var path2 = somePath();

      var path3 = somePath();
      var modulePath3a = somePath();
      var module3a = new ModuleCandidate("module3a", modulePath3a, mock());
      var modulePath3b = somePath();
      var module3b = new ModuleCandidate("module3b", modulePath3b, mock());
      var modulePath3c = somePath();
      var module3c = new ModuleCandidate("module3c", modulePath3c, mock());

      var path4 = somePath();

      specialLocationUtilsStatic.when(SpecialLocationUtils::currentClassPathLocations)
          .thenReturn(List.of(path1, path2, path3, path4));
      moduleDiscovererStatic.when(() -> ModuleDiscoverer.findModulesIn(any()))
          .thenReturn(Set.of());
      moduleDiscovererStatic.when(() -> ModuleDiscoverer.findModulesIn(path1))
          .thenReturn(Set.of(module1a, module1b));
      moduleDiscovererStatic.when(() -> ModuleDiscoverer.findModulesIn(path3))
          .thenReturn(Set.of(module3a, module3b, module3c));

      // When
      configurer.configure(fileManager);

      // Then
      verify(fileManager).addPath(StandardLocation.MODULE_PATH, module1a.createPathRoot());
      verify(fileManager).addPath(StandardLocation.MODULE_PATH, module1b.createPathRoot());
      verify(fileManager).addPath(StandardLocation.MODULE_PATH, module3a.createPathRoot());
      verify(fileManager).addPath(StandardLocation.MODULE_PATH, module3b.createPathRoot());
      verify(fileManager).addPath(StandardLocation.MODULE_PATH, module3c.createPathRoot());
      verifyNoMoreInteractions(fileManager);
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
