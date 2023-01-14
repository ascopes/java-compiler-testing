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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someBinaryData;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someCharset;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someJavaFileObject;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.assertions.AbstractJavaFileObjectAssert;
import io.github.ascopes.jct.assertions.JavaFileObjectKindAssert;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.annotation.Nullable;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.assertj.core.api.AbstractByteArrayAssert;
import org.assertj.core.api.AbstractInstantAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AbstractUriAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link AbstractJavaFileObjectAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("AbstractJavaFileObjectAssert tests")
class AbstractJavaFileObjectAssertTest {

  @DisplayName("AbstractJavaFileObjectAssert#uri tests")
  @Nested
  class UriTest {

    @DisplayName(".uri() fails if the JavaFileObject is null")
    @Test
    void failsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(assertions::uri)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".uri() returns a URI assertion")
    @Test
    void returnsUriAssertion() {
      // Given
      var fileObject = someJavaFileObject();
      var uri = somePath().toUri();
      when(fileObject.toUri()).thenReturn(uri);
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.uri())
          .isInstanceOf(AbstractUriAssert.class)
          .satisfies(uriAssertions -> uriAssertions
              .isNotNull()
              .isSameAs(uri));
    }
  }

  @DisplayName("AbstractJavaFileObjectAssert#name tests")
  @Nested
  class NameTest {
    @DisplayName(".name() fails if the JavaFileObject is null")
    @Test
    void failsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(assertions::name)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".name() returns a string assertion")
    @Test
    void returnsStringAssertion() {
      // Given
      var fileObject = someJavaFileObject();
      var name = somePath().getFileName().toString();
      when(fileObject.getName()).thenReturn(name);
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.name())
          .isInstanceOf(AbstractStringAssert.class)
          .satisfies(nameAssertions -> nameAssertions
              .isNotNull()
              .isSameAs(name));
    }
  }

  @DisplayName("AbstractJavaFileObjectAssert#binaryContent tests")
  @Nested
  class RawContentTest {
    @DisplayName(".binaryContent() fails if the JavaFileObject is null")
    @Test
    void failsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(assertions::binaryContent)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".binaryContent() returns a byte array assertion")
    @Test
    void returnsByteArrayAssertion() throws IOException {
      // Given
      var fileObject = someJavaFileObject();
      var data = someBinaryData();
      when(fileObject.openInputStream())
          .then(ctx -> new ByteArrayInputStream(data));
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.binaryContent())
          .isInstanceOf(AbstractByteArrayAssert.class)
          .satisfies(dataAssertions -> dataAssertions
              .isNotNull()
              .containsExactly(data));
    }
  }

  @DisplayName("AbstractJavaFileObjectAssert#content tests")
  @Nested
  class ContentTest {
    @DisplayName(".content() fails if the JavaFileObject is null")
    @Test
    void noArgsFailsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(assertions::content)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".content() returns assertions for the UTF-8 encoded string")
    @Test
    void noArgsContentReturnsAssertionsForUtf8EncodedString() throws IOException {
      // Given
      var stringContent = "Hello, World! €$£ →↤₱ԊƢ∎∈⋈";
      var fileObject = someJavaFileObject();
      when(fileObject.openInputStream())
          .then(ctx -> new ByteArrayInputStream(stringContent.getBytes(StandardCharsets.UTF_8)));
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.content())
          .isInstanceOf(AbstractStringAssert.class)
          .satisfies(contentAssert -> contentAssert
              .isNotNull()
              .isEqualTo(stringContent));
    }

    @DisplayName(".content(Charset) fails if the JavaFileObject is null")
    @Test
    void charsetArgsFailsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.content(someCharset()))
          .isInstanceOf(AssertionError.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @DisplayName(".content(Charset) fails if the charset is null")
    @Test
    void charsetArgsFailsIfCharsetIsNull() {
      // Given
      var assertions = new Impl(someJavaFileObject());

      // Then
      assertThatThrownBy(() -> assertions.content((Charset) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("charset must not be null");
    }

    @DisplayName(".content(Charset) returns assertions for the string")
    @ValueSource(strings = {"UTF-8", "UTF-16", "US-ASCII"})
    @ParameterizedTest(name = "for charset {0}")
    void charsetArgsContentReturnsAssertions(String charsetName) throws IOException {
      // Given
      var charset = Charset.forName(charsetName);
      var stringContent = "Hello, World! €$£ →↤₱ԊƢ∎∈⋈";
      var binaryContent = stringContent.getBytes(charset);
      var fileObject = someJavaFileObject();
      when(fileObject.openInputStream())
          .then(ctx -> new ByteArrayInputStream(binaryContent));
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.content(charset))
          .isInstanceOf(AbstractStringAssert.class)
          .satisfies(contentAssert -> contentAssert
              .isNotNull()
              .isEqualTo(new String(binaryContent, charset)));
    }

    @DisplayName(".content(CharsetDecoder) fails if the JavaFileObject is null")
    @Test
    void charsetDecoderArgsFailsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);
      var decoder = someCharset().newDecoder();

      // Then
      assertThatThrownBy(() -> assertions.content(decoder))
          .isInstanceOf(AssertionError.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @DisplayName(".content(CharsetDecoder) fails if the charset is null")
    @Test
    void charsetDecoderArgsFailsIfCharsetIsNull() {
      // Given
      var assertions = new Impl(someJavaFileObject());

      // Then
      assertThatThrownBy(() -> assertions.content((CharsetDecoder) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("charsetDecoder must not be null");
    }

    @DisplayName(".content(CharsetDecoder) returns assertions for the string")
    @ValueSource(strings = {"UTF-8", "UTF-16", "US-ASCII"})
    @ParameterizedTest(name = "for charset {0}")
    void charsetDecoderArgsContentReturnsAssertions(String charsetName) throws IOException {
      // Given
      var charset = Charset.forName(charsetName);
      var decoder = charset.newDecoder();
      var stringContent = "Hello, World! €$£ →↤₱ԊƢ∎∈⋈";
      var binaryContent = stringContent.getBytes(charset);
      var fileObject = someJavaFileObject();
      when(fileObject.openInputStream())
          .then(ctx -> new ByteArrayInputStream(binaryContent));
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.content(decoder))
          .isInstanceOf(AbstractStringAssert.class)
          .satisfies(contentAssert -> contentAssert
              .isNotNull()
              .isEqualTo(new String(binaryContent, charset)));
    }
  }

  @DisplayName("AbstractJavaFileObjectAssert#lastModified tests")
  @Nested
  class LastModifiedTest {
    @DisplayName(".lastModified() fails if the JavaFileObject is null")
    @Test
    void failsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(assertions::lastModified)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".lastModified() returns an instant assertion")
    @ValueSource(longs = {0L, 1673706474733L})
    @ParameterizedTest(name = "for timestamp {0}")
    void returnsInstantAssertion(long timestamp) {
      // Given
      var fileObject = someJavaFileObject();
      when(fileObject.getLastModified()).thenReturn(timestamp);
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.lastModified())
          .isInstanceOf(AbstractInstantAssert.class)
          .satisfies(instantAssertion -> instantAssertion
              .isNotNull()
              .isEqualTo(Instant.ofEpochMilli(timestamp)));
    }
  }

  @DisplayName("AbstractJavaFileObjectAssert#kind tests")
  @Nested
  class KindTest {
    @DisplayName(".kind() fails if the JavaFileObject is null")
    @Test
    void failsIfJavaFileObjectIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(assertions::lastModified)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".kind() returns a JavaFileObjectKind assertion")
    @EnumSource(Kind.class)
    @ParameterizedTest(name = "for kind {0}")
    void returnsInstantAssertion(Kind kind) {
      // Given
      var fileObject = someJavaFileObject();
      when(fileObject.getKind()).thenReturn(kind);
      var assertions = new Impl(fileObject);

      // Then
      assertThat(assertions.kind())
          .isInstanceOf(JavaFileObjectKindAssert.class)
          .satisfies(kindAssertion -> kindAssertion
              .isNotNull()
              .isEqualTo(kind));
    }
  }

  static class Impl extends AbstractJavaFileObjectAssert<Impl, JavaFileObject> {

    Impl(@Nullable JavaFileObject actual) {
      super(actual, Impl.class);
    }
  }
}
