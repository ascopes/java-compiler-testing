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

package com.github.ascopes.jct.test.intern;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.of;

import com.github.ascopes.jct.intern.StringUtils;
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
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link StringUtils} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("StringUtils tests")
class StringUtilsTest {

  @DisplayName("quoted() returns the expected value")
  @MethodSource("singleObjectCases")
  @ParameterizedTest(name = "quoted() returns \"{2}\" for input <{1}> of type {0}")
  void quotedReturnsExpectedValue(Type ignored, Object input, String expected) {
    // Then
    then(StringUtils.quoted(input)).isEqualTo(expected);
  }

  @DisplayName("quotedIterable() returns the expected value")
  @MethodSource("iterableCases")
  @ParameterizedTest(name = "quotedIterable() returns \"{2}\" for input {1} of type {0}")
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
