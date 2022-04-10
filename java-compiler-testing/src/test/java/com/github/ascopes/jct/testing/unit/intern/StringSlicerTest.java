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

package com.github.ascopes.jct.testing.unit.intern;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;

import com.github.ascopes.jct.intern.StringSlicer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link StringSlicer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("StringSlicer tests")
class StringSlicerTest {

  @DisplayName("Initializing with a null delimiter throws a NullPointerException")
  @Test
  void initializingWithNullDelimiterThrowsNullPointerException() {
    thenCode(() -> new StringSlicer(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("Initializing with an empty delimiter throws an IllegalArgumentException")
  @Test
  void initializingWithAnEmptyDelimiterThrowsAnIllegalArgumentException() {
    thenCode(() -> new StringSlicer(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot split on a zero-length string");
  }

  @DisplayName("Slicing a string into an array returns the expected result")
  @MethodSource("sliceCases")
  @ParameterizedTest(name = "where slicing \"{1}\" with \"{0}\" is expected to return {2}")
  void slicingStringIntoArrayReturnsTheExpectedResult(
      String delimiter,
      String input,
      List<String> expected
  ) {
    // Given
    var slicer = new StringSlicer(delimiter);

    // When
    var actual = List.of(slicer.splitToArray(input));

    // Then
    then(actual).isEqualTo(expected);
  }

  @DisplayName("Slicing a string into a stream returns the expected result")
  @MethodSource("sliceCases")
  @ParameterizedTest(name = "where slicing \"{1}\" with \"{0}\" is expected to return {2}")
  void slicingStringIntoStreamReturnsTheExpectedResult(
      String delimiter,
      String input,
      List<String> expected
  ) {
    // Given
    var slicer = new StringSlicer(delimiter);

    // When
    var actual = slicer
        .splitToStream(input)
        .collect(Collectors.toList());

    // Then
    then(actual).isEqualTo(expected);
  }

  @DisplayName("toString returns the expected result")
  @MethodSource("toStringCases")
  @ParameterizedTest(name = "for delimiter = \"{0}\"")
  void toStringReturnsTheExpectedResult(String delimiter, String expected) {
    // Given
    var slicer = new StringSlicer(delimiter);

    // When
    var actual = slicer.toString();

    // Then
    then(actual).isEqualTo(expected);
  }

  static Stream<Arguments> sliceCases() {
    return Stream.of(
        Arguments.of(".", "", List.of("")),
        Arguments.of(".", "a", List.of("a")),
        Arguments.of(".", "aa", List.of("aa")),
        Arguments.of(".", ".", List.of("", "")),
        Arguments.of(".", "a.", List.of("a", "")),
        Arguments.of(".", ".a", List.of("", "a")),
        Arguments.of(".", "a.b", List.of("a", "b")),
        Arguments.of(".", "ab.cde.fghij", List.of("ab", "cde", "fghij")),
        Arguments.of("$", "$eeingdollar$ign$", List.of("", "eeingdollar", "ign", "")),
        Arguments.of("!?", "hello!?world!", List.of("hello", "world!")),
        Arguments.of(
            ".",
            "com.github.ascopes.jct.intern.StringSlicer",
            List.of(
                "com",
                "github",
                "ascopes",
                "jct",
                "intern",
                "StringSlicer"
            )
        )
    );
  }

  static Stream<Arguments> toStringCases() {
    return Stream.of(
        Arguments.of(".", "StringSlicer{delimiter=\".\"}"),
        Arguments.of("\"", "StringSlicer{delimiter=\"\\\"\"}"),
        Arguments.of("!?", "StringSlicer{delimiter=\"!?\"}")
    );
  }
}
