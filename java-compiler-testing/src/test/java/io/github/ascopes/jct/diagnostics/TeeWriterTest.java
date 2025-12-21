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
package io.github.ascopes.jct.diagnostics;

import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static io.github.ascopes.jct.fixtures.Fixtures.unused;
import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TeeWriter} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TeeWriter tests")
@SuppressWarnings("resource")
class TeeWriterTest {

  @DisplayName("Null writers are disallowed")
  @Test
  void nullWritersAreDisallowed() {
    assertThatCode(() -> new TeeWriter(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName(".write() fails if the writer is closed")
  @Test
  void writeFailsIfWriterIsClosed() throws IOException {
    // Given
    var writer = new StringWriter();
    var tee = new TeeWriter(writer);
    var text = someText();
    tee.close();

    // Then
    assertThatThrownBy(() -> tee.write(text))
        .isInstanceOf(IOException.class)
        .hasMessage("TeeWriter is closed");

    assertThat(writer)
        .asString()
        .withFailMessage("Expected no content to be written")
        .isEmpty();
  }

  @DisplayName(".write() delegates to the writer")
  @Test
  void writeDelegatesToTheWriter() throws IOException {
    // Given
    var writer = new StringWriter();
    var tee = new TeeWriter(writer);
    var text = someText();

    // When
    tee.write(text);

    // Then
    assertThat(writer).hasToString(text);
  }

  @DisplayName(".flush() fails if the writer is closed")
  @Test
  void flushFailsIfTheWriterIsClosed() throws IOException {
    // Given
    var writer = mock(Writer.class);
    var tee = new TeeWriter(writer);
    tee.close();
    clearInvocations(writer);

    // Then
    assertThatThrownBy(tee::flush)
        .isInstanceOf(IOException.class)
        .hasMessage("TeeWriter is closed");

    then(writer).shouldHaveNoInteractions();
  }

  @DisplayName(".flush() delegates to the writer")
  @Test
  void flushDelegatesToTheWriter() throws IOException {
    // Given
    var writer = mock(Writer.class);
    var tee = new TeeWriter(writer);

    // When
    tee.flush();

    // Then
    then(writer).should().flush();
    then(writer).shouldHaveNoMoreInteractions();
  }

  @DisplayName(".close() delegates to the writer")
  @Test
  void closeDelegatesToTheWriter() throws IOException {
    // Given
    var writer = mock(Writer.class);

    try (var tee = new TeeWriter(writer)) {
      unused(tee);
    }

    // Then
    then(writer).should().flush();
    then(writer).should().close();
    then(writer).shouldHaveNoMoreInteractions();
  }

  @DisplayName(".close() is idempotent")
  @Test
  void closeIsIdempotent() throws IOException {
    var writer = mock(Writer.class);
    var tee = new TeeWriter(writer);

    for (var i = 0; i < 10; ++i) {
      tee.close();
    }

    then(writer).should(times(1)).flush();
    then(writer).should(times(1)).close();
  }

  @DisplayName(".getContent() should return the buffer content")
  @Test
  void getContentShouldReturnTheBufferContent() throws IOException {
    // Given
    var writer = mock(Writer.class);
    var tee = new TeeWriter(writer);

    // When
    tee.write("Hello, ");
    tee.write("World");
    tee.write("!");

    // Then
    assertThat(tee.getContent()).isEqualTo("Hello, World!");
  }

  @DisplayName(".toString() should return the buffer content")
  @Test
  void toStringShouldReturnTheBufferContent() throws IOException {
    // Given
    var writer = mock(Writer.class);
    var tee = new TeeWriter(writer);

    // When
    tee.write("Hello, ");
    tee.write("World");
    tee.write("!");

    // Then
    assertThat(tee).hasToString("Hello, World!");
  }

  @DisplayName(".wrapOutputStream(null, Charset) throws a NullPointerException")
  @Test
  void wrapOutputStreamWithNullOutputStreamThrowsNullPointerException() {
    // Then
    assertThatThrownBy(() -> TeeWriter.wrapOutputStream(null, defaultCharset()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("outputStream");
  }

  @DisplayName(".wrapOutputStream(OutputStream, null) throws a NullPointerException")
  @Test
  void wrapOutputStreamWithNullCharsetThrowsNullPointerException() {
    // Then
    var os = new ByteArrayOutputStream();
    assertThatThrownBy(() -> TeeWriter.wrapOutputStream(os, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("charset");
  }

  @DisplayName(".wrapOutputStream(OutputStream, Charset) creates a writer to the output stream")
  @Test
  void wrapOutputStreamWritesToTheOutputStream() throws IOException {
    // Given
    var outputStream = new ByteArrayOutputStream();
    var charset = StandardCharsets.UTF_8;
    var teeWriter = TeeWriter.wrapOutputStream(outputStream, charset);

    // When
    teeWriter.write("Hello, World!");
    teeWriter.write("\n");
    teeWriter.write("blah blah blah €${¾½€đ¢æßŧ");
    teeWriter.flush();

    // Then
    assertThat(teeWriter.toString())
        .isEqualTo("Hello, World!\nblah blah blah €${¾½€đ¢æßŧ");
    assertThat(outputStream.toString(charset))
        .isEqualTo("Hello, World!\nblah blah blah €${¾½€đ¢æßŧ");
  }
}
