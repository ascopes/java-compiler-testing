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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import io.github.ascopes.jct.containers.impl.ContainerGroupRepositoryImpl;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.workspaces.PathRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("JctFileManagerImpl Tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerImplTest {

  JctFileManagerImpl fileManager;
  ContainerGroupRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    // Mock the construction so that we can access the internally created container group repository
    // object.
    try (var construction = mockConstruction(ContainerGroupRepositoryImpl.class)) {
      fileManager = new JctFileManagerImpl("some-release");
      repository = construction.constructed().iterator().next();
    }
  }

  @DisplayName("null releases are disallowed")
  @SuppressWarnings({"resource", "ConstantConditions"})
  @Test
  void testIfNullPointerExceptionThrownIfReleaseNull() {
    // Then
    assertThatThrownBy(() -> new JctFileManagerImpl(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("release");
  }

  @DisplayName(".addPath(Location, PathRoot) adds the path to the repository")
  @Test
  void addPathAddsThePathToTheRepository() {
    // Given
    var location = someLocation();
    var pathRoot = mock(PathRoot.class);

    // When
    fileManager.addPath(location, pathRoot);

    // Then
    verify(repository).addPath(location, pathRoot);
  }
}
