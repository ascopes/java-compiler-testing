package com.github.ascopes.jct.diagnostics;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;

/**
 * A writer that also buffers the content being written within an in-memory buffer.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class TeeWriter extends Writer {

  private final Writer writer;
  private final StringBuffer buffer;

  /**
   * Initialize this writer by wrapping an output stream in an internally-held writer.
   *
   * @param outputStream the output stream to delegate to.
   */
  public TeeWriter(OutputStream outputStream) {
    this(new OutputStreamWriter(outputStream));
  }

  /**
   * Initialize this writer.
   *
   * @param writer the writer to delegate to.
   */
  public TeeWriter(Writer writer) {
    this.writer = Objects.requireNonNull(writer);
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
