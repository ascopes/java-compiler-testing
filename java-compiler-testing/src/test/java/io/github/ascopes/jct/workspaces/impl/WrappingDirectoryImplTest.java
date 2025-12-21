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
package io.github.ascopes.jct.workspaces.impl;

import static io.github.ascopes.jct.fixtures.Fixtures.somePath;
import static io.github.ascopes.jct.utils.FileUtils.retrieveRequiredUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link WrappingDirectoryImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("WrappingDirectoryImpl tests")
class WrappingDirectoryImplTest {

  @DisplayName("WrappingDirectoryImpl constructor tests")
  @Nested
  class ConstructorTest {

    @DisplayName("Initialising without a parent configures the object correctly")
    @SuppressWarnings("DataFlowIssue")
    @Test
    void initialisingWithoutParentConfiguresTheObjectCorrectly() {
      // Given
      var expectedPath = somePath();

      // When
      var wrappingDirectory = new WrappingDirectoryImpl(expectedPath);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(wrappingDirectory.getParent())
            .as(".getParent()")
            .isNull();
        softly.assertThat(wrappingDirectory.getPath())
            .as(".getPath()")
            .isSameAs(expectedPath);
        softly.assertThat(wrappingDirectory.getUri())
            .as(".getUri()")
            .isEqualTo(expectedPath.toUri());
        softly.assertThat(wrappingDirectory.getUrl())
            .as(".getUrl()")
            .isEqualTo(retrieveRequiredUrl(expectedPath));
      });
    }

    @DisplayName("Initialising with a parent configures the object correctly")
    @SuppressWarnings("DataFlowIssue")
    @Test
    void initialisingWithParentConfiguresTheObjectCorrectly() {
      // Given
      var parent = mock(PathRoot.class);
      var rootPath = somePath();
      when(parent.getPath()).thenReturn(rootPath);
      var parts = List.of("foo", "bar", "baz");
      var expectedPath = FileUtils.resolvePathRecursively(rootPath, parts);

      // When
      var wrappingDirectory = new WrappingDirectoryImpl(parent, parts);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(wrappingDirectory.getParent())
            .as(".getParent()")
            .isSameAs(parent);
        softly.assertThat(wrappingDirectory.getPath())
            .as(".getPath()")
            .isEqualTo(expectedPath);
        softly.assertThat(wrappingDirectory.getUri())
            .as(".getUri()")
            .isEqualTo(expectedPath.toUri());
        softly.assertThat(wrappingDirectory.getUrl())
            .as(".getUrl()")
            .isEqualTo(retrieveRequiredUrl(expectedPath));
      });
    }

    @DisplayName("Null paths are disallowed for parentless instances")
    @SuppressWarnings("DataFlowIssue")
    @Test
    void nullPathsAreDisallowedForParentlessInstances() {
      // Then
      assertThatThrownBy(() -> new WrappingDirectoryImpl(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("path");
    }

    @DisplayName("Null parents are disallowed for instances with parents")
    @SuppressWarnings("DataFlowIssue")
    @Test
    void nullParentsAreDisallowedForInstancesWithParents() {
      // Then
      assertThatThrownBy(() -> new WrappingDirectoryImpl(null, List.of("xxx")))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("parent");
    }

    @DisplayName("Null parts lists are disallowed for instances with parents")
    @SuppressWarnings("DataFlowIssue")
    @Test
    void nullPartsListsAreDisallowedForInstancesWithParents() {
      // Then
      assertThatThrownBy(() -> new WrappingDirectoryImpl(mock(PathRoot.class), null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("parts");
    }

    @DisplayName("Null parts are disallowed for instances with parents")
    @SuppressWarnings("DataFlowIssue")
    @Test
    void nullPartsAreDisallowedForInstancesWithParents() {
      // Then
      var list = new ArrayList<String>();
      list.add(null);

      assertThatThrownBy(() -> new WrappingDirectoryImpl(mock(PathRoot.class), list))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("parts[0]");
    }
  }

  @DisplayName("WrappingDirectoryImpl.equals(...) tests")
  @Nested
  class EqualsTest {

    WrappingDirectoryImpl wrappingDirectory;

    @BeforeEach
    void setUp() {
      wrappingDirectory = new WrappingDirectoryImpl(somePath());
    }

    @DisplayName("objects do not equal null")
    @Test
    void doNotEqualNull() {
      // Then
      assertThat(wrappingDirectory).isNotEqualTo(null);
    }

    @DisplayName("objects do not equal other unrelated objects")
    @Test
    void doNotEqualOtherObjects() {
      // Given
      var anotherWrappingDirectory = new WrappingDirectoryImpl(somePath());
      assertThat(wrappingDirectory.getPath()).isNotEqualTo(anotherWrappingDirectory.getPath());

      // Then
      assertThat(wrappingDirectory).isNotEqualTo(anotherWrappingDirectory);
    }

    @DisplayName("objects equal other objects with the same path")
    @Test
    void equalOtherObjectsWithSamePath() {
      // Given
      var anotherWrappingDirectory = new WrappingDirectoryImpl(wrappingDirectory.getPath());

      // Then
      assertThat(wrappingDirectory).isEqualTo(anotherWrappingDirectory);
    }

    @DisplayName("objects equal themselves")
    @SuppressWarnings("EqualsWithItself")  // Intentional, duh!
    @Test
    void equalThemselves() {
      // Then
      assertThat(wrappingDirectory).isEqualTo(wrappingDirectory);
    }
  }

  @DisplayName(".hashCode() returns the hashcode of the URI")
  @RepeatedTest(5)
  void hashCodeReturnsUriHashCode() {
    // Given
    var path = somePath();
    var wrappingDirectory = new WrappingDirectoryImpl(path);

    // Then
    assertThat(wrappingDirectory.hashCode())
        .as(".hashCode()")
        .isEqualTo(path.toUri().hashCode());
  }

  @DisplayName(".toString() returns the expected value")
  @MethodSource("toStringCases")
  @ParameterizedTest(name = "expecting \"{0}\" to equal \"{1}\"")
  void toStringReturnsExpectedValue(WrappingDirectoryImpl dir, String expected) {
    // Then
    assertThat(dir).hasToString(expected);
  }

  static Stream<Arguments> toStringCases() {
    var parentlessCase = new WrappingDirectoryImpl(somePath());
    var parentlessExpected = String.format(
        "WrappingDirectoryImpl{parent=null, uri=\"%s\"}",
        parentlessCase.getUri()
    );

    var parent = mock(PathRoot.class, "some path root parent");
    when(parent.getPath()).thenReturn(somePath());

    var parentCase = new WrappingDirectoryImpl(parent, List.of("foo", "bar", "baz"));
    var parentExpected = String.format(
        "WrappingDirectoryImpl{parent=some path root parent, uri=\"%s\"}",
        parentCase.getUri()
    );

    return Stream.of(
        arguments(parentlessCase, parentlessExpected),
        arguments(parentCase, parentExpected)
    );
  }
}
