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

package io.github.ascopes.jct.testing.unit.intern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.of;

import io.github.ascopes.jct.intern.StringUtils;
import io.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
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
class StringUtilsTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return StringUtils.class;
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

  @DisplayName("quotedIterable() returns the expected value")
  @MethodSource("iterableCases")
  @ParameterizedTest(name = "where a {0} <{1}> is expected to return \"{2}\"")
  void quotedIterableReturnsExpectedValue(Type ignored, Iterable<?> input, String expected) {
    // Then
    then(StringUtils.quotedIterable(input)).isEqualTo(expected);
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
