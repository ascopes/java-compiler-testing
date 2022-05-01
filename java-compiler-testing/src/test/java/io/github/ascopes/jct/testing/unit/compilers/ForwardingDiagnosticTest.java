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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.ascopes.jct.compilers.ForwardingDiagnostic;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link ForwardingDiagnostic} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ForwardingDiagnostic tests")
@ExtendWith(MockitoExtension.class)
class ForwardingDiagnosticTest {

  @Mock
  Diagnostic<Source> original;

  @InjectMocks
  ForwardingDiagnosticImpl<Source> forwarding;

  @DisplayName("Null delegates are disallowed")
  @Test
  void nullDelegatesAreDisallowed() {
    // Then
    assertThatCode(() -> new ForwardingDiagnosticImpl<>(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("getKind() delegates to the inner diagnostic")
  @EnumSource(Kind.class)
  @ParameterizedTest(name = "for kind = {0}")
  void getKindDelegates(Kind expected) {
    // Given
    given(original.getKind()).willReturn(expected);

    // When
    var actual = forwarding.getKind();

    // Then
    assertThat(actual).isSameAs(expected);
    then(original).should().getKind();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getSource() delegates to the inner diagnostic")
  @Test
  void getSourceDelegates() {
    // Given
    var expected = new Source();
    given(original.getSource()).willReturn(expected);

    // When
    var actual = forwarding.getSource();

    // Then
    assertThat(actual).isSameAs(expected);
    then(original).should().getSource();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getPosition() delegates to the inner diagnostic")
  @Test
  void getPositionDelegates() {
    // Given
    var expected = (long) new Random().nextInt(Integer.MAX_VALUE);
    given(original.getPosition()).willReturn(expected);

    // When
    var actual = forwarding.getPosition();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getPosition();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getStartPosition() delegates to the inner diagnostic")
  @Test
  void getStartPositionDelegates() {
    // Given
    var expected = (long) new Random().nextInt(Integer.MAX_VALUE);
    given(original.getStartPosition()).willReturn(expected);

    // When
    var actual = forwarding.getStartPosition();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getStartPosition();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getEndPosition() delegates to the inner diagnostic")
  @Test
  void getEndPositionDelegates() {
    // Given
    var expected = (long) new Random().nextInt(Integer.MAX_VALUE);
    given(original.getEndPosition()).willReturn(expected);

    // When
    var actual = forwarding.getEndPosition();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getEndPosition();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getLineNumber() delegates to the inner diagnostic")
  @Test
  void getLineNumberDelegates() {
    // Given
    var expected = (long) new Random().nextInt(Integer.MAX_VALUE);
    given(original.getLineNumber()).willReturn(expected);

    // When
    var actual = forwarding.getLineNumber();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getLineNumber();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getColumnNumber() delegates to the inner diagnostic")
  @Test
  void getColumnNumberDelegates() {
    // Given
    var expected = (long) new Random().nextInt(Integer.MAX_VALUE);
    given(original.getColumnNumber()).willReturn(expected);

    // When
    var actual = forwarding.getColumnNumber();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getColumnNumber();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getCode() delegates to the inner diagnostic")
  @NullSource
  @ValueSource(strings = "you.messed.up.something")
  @ParameterizedTest(name = "for code = \"{0}\"")
  void getCodeDelegates(String expected) {
    // Given
    given(original.getCode()).willReturn(expected);

    // When
    var actual = forwarding.getCode();

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getCode();
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("getMessage() delegates to the inner diagnostic")
  @Test
  void getMessageDelegates() {
    // Given
    var expected = UUID.randomUUID().toString();
    given(original.getMessage(any())).willReturn(expected);

    // When
    var actual = forwarding.getMessage(Locale.ENGLISH);

    // Then
    assertThat(actual).isEqualTo(expected);
    then(original).should().getMessage(Locale.ENGLISH);
    then(original).shouldHaveNoMoreInteractions();
  }

  @DisplayName("toString provides the expected output")
  @Test
  void toStringProvidesExpectedOutput() {
    // Given
    var expected = UUID.randomUUID().toString();
    given(original.toString()).willReturn(expected);

    // When
    var actual = forwarding.toString();

    // Then
    assertThat(actual).isEqualTo(expected);
    // We cannot verify the use of toString with Mockito.
  }


  /**
   * Stub for the implementation of a forwarding diagnostic.
   */
  static class ForwardingDiagnosticImpl<S> extends ForwardingDiagnostic<S> {

    /**
     * Initialize this forwarding diagnostic.
     *
     * @param original the original diagnostic to delegate to.
     */
    public ForwardingDiagnosticImpl(Diagnostic<? extends S> original) {
      super(original);
    }
  }

  /**
   * Stub for a source of a diagnostic.
   */
  static class Source {

  }
}
