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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerLoggingProxyConfigurer;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.filemanagers.impl.LoggingFileManagerProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerLoggingProxyConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerLoggingProxyConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerLoggingProxyConfigurerTest {

  @Mock
  JctCompiler compiler;

  @Mock
  JctFileManager proxiedFileManager;

  @Mock
  JctFileManagerImpl fileManager;

  @InjectMocks
  JctFileManagerLoggingProxyConfigurer configurer;

  @DisplayName(".configure(...) will wrap the file manager in a proxy")
  @CsvSource({
      "STACKTRACES, true",
      "ENABLED, false",
  })
  @ParameterizedTest(
      name = ".configure(...) for logging mode {0} will initialise a proxy with stacktraces = {0}"
  )
  void configureWillCopyAllWorkspacePathsToTheFileManager(LoggingMode mode, boolean stacktraces) {
    // Given
    try (var loggingFileManagerProxyStatic = mockStatic(LoggingFileManagerProxy.class)) {
      loggingFileManagerProxyStatic.when(() -> LoggingFileManagerProxy.wrap(any(), anyBoolean()))
          .thenReturn(proxiedFileManager);

      when(compiler.getFileManagerLoggingMode()).thenReturn(mode);

      // When
      configurer.configure(fileManager);

      // Then
      loggingFileManagerProxyStatic
          .verify(() -> LoggingFileManagerProxy.wrap(fileManager, stacktraces));
    }
  }

  @DisplayName(".configure(...) will raise an IllegalStateException if logging is disabled")
  @Test
  void configureThrowsIllegalStateExceptionIfLoggingDisabled() {
    // Given
    when(compiler.getFileManagerLoggingMode()).thenReturn(LoggingMode.DISABLED);

    // Then
    assertThatThrownBy(() -> configurer.configure(fileManager))
        .isInstanceOf(IllegalStateException.class);
  }

  @DisplayName(".configure(...) returns the proxied file manager")
  @EnumSource(value = LoggingMode.class, names = "DISABLED", mode = Mode.EXCLUDE)
  @ParameterizedTest(name = "for logging mode = {0}")
  void configureReturnsTheProxiedFileManager(LoggingMode mode) {
    // Given
    try (var loggingFileManagerProxyStatic = mockStatic(LoggingFileManagerProxy.class)) {
      loggingFileManagerProxyStatic.when(() -> LoggingFileManagerProxy.wrap(any(), anyBoolean()))
          .thenReturn(proxiedFileManager);
      when(compiler.getFileManagerLoggingMode()).thenReturn(mode);

      // When
      var result = configurer.configure(fileManager);

      // Then
      assertThat(result)
          .isNotSameAs(fileManager)
          .isSameAs(proxiedFileManager);
    }
  }

  @DisplayName(".isEnabled() returns the expected result")
  @CsvSource({
      "STACKTRACES, true",
      "ENABLED, true",
      "DISABLED, false",
  })
  @ParameterizedTest(name = "returns {1} when logging mode is {0}")
  void isEnabledReturnsExpectedResult(LoggingMode mode, boolean enabled) {
    // Given
    when(compiler.getFileManagerLoggingMode()).thenReturn(mode);

    // Then
    assertThat(configurer.isEnabled()).isEqualTo(enabled);
  }
}
