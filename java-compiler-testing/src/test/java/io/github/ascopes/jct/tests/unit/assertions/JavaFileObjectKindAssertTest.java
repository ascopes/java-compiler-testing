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
package io.github.ascopes.jct.tests.unit.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.assertions.JavaFileObjectKindAssert;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;

/**
 * {@link JavaFileObjectKindAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavaFileObjectKindAssert tests")
class JavaFileObjectKindAssertTest {

  @DisplayName("JavaFileObjectKindAssert#isSource tests")
  @Nested
  class IsSourceTest {

    @DisplayName(".isSource() fails if the kind is not a source")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "SOURCE")
    @ParameterizedTest(name = "for {0}")
    void isSourceFailsIfKindIsNotSource(Kind kind) {
      // Given
      var assertions = new JavaFileObjectKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isSource)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isSource() succeeds if the kind is a source")
    @Test
    void isSourceSucceedsIfTheKindIsSource() {
      // Given
      var assertions = new JavaFileObjectKindAssert(Kind.SOURCE);

      // Then
      assertThat(assertions.isSource())
          .isSameAs(assertions);
    }
  }

  @DisplayName("JavaFileObjectKindAssert#isClass tests")
  @Nested
  class IsClassTest {

    @DisplayName(".isClass() fails if the kind is not a class")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "CLASS")
    @ParameterizedTest(name = "for {0}")
    void isClassFailsIfKindIsNotClass(Kind kind) {
      // Given
      var assertions = new JavaFileObjectKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isClass)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isClass() succeeds if the kind is a class")
    @Test
    void isClassSucceedsIfTheKindIsClass() {
      // Given
      var assertions = new JavaFileObjectKindAssert(Kind.CLASS);

      // Then
      assertThat(assertions.isClass())
          .isSameAs(assertions);
    }
  }

  @DisplayName("JavaFileObjectKindAssert#isHtml tests")
  @Nested
  class IsHtmlTest {

    @DisplayName(".isHtml() fails if the kind is not HTML")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "HTML")
    @ParameterizedTest(name = "for {0}")
    void isHtmlFailsIfKindIsNotHtml(Kind kind) {
      // Given
      var assertions = new JavaFileObjectKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isHtml)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isHtml() succeeds if the kind is HTML")
    @Test
    void isHtmlSucceedsIfTheKindIsHtml() {
      // Given
      var assertions = new JavaFileObjectKindAssert(Kind.HTML);

      // Then
      assertThat(assertions.isHtml())
          .isSameAs(assertions);
    }
  }

  @DisplayName("JavaFileObjectKindAssert#isOther tests")
  @Nested
  class IsOtherTest {

    @DisplayName(".isOther() fails if the kind is not OTHER")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "OTHER")
    @ParameterizedTest(name = "for {0}")
    void isOtherFailsIfKindIsNotOther(Kind kind) {
      // Given
      var assertions = new JavaFileObjectKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isOther)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isOther() succeeds if the kind is OTHER")
    @Test
    void isOtherSucceedsIfTheKindIsOther() {
      // Given
      var assertions = new JavaFileObjectKindAssert(Kind.OTHER);

      // Then
      assertThat(assertions.isOther())
          .isSameAs(assertions);
    }
  }

  @DisplayName("JavaFileObjectKindAssert#extension tests")
  @Nested
  class ExtensionTest {
    @DisplayName(".extension() fails if the kind is null")
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void extensionFailsIfKindIsNull() {
      // Given
      var assertions = new JavaFileObjectKindAssert(null);

      // Then
      assertThatThrownBy(assertions::extension)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".extension() returns assertions on the extension")
    @EnumSource(JavaFileObject.Kind.class)
    @ParameterizedTest(name = "for {0}")
    void extensionReturnsAssertionsOnExtension(Kind kind) {
      // Given
      var assertions = new JavaFileObjectKindAssert(kind);

      // Then
      assertThat(assertions.extension())
          .isInstanceOf(AbstractStringAssert.class)
          .satisfies(extensionAssertion -> extensionAssertion.isEqualTo(kind.extension));
    }
  }
}
