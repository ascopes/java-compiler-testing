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
package io.github.ascopes.jct.tests.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.utils.ToStringBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

/**
 * Fake implementation of a SLF4J logger that captures all logging inputs.
 *
 * <p>This implementation is thread-safe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Slf4jLoggerFake extends AbstractLogger {

  private final List<LogRecord> entries;

  public Slf4jLoggerFake() {
    entries = Collections.synchronizedList(new ArrayList<LogRecord>(32));
  }

  /**
   * Assert that a log entry with the given contents is logged once.
   *
   * @param level   the logger level.
   * @param ex      any exception, or {@code null} if no throwable is provided.
   * @param message the logger format message string.
   * @param args    any arguments interpolated into the message.
   */
  public void assertThatEntryLogged(
      Level level,
      @Nullable Throwable ex,
      String message,
      Object... args
  ) {
    var levelEntries = entries
        .stream()
        .filter(entry -> entry.level.equals(level));

    var expect = new LogRecord(level, ex, message, args);

    assertThat(levelEntries)
        .withFailMessage("Expected at least one %s entry to be logged", level)
        .hasSizeGreaterThan(0)
        .withFailMessage(
            "No log entry at level %s found that matches\n - %s\nAll invocations:\n%s",
            level,
            expect,
            entries.stream()
                .map(Objects::toString)
                .map(" - "::concat)
                .collect(Collectors.joining("\n"))
        )
        .containsOnlyOnce(expect);
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return "foo.bar.baz";
  }

  @Override
  protected void handleNormalizedLoggingCall(
      Level level,
      @Nullable Marker marker,
      String format,
      Object[] args,
      @Nullable Throwable throwable
  ) {
    entries.add(new LogRecord(level, throwable, format, args));
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public boolean isTraceEnabled(@Nullable Marker marker) {
    return true;
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public boolean isDebugEnabled(@Nullable Marker marker) {
    return true;
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public boolean isInfoEnabled(@Nullable Marker marker) {
    return true;

  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public boolean isWarnEnabled(@Nullable Marker marker) {
    return true;
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public boolean isErrorEnabled(@Nullable Marker marker) {
    return true;
  }

  private static final class LogRecord {

    private final Level level;
    private final @Nullable Throwable ex;
    private final String message;
    private final Object[] args;

    LogRecord(Level level, @Nullable Throwable ex, String message, Object... args) {
      this.level = level;
      this.ex = ex;
      this.message = message;
      this.args = args;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (!(obj instanceof LogRecord)) {
        return false;
      }

      var that = (LogRecord) obj;

      return Objects.equal(level, that.level)
          && Objects.equals(ex, that.ex)
          && Objects.equals(message, that.message)
          && Arrays.deepEquals(args, that.args);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .attribute("level", level)
          .attribute("ex", ex)
          .attribute("message", message)
          .attribute("args", args)
          .toString();
    }
  }
}
