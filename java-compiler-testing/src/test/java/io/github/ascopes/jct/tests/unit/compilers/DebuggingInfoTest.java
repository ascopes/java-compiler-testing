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
package io.github.ascopes.jct.tests.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.compilers.DebuggingInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * {@link DebuggingInfo} tests.
 *
 * @author Ashley Scopes
 */
class DebuggingInfoTest {
  @DisplayName(".none() returns an empty set")
  @Test
  void noneReturnsEmptySet() {
    // When
    var set = DebuggingInfo.none();

    // Then
    assertThat(set).isEmpty();
  }


  @DisplayName(".just(DebuggingInfo, DebuggingInfo...) returns a combined set")
  @Test
  void justReturnsCombinedSet() {
    // When
    var set = DebuggingInfo.just(DebuggingInfo.LINES, DebuggingInfo.VARS);

    // Then
    assertThat(set).containsExactlyInAnyOrder(
        DebuggingInfo.LINES,
        DebuggingInfo.VARS
    );
  }

  @DisplayName(".all() returns a set of all members")
  @Test
  void allReturnsSetOfAllMembers() {
    // When
    var set = DebuggingInfo.all();

    // Then
    assertThat(set).containsExactlyInAnyOrder(DebuggingInfo.values());
  }

}
