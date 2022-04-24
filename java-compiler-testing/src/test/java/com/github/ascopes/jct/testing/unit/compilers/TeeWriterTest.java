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

package com.github.ascopes.jct.testing.unit.compilers;

import static com.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.github.ascopes.jct.compilers.TeeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
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

  @DisplayName("Null charsets are disallowed")
  @Test
  void nullCharsetsAreDisallowed() {
    assertThatCode(() -> new TeeWriter(null, new ByteArrayOutputStream()))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("Null output streams are disallowed")
  @Test
  void nullOutputStreamsAreDisallowed() {
    assertThatCode(() -> new TeeWriter(StandardCharsets.UTF_8, null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("Null writers are disallowed")
  @Test
  void nullWritersAreDisallowed() {
    assertThatCode(() -> new TeeWriter(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("write() delegates to the writer")
  @Test
  void writeDelegatesToTheWriter() throws IOException {
    // Given
    var writer = new StringWriter();
    var tee = new TeeWriter(writer);
    var text = UUID.randomUUID().toString();

    // When
    tee.write(text);

    // Then
    assertThat(writer).hasToString(text);
  }

  @DisplayName("flush() delegates to the writer")
  @Test
  void flushDelegatesToTheWriter() throws IOException {
    // Given
    var writer = mock(OutputStream.class);
    var tee = new TeeWriter(StandardCharsets.UTF_8, writer);

    // When
    tee.flush();

    // Then
    then(writer).should().flush();
    then(writer).shouldHaveNoMoreInteractions();
  }

  @DisplayName("close() delegates to the writer")
  @SuppressWarnings("EmptyTryBlock")
  @Test
  void closeDelegatesToTheWriter() throws IOException {
    // Given
    var writer = mock(Writer.class);

    try (var ignoredTee = new TeeWriter(writer)) {
      // Do nothing
    }

    // Then
    then(writer).should().close();
    then(writer).shouldHaveNoMoreInteractions();
  }

  @DisplayName("toString() should return the buffer content")
  @Test
  void toStringShouldReturnTheBufferContent() throws IOException {
    // Given
    var writer = stub(OutputStream.class);
    var tee = new TeeWriter(StandardCharsets.UTF_8, writer);

    // When
    tee.write("Hello, ");
    tee.write("World");
    tee.write("!");

    // Then
    assertThat(tee).hasToString("Hello, World!");
  }
}
