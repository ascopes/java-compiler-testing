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
package io.github.ascopes.jct.diagnostics;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A writer that wraps an output stream and also writes any content to an in-memory buffer.
 *
 * <p>This is thread-safe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class TeeWriter extends Writer {

  private final Object lock;

  private volatile boolean closed;

  private final Writer writer;

  // We use a StringBuilder and manually synchronise it rather than
  // a string buffer, as we want to manually synchronise the builder
  // and the delegated output writer at the same time.
  private final StringBuilder builder;

  /**
   * Initialise the writer.
   *
   * @param writer the underlying writer to "tee" to.
   */
  public TeeWriter(Writer writer) {
    lock = new Object();
    closed = false;

    this.writer = requireNonNull(writer, "writer");
    builder = new StringBuilder();
    builder.ensureCapacity(512);
  }

  @Override
  public void close() throws IOException {
    // release to set and acquire to check ensures in-order operations to prevent
    // a very minute chance of a race condition.
    synchronized (lock) {
      if (!closed) {
        closed = true;
        builder.trimToSize();
        writer.flush();
        writer.close();
      }
    }
  }

  @Override
  public void flush() throws IOException {
    synchronized (lock) {
      ensureOpen();
      writer.flush();
    }
  }

  /**
   * Get the content of the internal buffer.
   *
   * @return the content.
   * @since 0.2.1
   */
  @API(since = "0.2.1", status = Status.STABLE)
  public String getContent() {
    synchronized (lock) {
      return builder.toString();
    }
  }

  /**
   * Get the content of the internal buffer.
   *
   * <p>This calls {@link #getContent()} internally as of 0.2.1.
   *
   * @return the content.
   */
  @Override
  public String toString() {
    return getContent();
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();

      writer.write(cbuf, off, len);
      // Only append to the buffer once we know that the writing
      // operation has completed.
      builder.append(cbuf, off, len);
    }
  }

  private void ensureOpen() {
    if (closed) {
      throw new IllegalStateException("TeeWriter is closed");
    }
  }

  /**
   * Create a tee writer for the given output stream.
   *
   * <p>Remember you may need to manually flush the tee writer for all contents to be committed to
   * the output stream.
   *
   * @param outputStream the output stream.
   * @param charset the charset.
   * @return the Tee Writer.
   * @since 0.2.1
   */
  @API(since = "0.2.1", status = Status.STABLE)
  public static TeeWriter wrapOutputStream(OutputStream outputStream, Charset charset) {
    requireNonNull(outputStream, "outputStream");
    requireNonNull(charset, "charset");
    var writer = new OutputStreamWriter(outputStream, charset);
    return new TeeWriter(writer);
  }
}
