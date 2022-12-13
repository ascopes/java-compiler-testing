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
package io.github.ascopes.jct.testing.unit.workspaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.impl.RamDirectory;
import io.github.ascopes.jct.workspaces.impl.TempDirectory;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link PathStrategy} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PathStrategy tests")
class PathStrategyTest {

  @DisplayName("PathStrategies should produce the expected ManagedDirectory objects")
  @MethodSource("testCases")
  @ParameterizedTest(name = "{0} should create {1} objects")
  void pathStrategiesShouldProduceTheExpectedManagedDirectoryObjects(
      PathStrategy strategy,
      Class<?> type
  ) throws IOException {
    var obj = strategy.newInstance("foobar");
    try {
      assertThat(obj)
          .isInstanceOf(ManagedDirectory.class)
          .isInstanceOf(type);
    } finally {
      obj.close();
    }
  }

  @DisplayName("Expected objects have the right names")
  @MethodSource("testCases")
  @ParameterizedTest(name = "{0} should create objects with the right name")
  void ramDirectoriesMemberCreatesRamDirectory(
      PathStrategy strategy,
      Class<?> ignored
  ) throws IOException {
    var name = UUID.randomUUID().toString();
    var obj = strategy.newInstance(name);

    try {
      assertThat(obj)
          .extracting(ManagedDirectory::getName)
          .isEqualTo(name);
    } finally {
      obj.close();
    }
  }

  @DisplayName("defaultStrategy() returns RAM_DIRECTORIES")
  @Test
  void defaultStrategyReturnsRamDirectories() {
    assertThat(PathStrategy.defaultStrategy())
        .isSameAs(PathStrategy.RAM_DIRECTORIES);
  }

  static Stream<Arguments> testCases() {
    return Stream.of(
        arguments(PathStrategy.RAM_DIRECTORIES, RamDirectory.class),
        arguments(PathStrategy.TEMP_DIRECTORIES, TempDirectory.class)
    );
  }
}
