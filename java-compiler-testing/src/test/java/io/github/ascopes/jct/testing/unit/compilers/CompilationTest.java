/*
 * Copyright (C) 2022 Ashley Scopes
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

package io.github.ascopes.jct.testing.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.ascopes.jct.compilers.Compilation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link Compilation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Compilation tests")
@ExtendWith(MockitoExtension.class)
class CompilationTest {

  @Mock
  Compilation compilation;

  @DisplayName("isFailure() returns opposite of isSuccessful()")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for isSuccessful() = {0}")
  void isFailureReturnsOppositeOfIsSuccessful(boolean successful) {
    // Given
    given(compilation.isSuccessful()).willReturn(successful);
    given(compilation.isFailure()).willCallRealMethod();

    // When
    var failure = compilation.isFailure();

    // Then
    assertThat(failure).isEqualTo(!successful);
    then(compilation).should().isSuccessful();
  }
}
