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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.CharBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link ToStringBuilder} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ToStringBuilder tests")
class ToStringBuilderTest {

  @DisplayName("Owner cannot be null")
  @Test
  void ownerCannotBeNull() {
    // Then
    assertThatThrownBy(() -> new ToStringBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("owner must not be null");
  }

  @DisplayName("Empty declarations only apply the class name")
  @Test
  void emptyDeclarationsOnlyApplyTheClassName() {
    // Given
    class EmptyClass {

      @Override
      public String toString() {
        return new ToStringBuilder(this).toString();
      }
    }

    var obj = new EmptyClass();

    // Then
    assertThat(obj).hasToString("EmptyClass{}");
  }

  @DisplayName("Multiple attributes can be added to the toString representation")
  @Test
  void singleAttributesCanBeAddedToTheToStringRepresentation() {
    class AnotherClass {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("foo", "foo")
            .toString();
      }
    }

    var obj = new AnotherClass();

    // Then
    assertThat(obj).hasToString("AnotherClass{foo=\"foo\"}");
  }

  @DisplayName("Multiple attributes can be added to the toString representation")
  @Test
  void multipleAttributesCanBeAddedToTheToStringRepresentation() {
    // Given
    class SomeClass {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("foo", "foo")
            .attribute("bar", "bar")
            .attribute("baz", "baz")
            .toString();
      }
    }

    var obj = new SomeClass();

    // Then
    assertThat(obj).hasToString("SomeClass{foo=\"foo\", bar=\"bar\", baz=\"baz\"}");
  }

  @DisplayName("Unhandled objects can be added to the toString representation")
  @Test
  void unhandledObjectsCanBeAddedToTheToStringRepresentation() {
    var innerObj = new Object();

    class YetAnotherClass {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("innerObj", innerObj)
            .toString();
      }
    }

    var obj = new YetAnotherClass();

    // Then
    assertThat(obj).hasToString("YetAnotherClass{innerObj=" + innerObj + "}");
  }

  @DisplayName("Primitives escape correctly")
  @MethodSource("primitiveRepresentations")
  @ParameterizedTest(name = "{0} escapes to {1}")
  void primitivesGetEscapedCorrectly(Object primitive, String expected) {
    // Given
    class Florb {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("primitive", primitive)
            .toString();
      }
    }

    var florb = new Florb();

    // Then
    assertThat(florb)
        .hasToString("Florb{primitive=" + expected + "}");
  }

  static Stream<Arguments> primitiveRepresentations() {
    return Stream.of(
        Arguments.of(true, "true"),
        Arguments.of(false, "false"),
        Arguments.of((byte) 0, "0"),
        Arguments.of((byte) 1, "1"),
        Arguments.of((byte) 100, "100"),
        Arguments.of((short) -103, "-103"),
        Arguments.of((short) 104, "104"),
        Arguments.of(-1_066, "-1066"),
        Arguments.of(80_010, "80010"),
        Arguments.of(-271_828_182_845L, "-271828182845"),
        Arguments.of(271_828_182_845L, "271828182845"),
        Arguments.of(1.234F, Float.toString(1.234F)),
        Arguments.of(1234F, Float.toString(1234F)),
        Arguments.of(-1.234E-5F, Float.toString(-1.234E-5F)),
        Arguments.of(1.234D, Double.toString(1.234D)),
        Arguments.of(12340.001E6D, Double.toString(12340.001E6D)),
        Arguments.of(-1.234E-5D, Double.toString(-1.234E-5D)),
        Arguments.of(1.23E4D, Double.toString(1.23E4D)),
        Arguments.of('c', "c")
    );
  }

  @DisplayName("Strings get escaped correctly")
  @MethodSource("stringRepresentations")
  @ParameterizedTest(name = "\"{0}\" escapes to {1}")
  void stringsGetEscapedCorrectly(String input, String expected) {
    // Given
    class Foo {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("input", input)
            .toString();
      }
    }

    var foo = new Foo();

    // Then
    assertThat(foo)
        .hasToString("Foo{input=" + expected + "}");
  }

  @DisplayName("CharSequences get escaped correctly")
  @MethodSource("stringRepresentations")
  @ParameterizedTest(name = "\"{0}\" escapes to {1}")
  void charSequencesGetEscapedCorrectly(String input, String expected) {
    // Given
    class Bar {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("input", CharBuffer.wrap(input))
            .toString();
      }
    }

    var bar = new Bar();

    // Then
    assertThat(bar)
        .hasToString("Bar{input=" + expected + "}");
  }

  static Stream<Arguments> stringRepresentations() {
    return Stream.of(
        // (input, expected output)
        Arguments.of("hello, world!", "\"hello, world!\""),
        Arguments.of("\r\n\t\0hello\r\n\t\0", "\"\\r\\n\\t\\0hello\\r\\n\\t\\0\""),
        Arguments.of("hello, m'lady", "\"hello, m'lady\""),
        Arguments.of("quotes: \"hello!\" <--", "\"quotes: \\\"hello!\\\" <--\""),
        Arguments.of("back slash: \\ <--", "\"back slash: \\\\ <--\""),
        Arguments.of("form feed: \f <--", "\"form feed: \\u000c <--\""),
        Arguments.of("\1", "\"\\u0001\""),
        Arguments.of("\u000b", "\"\\u000b\""),
        Arguments.of("\u007f", "\"\\u007f\""),
        Arguments.of(" !#$%'()*+,-./", "\" !#$%'()*+,-./\""),
        Arguments.of("0123456789", "\"0123456789\""),
        Arguments.of("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\""),
        Arguments.of("abcdefghijklmnopqrstuvwxyz", "\"abcdefghijklmnopqrstuvwxyz\""),
        Arguments.of(":;<=>?@", "\":;<=>?@\""),
        Arguments.of("[]^_`", "\"[]^_`\""),
        Arguments.of("{|}", "\"{|}\""),
        Arguments.of(
            "¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑíîïðñòóôõö÷øùúûüýþÿ",
            "\"¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑíîïðñòóôõö÷øùúûüýþÿ\""
        ),
        Arguments.of("€", "\"\\u20ac\""),
        Arguments.of("😁", "\"\\ud83d\\ude01\"")
    );
  }

  @DisplayName("Arrays get escaped correctly")
  @MethodSource("arrayRepresentations")
  @ParameterizedTest(name = "{0} escapes to {1}")
  void arraysGetEscapedCorrectly(Object array, String expected) {
    // Given
    class Baz {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("array", array)
            .toString();
      }
    }

    var baz = new Baz();

    // Then
    assertThat(baz)
        .hasToString("Baz{array=" + expected + "}");
  }

  static Stream<Arguments> arrayRepresentations() {
    return Stream.of(
        Arguments.of(new Object[0], "[]"),
        Arguments.of(new String[]{"foo"}, "[\"foo\"]"),
        Arguments.of(
            new String[]{"foo", "bar", "baz", "\t", "\ud83d\ude01"},
            "[\"foo\", \"bar\", \"baz\", \"\\t\", \"\\ud83d\\ude01\"]"
        ),
        Arguments.of(new byte[]{1, 2, 3, 4}, "[1, 2, 3, 4]"),
        Arguments.of(new short[]{1, 2, 3, 4}, "[1, 2, 3, 4]"),
        Arguments.of(new int[]{1, 2, 3, 4}, "[1, 2, 3, 4]"),
        Arguments.of(new long[]{1, 2, 3, 4}, "[1, 2, 3, 4]"),
        Arguments.of(new char[]{'h', 'e', 'l', 'l', 'o'}, "[h, e, l, l, o]"),
        Arguments.of(new boolean[]{true, false, true}, "[true, false, true]"),
        Arguments.of(new float[]{1.23e-4f}, "[" + 1.23e-4f + "]"),
        Arguments.of(new double[]{1.23e4}, "[" + 1.23e4 + "]")
    );
  }

  @DisplayName("Lists get escaped correctly")
  @MethodSource("listRepresentations")
  @ParameterizedTest(name = "{0} escapes to {1}")
  void listsGetEscapedCorrectly(List<?> list, String expected) {
    // Given
    class Smurf {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("list", list)
            .toString();
      }
    }

    var smurf = new Smurf();

    // Then
    assertThat(smurf)
        .hasToString("Smurf{list=" + expected + "}");
  }

  static Stream<Arguments> listRepresentations() {
    return Stream.of(
        Arguments.of(List.of(), "[]"),
        Arguments.of(List.of("foo"), "[\"foo\"]"),
        Arguments.of(
            List.of("foo", "bar", "baz", "\t", "\ud83d\ude01"),
            "[\"foo\", \"bar\", \"baz\", \"\\t\", \"\\ud83d\\ude01\"]"
        ),
        Arguments.of(List.of((byte) 1, 2, 3, 4), "[1, 2, 3, 4]"),
        Arguments.of(List.of((short) 1, 2, 3, 4), "[1, 2, 3, 4]"),
        Arguments.of(List.of(1, 2, 3, 4), "[1, 2, 3, 4]"),
        Arguments.of(List.of(1L, 2L, 3L, 4L), "[1, 2, 3, 4]"),
        Arguments.of(List.of('h', 'e', 'l', 'l', 'o'), "[h, e, l, l, o]"),
        Arguments.of(List.of(true, false, true), "[true, false, true]"),
        Arguments.of(List.of(1.23e-4f), "[" + 1.23e-4f + "]"),
        Arguments.of(List.of(1.23e4), "[" + 1.23e4 + "]")
    );
  }

  @DisplayName("Maps get escaped correctly")
  @MethodSource("mapRepresentations")
  @ParameterizedTest(name = "{0} escapes to {1}")
  void mapsGetEscapedCorrectly(Map<?, ?> map, String expected) {
    // Given
    class Flannimal {

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attribute("map", map)
            .toString();
      }
    }

    var flannimal = new Flannimal();

    // Then
    assertThat(flannimal)
        .hasToString("Flannimal{map=" + expected + "}");
  }

  static Stream<Arguments> mapRepresentations() {
    return Stream.of(
        Arguments.of(
            orderedMapOf("foo", "bar", "baz", 123, "qux", false),
            "{\"foo\"=\"bar\", \"baz\"=123, \"qux\"=false}"
        ),
        Arguments.of(orderedMapOf(), "{}")
    );
  }

  static Map<Object, Object> orderedMapOf(Object... objects) {
    var map = new LinkedHashMap<>();
    for (var i = 0; i < objects.length; i += 2) {
      map.put(objects[i], objects[i + 1]);
    }
    return map;
  }
}
