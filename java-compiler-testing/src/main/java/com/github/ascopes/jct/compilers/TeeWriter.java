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

package com.github.ascopes.jct.compilers;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class TeeWriter extends Writer {

  private final Writer writer;
  private final StringBuffer buffer;

  /**
   * Initialize this writer by wrapping an output stream in an internally-held writer.
   *
   * <p>Note that this will not buffer the output stream itself. That is up to you to do.
   *
   * @param charset      the charset to write with.
   * @param outputStream the output stream to delegate to.
   */
  public TeeWriter(Charset charset, OutputStream outputStream) {
    this(new OutputStreamWriter(
        requireNonNull(outputStream),
        charset
    ));
  }

  /**
   * Initialize this writer.
   *
   * @param writer the writer to delegate to.
   */
  public TeeWriter(Writer writer) {
    this.writer = requireNonNull(writer);
    buffer = new StringBuffer();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    writer.write(cbuf, off, len);
    buffer.append(cbuf, off, len);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException {
    writer.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    writer.close();
  }

  /**
   * Get the string content that was written out to the buffer.
   *
   * @return the buffer content.
   */
  @Override
  public String toString() {
    return buffer.toString();
  }
}
