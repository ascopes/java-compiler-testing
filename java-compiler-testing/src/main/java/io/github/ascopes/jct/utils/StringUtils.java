/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.jspecify.annotations.Nullable;

/**
 * Utilities for string manipulation used internally in this library.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class StringUtils extends UtilityClass {

  // Number formatting stuff
  private static final BigDecimal THOUSAND = BigDecimal.valueOf(1_000);
  private static final List<String> TIME_UNITS = List.of("ns", "µs", "ms", "s");
  private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.##");

  // Other stuff
  private static final char LF = '\n';
  private static final String NULL_STRING = "null";
  private static final String EMPTY_STRING = "";

  private StringUtils() {
    // Disallow initialisation.
  }

  /**
   * Take the given list of strings and produce a connected single string, separating all but the
   * last element by the {@code connector} value, and the last element by the {@code lastConnector}
   * element.
   *
   * <p>This is designed to be able to take an input such as {@code List.of("foo", "bar", "baz")},
   * and be able to produce a string result such as {@code "foo, bar, or baz"} (where
   * {@code connector} in this case would be {@code ", "} and {@code lastConnector} would be
   * {@code ", or "}).
   *
   * <p>If no arguments are available, then an empty string is output instead.
   *
   * <p>Effectively, this is a more complex version of
   * {@link String#join(CharSequence, CharSequence...)}.
   *
   * @param words         the words to connect.
   * @param connector     the connector string to use.
   * @param lastConnector the last connector string to use.
   * @return the resulting string.
   */
  public static String toWordedList(
      List<? extends CharSequence> words,
      CharSequence connector,
      CharSequence lastConnector
  ) {
    if (words.isEmpty()) {
      return EMPTY_STRING;
    }

    if (words.size() == 1) {
      return words.get(0).toString();
    }

    var builder = new StringBuilder(words.get(0));
    var index = 1;

    for (; index < words.size() - 1; ++index) {
      builder.append(connector).append(words.get(index));
    }

    return builder
        .append(lastConnector)
        .append(words.get(index))
        .toString();
  }

  /**
   * Left-pad the given content with the given padding char until it is the given length.
   *
   * @param content     the content to process.
   * @param length      the max length of the resultant content.
   * @param paddingChar the character to pad with.
   * @return the padded string.
   */
  public static String leftPad(String content, int length, char paddingChar) {
    var builder = new StringBuilder();
    while (builder.length() + content.length() < length) {
      builder.append(paddingChar);
    }
    return builder.append(content).toString();
  }

  /**
   * Find the index for the start of the given line number (1-indexed).
   *
   * <p>This assumes lines use UNIX line endings ({@code '\n'}).
   *
   * <p>The first line number will always be at index 0. If the line is not found, then
   * {@code -1} is returned.
   *
   * @param content    the content to read through.
   * @param lineNumber the 1-indexed line number to find.
   * @return the index of the line.
   */
  public static int indexOfLine(String content, int lineNumber) {
    var currentLine = 1;
    var index = 0;
    var length = content.length();

    for (; currentLine < lineNumber && index < length; ++index) {
      if (content.charAt(index) == LF) {
        ++currentLine;
      }
    }

    return currentLine == lineNumber
        ? index
        : -1;
  }

  /**
   * Find the index of the next UNIX end of line ({@code '\n'}) character from the given offset.
   *
   * <p>If there is no further line feed, then the length of the string is returned.
   *
   * @param content the content to read through.
   * @param startAt the 0-indexed position to start at in the string.
   * @return the index of the end of line or end of string, whichever comes first.
   */
  public static int indexOfEndOfLine(String content, int startAt) {
    var index = content.indexOf(LF, startAt);
    return index == -1 ? content.length() : index;
  }

  /**
   * Format a given number of nanoseconds into a string with a meaningful time unit, and then return
   * it.
   *
   * @param nanos the nanosecond time to format.
   * @return the formatted string, in the format {@code {time}{unit}}.
   */
  public static String formatNanos(long nanos) {
    var duration = BigDecimal.valueOf(nanos);
    var index = 0;
    for (; duration.compareTo(THOUSAND) >= 0 && index < TIME_UNITS.size() - 1; ++index) {
      duration = duration.divide(
          THOUSAND,
          TIME_FORMAT.getMaximumFractionDigits(),
          RoundingMode.HALF_UP
      );
    }
    return TIME_FORMAT.format(duration) + TIME_UNITS.get(index);
  }

  /**
   * Wrap the string representation of the given argument in double-quotes.
   *
   * <p>Backslashes will be doubled (i.e. '\' will become '\\'), and double-quotes
   * within the string representation of the object will have a backslash prefixed (i.e. '"' will
   * become '\"').
   *
   * @param object the object to produce a quoted string representation of.
   * @return the string representation, surrounded by double quotes.
   */
  public static String quoted(Object object) {
    var builder = new StringBuilder();
    appendQuoted(builder, object);
    return builder.toString();
  }

  /**
   * Produce a string representation of the iterable, quoting each member.
   *
   * @param iterable the iterable to process.
   * @return the string representation of the iterable, with each member quoted (unless null).
   */
  public static String quotedIterable(@Nullable Iterable<?> iterable) {
    if (iterable == null) {
      return NULL_STRING;
    }

    var builder = new StringBuilder("[");
    var first = true;

    for (var item : iterable) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      appendQuoted(builder, item);
    }

    return builder.append(']').toString();
  }

  private static void appendQuoted(StringBuilder builder, @Nullable Object object) {
    if (object == null) {
      builder.append(NULL_STRING);
      return;
    }

    var objectStr = Objects.toString(object);

    builder.append('"');

    for (var i = 0; i < objectStr.length(); ++i) {
      var c = objectStr.charAt(i);
      switch (c) {
        case '\\':
          builder.append("\\\\");
          break;
        case '"':
          builder.append("\\\"");
          break;
        default:
          builder.append(c);
      }
    }

    builder.append('"');
  }

  /**
   * Make an assertion error message that reports a given object was not found somewhere, and then
   * try to list some close matches below that message, if any relevant close matches were found.
   *
   * @param stringToLocate          the object we tried to look for represented as a string.
   * @param userFriendlyString      a user-friendly string representation of the object we tried to
   *                                locate.
   * @param existingObjects         existing objects we know about.
   * @param comparisonStringifier   a function to produce a string representation of an object that
   *                                can be used to perform fuzzy string comparison on.
   * @param userFriendlyStringifier a function to produce a human-readable string representation of
   *                                an object to display in an error message.
   * @param entityTypeName          the name of the entity type that could not be found.
   * @param <T>                     the type being looked for.
   * @return the assertion error to throw.
   */
  public static <T> String resultNotFoundWithFuzzySuggestions(
      String stringToLocate,
      String userFriendlyString,
      Collection<T> existingObjects,
      ToStringFunction<T> comparisonStringifier,
      Function<T, String> userFriendlyStringifier,
      String entityTypeName
  ) {
    var errorBuilder = new StringBuilder();
    errorBuilder
        .append("No ")
        .append(entityTypeName)
        .append(" matching ")
        .append(userFriendlyString)
        .append(" was found.");

    var closestMatches = FuzzySearch
        .extractSorted(stringToLocate, existingObjects, comparisonStringifier, 75)
        .stream()
        .limit(5)
        .map(BoundExtractedResult::getReferent)
        .map(userFriendlyStringifier)
        .sorted()
        .toList();

    if (!closestMatches.isEmpty()) {
      errorBuilder.append(" Maybe you meant:");

      for (var match : closestMatches) {
        errorBuilder
            .append(LF)
            .append("  - ")
            .append(match);
      }
    }

    return errorBuilder.toString();
  }
}
