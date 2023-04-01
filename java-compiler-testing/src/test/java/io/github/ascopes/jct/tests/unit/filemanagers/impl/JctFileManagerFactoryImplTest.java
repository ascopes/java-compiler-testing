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
package io.github.ascopes.jct.tests.unit.filemanagers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerAnnotationProcessorClassPathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerConfigurerChain;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmClassPathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmClassPathModuleConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmModulePathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmPlatformClassPathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmSystemModulesConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerLoggingProxyConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerRequiredLocationsConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerWorkspaceConfigurer;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerFactoryImpl;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import io.github.ascopes.jct.workspaces.Workspace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

/**
 * {@link JctFileManagerFactoryImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerFactoryImpl test")
@ExtendWith(MockitoExtension.class)
class JctFileManagerFactoryImplTest {

  @Mock(answer = Answers.RETURNS_MOCKS)
  JctCompiler<?, ?> compiler;

  @Mock(answer = Answers.RETURNS_MOCKS)
  Workspace workspace;

  @InjectMocks
  @Spy
  JctFileManagerFactoryImpl factory;

  @DisplayName("Created file managers use the effective release")
  @Test
  void createdFileManagersUseTheEffectiveRelease() {
    // Given
    try (var ignored = configurerChainMock()) {
      var release = Fixtures.someRelease();
      when(compiler.getEffectiveRelease()).thenReturn(release);

      // When
      var fileManager = factory.createFileManager(workspace);

      // Then
      assertThat(fileManager.getEffectiveRelease())
          .isEqualTo(release);
    }
  }

  @DisplayName("Created file managers are transformed by the configurer chain")
  @Test
  void createdFileManagersAreTransformedByTheConfigurerChain() {
    // Given
    var expectedFileManager = mock(JctFileManager.class);

    try (
        var chainCls = configurerChainMock(ctx -> expectedFileManager);
        var managerCls = fileManagerMock()
    ) {
      // When
      var fileManager = factory.createFileManager(workspace);

      // Then
      verify(factory).createConfigurerChain(workspace);

      assertThat(managerCls.constructed())
          .withFailMessage("Expected 1 file manager to be initialized")
          .hasSize(1);

      assertThat(chainCls.constructed())
          .singleElement()
          .satisfies(chain -> verify(chain).configure(managerCls.constructed().get(0)));

      assertThat(fileManager).isSameAs(expectedFileManager);
    }
  }

  @DisplayName("The configurer chain uses the expected configurers")
  @Test
  @SuppressWarnings("removal")
  void createdFileManagersAreReturnedAsTheResultFromTheConfigurerChain() {
    // When
    var configurerChain = factory.createConfigurerChain(workspace);

    // Then
    assertThat(configurerChain.list())
        .map(JctFileManagerConfigurer::getClass)
        .map(Class.class::cast)
        .containsExactly(
            JctFileManagerWorkspaceConfigurer.class,
            JctFileManagerJvmClassPathConfigurer.class,
            JctFileManagerJvmClassPathModuleConfigurer.class,
            JctFileManagerJvmModulePathConfigurer.class,
            JctFileManagerJvmPlatformClassPathConfigurer.class,
            JctFileManagerJvmSystemModulesConfigurer.class,
            JctFileManagerAnnotationProcessorClassPathConfigurer.class,
            JctFileManagerRequiredLocationsConfigurer.class,
            JctFileManagerLoggingProxyConfigurer.class
        );
  }

  static MockedConstruction<JctFileManagerImpl> fileManagerMock() {
    return mockConstruction(JctFileManagerImpl.class, withSettings().defaultAnswer(RETURNS_MOCKS));
  }

  static MockedConstruction<JctFileManagerConfigurerChain> configurerChainMock() {
    return configurerChainMock(input -> input.getArgument(0));
  }

  static MockedConstruction<JctFileManagerConfigurerChain> configurerChainMock(
      Answer<JctFileManager> configureResult
  ) {
    return mockConstruction(
        JctFileManagerConfigurerChain.class,
        withSettings()
            .defaultAnswer(Answers.RETURNS_SELF)
            .strictness(Strictness.LENIENT),
        (instance, ctx) -> when(instance.configure(any())).then(configureResult)
    );
  }
}
