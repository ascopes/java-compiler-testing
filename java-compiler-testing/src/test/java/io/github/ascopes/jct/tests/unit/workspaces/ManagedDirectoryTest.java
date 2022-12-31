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
package io.github.ascopes.jct.tests.unit.workspaces;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.workspaces.ManagedDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link ManagedDirectory} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ManagedDirectory tests")
@ExtendWith(MockitoExtension.class)
class ManagedDirectoryTest {
  @Mock(answer = Answers.CALLS_REAL_METHODS)
  ManagedDirectory managedDirectory;

  @DisplayName(".and() returns the object")
  @Test
  void andReturnsTheObject() {
    // Then
    assertThat(managedDirectory.and()).isSameAs(managedDirectory);
  }

  @DisplayName(".also() returns the object")
  @Test
  void alsoReturnsTheObject() {
    // Then
    assertThat(managedDirectory.also()).isSameAs(managedDirectory);
  }

  @DisplayName(".then() returns the object")
  @Test
  void thenReturnsTheObject() {
    // Then
    assertThat(managedDirectory.then()).isSameAs(managedDirectory);
  }
}
