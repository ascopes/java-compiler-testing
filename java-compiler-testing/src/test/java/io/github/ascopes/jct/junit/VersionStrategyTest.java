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
package io.github.ascopes.jct.junit;

import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link VersionStrategy} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("VersionStrategy tests")
@ExtendWith(MockitoExtension.class)
class VersionStrategyTest {

  String baseName;

  @Mock(answer = Answers.RETURNS_SELF, strictness = Strictness.LENIENT)
  JctCompiler compiler;

  @BeforeEach
  void setUp() {
    baseName = someText();
    when(compiler.getName()).thenReturn(baseName);
  }

  @DisplayName("RELEASE sets the release")
  @ValueSource(ints = {10, 15, 20})
  @ParameterizedTest(name = "for version {0}")
  void releaseSetsTheRelease(int version) {
    // When
    VersionStrategy.RELEASE.configureCompiler(compiler, version);

    // Then
    verify(compiler).release(version);
    verify(compiler).getName();
    verify(compiler).name(baseName + " (release = Java " + version + ")");
    verifyNoMoreInteractions(compiler);
  }

  @DisplayName("SOURCE sets the source")
  @ValueSource(ints = {10, 15, 20})
  @ParameterizedTest(name = "for version {0}")
  void sourceSetsTheSource(int version) {
    // When
    VersionStrategy.SOURCE.configureCompiler(compiler, version);

    // Then
    verify(compiler).source(version);
    verify(compiler).getName();
    verify(compiler).name(baseName + " (source = Java " + version + ")");
    verifyNoMoreInteractions(compiler);
  }

  @DisplayName("TARGET sets the target")
  @ValueSource(ints = {10, 15, 20})
  @ParameterizedTest(name = "for version {0}")
  void targetSetsTheTarget(int version) {
    // When
    VersionStrategy.TARGET.configureCompiler(compiler, version);

    // Then
    verify(compiler).target(version);
    verify(compiler).getName();
    verify(compiler).name(baseName + " (target = Java " + version + ")");
    verifyNoMoreInteractions(compiler);
  }

  @DisplayName("SOURCE_AND_TARGET sets the target")
  @ValueSource(ints = {10, 15, 20})
  @ParameterizedTest(name = "for version {0}")
  void sourceAndTargetSetsTheSourceAndTarget(int version) {
    // When
    VersionStrategy.SOURCE_AND_TARGET.configureCompiler(compiler, version);

    // Then
    verify(compiler).source(version);
    verify(compiler).target(version);
    verify(compiler).getName();
    verify(compiler).name(baseName + " (source and target = Java " + version + ")");
    verifyNoMoreInteractions(compiler);
  }
}
