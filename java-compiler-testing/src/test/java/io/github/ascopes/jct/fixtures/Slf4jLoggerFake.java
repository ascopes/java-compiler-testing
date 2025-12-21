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
package io.github.ascopes.jct.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

/**
 * Fake implementation of a SLF4J logger that captures all logging inputs.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Slf4jLoggerFake extends AbstractLogger {

  private final List<Entry<Level, LogRecord>> entries;

  public Slf4jLoggerFake() {
    entries = new ArrayList<>();
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
      Throwable ex,
      String message,
      Object... args
  ) {
    var levelEntries = entries
        .stream()
        .filter(entry -> entry.getKey().equals(level));

    var expect = new LogRecord(ex, message, args);

    assertThat(levelEntries)
        .withFailMessage("Expected at least one %s entry to be logged", level)
        .hasSizeGreaterThan(0)
        .extracting(Entry::getValue)
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
      Marker marker,
      String format,
      Object[] args,
      Throwable throwable
  ) {
    entries.add(new SimpleImmutableEntry<>(level, new LogRecord(
        throwable,
        format,
        args
    )));
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return true;

  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return true;
  }

  private record LogRecord(Throwable ex, String message, Object... args) {

    @Override
    public boolean equals(Object other) {
      if (other instanceof LogRecord that) {
        return Objects.equals(ex, that.ex)
            && Objects.equals(message, that.message)
            && Arrays.deepEquals(args, that.args);
      }
      return false;
    }
  }
}
