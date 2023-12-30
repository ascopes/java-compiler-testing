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
package io.github.ascopes.jct.tests.unit.assertions;


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.assertions.OutputContainerGroupAssert;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link OutputContainerGroupAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("OutputContainerGroupAssert tests")
class OutputContainerGroupAssertTest {

  @DisplayName("OutputContainerGroupAssert.packages(...) tests")
  @Nested
  class PackagesTest {

    @DisplayName(".packages() throws an exception if the group is null")
    @Test
    void packagesThrowsExceptionIfGroupIsNull() {
      // Given
      var assertions = new OutputContainerGroupAssert(null);

      // Then
      assertThatThrownBy(assertions::packages)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".packages() returns assertions across the package groups")
    @Test
    void packagesReturnsAssertionsAcrossPackageGroups() {
      // Given
      var outputGroup = mock(OutputContainerGroup.class);
      var assertions = new OutputContainerGroupAssert(outputGroup);

      // Then
      assertThatCode(() -> assertions.packages().isSameAs(outputGroup)).doesNotThrowAnyException();
    }
  }

  @DisplayName("OutputContainerGroupAssert.modules(...) tests")
  @Nested
  class ModulesTest {

    @DisplayName(".modules() throws an exception if the group is null")
    @Test
    void modulesThrowsExceptionIfGroupIsNull() {
      // Given
      var assertions = new OutputContainerGroupAssert(null);

      // Then
      assertThatThrownBy(assertions::modules)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".modules() returns assertions across the module groups")
    @Test
    void modulesReturnsAssertionsAcrossModuleGroups() {
      // Given
      var outputGroup = mock(OutputContainerGroup.class);
      var assertions = new OutputContainerGroupAssert(outputGroup);

      // Then
      assertThatCode(() -> assertions.modules().isSameAs(outputGroup)).doesNotThrowAnyException();
    }
  }
}
