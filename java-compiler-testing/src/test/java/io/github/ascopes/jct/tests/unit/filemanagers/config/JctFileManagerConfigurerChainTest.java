/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerConfigurerChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

/**
 * {@link JctFileManagerConfigurerChain} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerConfigurerChain tests")
class JctFileManagerConfigurerChainTest {

  JctFileManagerConfigurerChain chain;

  @BeforeEach
  void setUp() {
    chain = new JctFileManagerConfigurerChain();
  }

  @DisplayName(".addFirst(...) prepends configurers")
  @Test
  void addFirstPrependsConfigurers() {
    // Given
    var configurer1 = mock(JctFileManagerConfigurer.class);
    var configurer2 = mock(JctFileManagerConfigurer.class);
    var configurer3 = mock(JctFileManagerConfigurer.class);
    var configurer4 = mock(JctFileManagerConfigurer.class);

    // When
    var actualConfigurers = chain
        .addFirst(configurer1)
        .addFirst(configurer2)
        .addFirst(configurer3)
        .addFirst(configurer4)
        .list();

    // Then
    assertThat(actualConfigurers)
        .containsExactly(configurer4, configurer3, configurer2, configurer1);
  }

  @DisplayName(".addFirst(...) appends configurers")
  @Test
  void addLastAppendsConfigurers() {
    // Given
    var configurer1 = mock(JctFileManagerConfigurer.class);
    var configurer2 = mock(JctFileManagerConfigurer.class);
    var configurer3 = mock(JctFileManagerConfigurer.class);
    var configurer4 = mock(JctFileManagerConfigurer.class);

    // When
    var actualConfigurers = chain
        .addLast(configurer1)
        .addLast(configurer2)
        .addLast(configurer3)
        .addLast(configurer4)
        .list();

    // Then
    assertThat(actualConfigurers)
        .containsExactly(configurer1, configurer2, configurer3, configurer4);
  }

  @DisplayName(".list() returns an immutable view")
  @Test
  void listReturnsAnImmutableView() {
    // Given
    var configurer1 = mock(JctFileManagerConfigurer.class);
    var configurer2 = mock(JctFileManagerConfigurer.class);
    var configurer3 = mock(JctFileManagerConfigurer.class);
    var configurer4 = mock(JctFileManagerConfigurer.class);

    // When, Then
    var actualConfigurers = chain
        .addFirst(configurer1)
        .addLast(configurer2)
        .addFirst(configurer3)
        .list();

    assertThat(actualConfigurers)
        .hasSize(3);

    assertThat(chain.addFirst(configurer4).list())
        .isNotSameAs(actualConfigurers)
        .hasSize(4);

    assertThatThrownBy(() -> actualConfigurers.add(configurer4))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @DisplayName(".configure(...) folds and applies all configurers in the given order")
  @Test
  void configureFoldsAndAppliesAllConfigurersInTheGivenOrder() {
    // Given
    var fileManager1 = mock(JctFileManager.class);

    var configurer1 = mock(JctFileManagerConfigurer.class);
    when(configurer1.isEnabled()).thenReturn(true);
    when(configurer1.configure(any())).then(returnParameter());

    var configurer2 = mock(JctFileManagerConfigurer.class);
    when(configurer2.isEnabled()).thenReturn(true);
    when(configurer2.configure(any())).then(returnParameter());

    // This one will return a different file manager in the transformation.
    var fileManager2 = mock(JctFileManager.class);
    var configurer3 = mock(JctFileManagerConfigurer.class);
    when(configurer3.isEnabled()).thenReturn(true);
    when(configurer3.configure(any())).thenReturn(fileManager2);

    // This one will return a different file manager in the transformation.
    var fileManager3 = mock(JctFileManager.class);
    var configurer4 = mock(JctFileManagerConfigurer.class);
    when(configurer4.isEnabled()).thenReturn(true);
    when(configurer4.configure(any())).thenReturn(fileManager3);

    // This one is not enabled, so should be skipped.
    var configurer5 = mock(JctFileManagerConfigurer.class);
    when(configurer5.isEnabled()).thenReturn(false);

    var configurer6 = mock(JctFileManagerConfigurer.class);
    when(configurer6.isEnabled()).thenReturn(true);
    when(configurer6.configure(any())).then(returnParameter());

    chain
        .addLast(configurer1)
        .addLast(configurer2)
        .addLast(configurer3)
        .addLast(configurer4)
        .addLast(configurer5)
        .addLast(configurer6);

    // When
    final var resultFileManager = chain.configure(fileManager1);

    // Then
    verify(configurer1).isEnabled();
    verify(configurer1).configure(fileManager1);
    verify(configurer2).isEnabled();
    verify(configurer2).configure(fileManager1);
    verify(configurer3).isEnabled();
    verify(configurer3).configure(fileManager1);
    verify(configurer4).isEnabled();
    verify(configurer4).configure(fileManager2);
    verify(configurer5).isEnabled();
    verify(configurer6).isEnabled();
    verify(configurer6).configure(fileManager3);
    verifyNoMoreInteractions(configurer1);
    verifyNoMoreInteractions(configurer2);
    verifyNoMoreInteractions(configurer3);
    verifyNoMoreInteractions(configurer4);
    verifyNoMoreInteractions(configurer5);
    verifyNoMoreInteractions(configurer6);

    assertThat(resultFileManager)
        .isSameAs(fileManager3);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  static <T> Answer<T> returnParameter(T... sentinel) {
    if (sentinel.length > 0) {
      throw new IllegalArgumentException(
          "varargs here are a hack to retrieve type info implicitly. "
              + "Do not provide any arguments here."
      );
    }
    return ctx -> (T) ctx.getArgument(0, sentinel.getClass().getComponentType());
  }
}
