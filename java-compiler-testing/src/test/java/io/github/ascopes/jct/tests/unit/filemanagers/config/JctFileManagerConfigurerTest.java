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

import io.github.ascopes.jct.filemanagers.config.JctFileManagerConfigurer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerConfigurerTest {
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  JctFileManagerConfigurer configurer;

  @DisplayName(".isEnabled() defaults to returning true")
  @Test
  void isEnabledReturnsTrue() {
    // Then
    assertThat(configurer.isEnabled()).isTrue();
  }
}
