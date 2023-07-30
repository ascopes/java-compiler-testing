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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelease;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManagers;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerFactoryImpl;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link JctFileManagers} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagers tests")
class JctFileManagersTest implements UtilityClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return JctFileManagers.class;
  }

  @DisplayName(".newJctFileManager(String) returns a new JctFileManagerImpl instance")
  @Test
  void newJctFileManagerReturnsNewJctFileManagerImplInstance() {
    // Given
    var release = someRelease();

    // When
    var fileManager = JctFileManagers.newJctFileManager(release);

    // Then
    assertThat(fileManager).isInstanceOf(JctFileManagerImpl.class);
    assertThat(fileManager.getEffectiveRelease()).isEqualTo(release);
  }

  @DisplayName(
      ".newJctFileManagerFactory(JctCompiler) returns a new JctFileManagerFactoryImpl instance"
  )
  @Test
  void newJctFileManagerFactoryReturnsNewJctFileManagerFactoryImplInstance() {
    // Given
    var compiler = mock(JctCompiler.class);

    // When
    var fileManagerFactory = JctFileManagers.newJctFileManagerFactory(compiler);

    // Then
    assertThat(fileManagerFactory).isInstanceOf(JctFileManagerFactoryImpl.class);
    assertThat(((JctFileManagerFactoryImpl) fileManagerFactory).getCompiler()).isSameAs(compiler);
  }
}
