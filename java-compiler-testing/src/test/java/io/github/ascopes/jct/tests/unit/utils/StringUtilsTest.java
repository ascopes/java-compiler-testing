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
package io.github.ascopes.jct.tests.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.of;

import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.utils.StringUtils;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link StringUtils} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("StringUtils tests")
class StringUtilsTest implements UtilityClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return StringUtils.class;
  }

  @DisplayName("toWordedList() returns the expected results")
  @MethodSource("toWordedListCases")
  @ParameterizedTest(name = "expect {0} to produce <{3}> when separated by <{1}> and <{2}>")
  void toWordedListReturnsTheExpectedResults(
      List<String> inputs,
      String connector,
      String lastConnector,
      String expected
  ) {
    // When
    var actual = StringUtils.toWordedList(inputs, connector, lastConnector);

    // Then
    assertThat(actual).isEqualTo(expected);
  }

  static Stream<Arguments> toWordedListCases() {
    return Stream.of(
        of(List.of(), ", ", ", and ", ""),
        of(List.of("foo"), ", ", ", and ", "foo"),
        of(List.of("foo", "bar"), ", ", ", and ", "foo, and bar"),
        of(List.of("foo", "bar", "baz"), ", ", ", and ", "foo, bar, and baz"),
        of(List.of("foo", "bar", "baz", "bork"), ", ", ", and ", "foo, bar, baz, and bork"),

        of(List.of(), ", or ", ", or even ", ""),
        of(List.of("foo"), ", or ", ", or even ", "foo"),
        of(List.of("foo", "bar"), ", or ", ", or even ", "foo, or even bar"),
        of(List.of("foo", "bar", "baz"), ", or ", ", or even ", "foo, or bar, or even baz")
    );
  }

  @DisplayName("leftPad() pads the string on the left")
  @CsvSource({
      "'foo', -1, 'x', 'foo'",
      "'foo',  0, 'x', 'foo'",
      "'foo',  1, 'x', 'foo'",
      "'foo',  2, 'x', 'foo'",
      "'foo',  3, 'x', 'foo'",
      "'foo',  4, 'x', 'xfoo'",
      "'foo',  5, 'x', 'xxfoo'",
      "'foo',  5, ' ', '  foo'",

  })
  @ParameterizedTest(name = "expect leftPad(\"{0}\", {1}, ''{2}'') to return \"{3}\"")
  void leftPadWillPadTheStringOnTheLeft(
      String input,
      int length,
      char paddingChar,
      String expected
  ) {
    // When
    var actual = StringUtils.leftPad(input, length, paddingChar);

    // Then
    assertThat(actual).isEqualTo(expected);
  }

  @DisplayName("indexOfLine() returns the expected value")
  @CsvSource({
      "'', 1, 0",
      "'foo', 1, 0",
      "'foo', 2, -1",
      "'foo\nbar', 1, 0",
      "'foo\nbar', 2, 4",
      "'foo\nbar', 3, -1",
      "'hello\nworld\n\nblahblah\nblah\n', 1, 0",
      "'hello\nworld\n\nblahblah\nblah\n', 2, 6",
      "'hello\nworld\n\nblahblah\nblah\n', 3, 12",
      "'hello\nworld\n\nblahblah\nblah\n', 4, 13",
      "'hello\nworld\n\nblahblah\nblah\n', 5, 22",
      "'hello\nworld\n\nblahblah\nblah\n', 6, 27",
      "'hello\nworld\n\nblahblah\nblah\n', 7, -1",
      "'hello\nworld\n\nblahblah\nblah\n', 100_000, -1",
  })
  @ParameterizedTest(name = "indexOfLine(..., {1}) returns {2}")
  void indexOfLineReturnsExpectedValue(String input, int line, int expectedIndex) {
    // When
    var actualIndex = StringUtils.indexOfLine(input, line);

    // Then
    assertThat(actualIndex)
        .withFailMessage(
            "Incorrect index given. Expected %s, got %s.\nCase was for line %s in input:\n%s",
            expectedIndex,
            actualIndex,
            line,
            input
        )
        .isEqualTo(expectedIndex);
  }

  @DisplayName("indexOfEndOfLine() returns the expected value")
  @CsvSource({
      "'', 1, 0",
      "'foo', 0, 3",
      "'foo', 1, 3",
      "'foo', 2, 3",
      "'foo', 3, 3",
      "'foo', 4, 3",
      "'foo\nbar', 1, 3",
      "'foo\nbar', 3, 3",
      "'foo\nbar', 4, 7",
      "'foo\nbar', 5, 7",
      "'foo\nbar', 6, 7",
      "'hello\nworld\n\nblahblah\nblah\n', 4, 5",
      "'hello\nworld\n\nblahblah\nblah\n', 5, 5",
      "'hello\nworld\n\nblahblah\nblah\n', 7, 11",
      "'hello\nworld\n\nblahblah\nblah\n', 11, 11",
      "'hello\nworld\n\nblahblah\nblah\n', 12, 12",
      "'hello\nworld\n\nblahblah\nblah\n', 13, 21",
      "'hello\nworld\n\nblahblah\nblah\n', 21, 21",
      "'hello\nworld\n\nblahblah\nblah\n', 22, 26",
      "'hello\nworld\n\nblahblah\nblah\n', 26, 26",
      "'hello\nworld\n\nblahblah\nblah\n', 27, 27",
      "'hello\nworld\n\nblahblah\nblah\n', 100_000, 27",
  })
  @ParameterizedTest(name = "indexOfEndOfLine(..., {1}) returns {2}")
  void indexOfEndOfLineReturnsExpectedValue(String input, int startAt, int expectedIndex) {
    // When
    var actualIndex = StringUtils.indexOfEndOfLine(input, startAt);

    // Then
    assertThat(actualIndex)
        .withFailMessage(
            "Incorrect index given. Expected %s, got %s.\n"
                + "Case was for starting at index %s in input:\n%s",
            expectedIndex,
            actualIndex,
            startAt,
            input
        )
        .isEqualTo(expectedIndex);
  }

  @DisplayName("formatNanos() returns the expected value")
  @CsvSource({
      "0, 0ns",
      "1, 1ns",
      "10, 10ns",
      "15, 15ns",
      "100, 100ns",
      "999, 999ns",
      "1_000, 1µs",
      "1_001, 1µs",
      "1_005, 1.01µs",
      "1_010, 1.01µs",
      "1_015, 1.02µs",
      "1_050, 1.05µs",
      "1_100, 1.1µs",
      "1_150, 1.15µs",
      "1_499, 1.5µs",
      "1_500, 1.5µs",
      "1_999, 2µs",
      "2_000, 2µs",
      "999_990, 999.99µs",
      "999_995, 1ms",
      "1_000_000, 1ms",
      "999_990_000, 999.99ms",
      "999_995_000, 1s",
      "1_000_000_000, 1s",
      "999_990_000_000, 999.99s",
      "1_999_990_000_000, 1999.99s",
  })
  @ParameterizedTest(name = "expect {0}L to output \"{1}\"")
  void formatNanosReturnsExpectedValue(long nanos, String expected) {
    // Then
    assertThat(StringUtils.formatNanos(nanos))
        .isEqualTo(expected);
  }

  @DisplayName("quoted() returns the expected value")
  @MethodSource("singleObjectCases")
  @ParameterizedTest(name = "where a {0} <{1}> is expected to return \"{2}\"")
  void quotedReturnsExpectedValue(Type ignored, Object input, String expected) {
    // Then
    then(StringUtils.quoted(input)).isEqualTo(expected);
  }

  static Stream<Arguments> singleObjectCases() {
    return Stream.of(
        of(Object.class, null, "null"),
        of(int.class, 1, "\"1\""),
        of(double.class, 2.718281828459045, "\"2.718281828459045\""),
        of(boolean.class, true, "\"true\""),
        of(boolean.class, false, "\"false\""),
        of(String.class, "", "\"\""),
        of(String.class, "hello", "\"hello\""),
        of(String.class, "Hello, World!", "\"Hello, World!\""),
        of(String.class, "What's up?", "\"What's up?\""),
        of(String.class, "I was like \"hey\"", "\"I was like \\\"hey\\\"\""),
        of(String.class, "I was like \\\"hey\\\"", "\"I was like \\\\\\\"hey\\\\\\\"\""),
        of(
            Thing.class,
            new Thing(),
            "\"Thing{foo=1, bar=\\\"2\\\", bork=\\\"C:\\\\Windows\\\"}\""
        )
    );
  }

  @DisplayName("quotedIterable() returns the expected value")
  @MethodSource("iterableCases")
  @ParameterizedTest(name = "where a {0} <{1}> is expected to return \"{2}\"")
  void quotedIterableReturnsExpectedValue(Type ignored, Iterable<?> input, String expected) {
    // Then
    then(StringUtils.quotedIterable(input)).isEqualTo(expected);
  }

  static Stream<Arguments> iterableCases() {
    return Stream.of(
        of(Iterable.class, null, "null"),
        of(List.class, List.of(), "[]"),
        of(Set.class, Set.of(), "[]"),
        of(LinkedList.class, new LinkedList<>(), "[]"),
        of(Set.class, Collections.singleton(null), "[null]"),
        of(List.class, Arrays.asList(null, null), "[null, null]"),
        of(List.class, Arrays.asList(null, null, null), "[null, null, null]"),
        of(List.class, Arrays.asList(null, true, null), "[null, \"true\", null]"),
        of(List.class, List.of(true, false, true), "[\"true\", \"false\", \"true\"]"),
        of(List.class, List.of(1.1, 2, 3, true), "[\"1.1\", \"2\", \"3\", \"true\"]"),
        of(List.class, List.of(""), "[\"\"]"),
        of(List.class, List.of("", ""), "[\"\", \"\"]"),
        of(List.class, List.of("", "", ""), "[\"\", \"\", \"\"]"),
        of(List.class, List.of("hello"), "[\"hello\"]"),
        of(List.class, List.of("Hello, World!"), "[\"Hello, World!\"]"),
        of(List.class, List.of("What's up?"), "[\"What's up?\"]"),
        of(List.class, List.of("hello", "world"), "[\"hello\", \"world\"]"),
        of(List.class, List.of("like \"hey\""), "[\"like \\\"hey\\\"\"]"),
        of(List.class, List.of("\\\"hey\\\""), "[\"\\\\\\\"hey\\\\\\\"\"]"),
        of(
            List.class,
            List.of(new Thing()),
            "[\"Thing{foo=1, bar=\\\"2\\\", bork=\\\"C:\\\\Windows\\\"}\"]"
        )
    );
  }

  static class Thing {

    @Override
    public String toString() {
      return "Thing{foo=1, bar=\"2\", bork=\"C:\\Windows\"}";
    }
  }
}
