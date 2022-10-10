/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.testing.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.compilers.FlagBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;

/**
 * Helper methods for writing flag builder tests.
 *
 * @param <T> the implementation type
 * @author Ashley Scopes
 */
@SuppressWarnings("SameParameterValue")
public abstract class FlagBuilderTestSupport<T extends FlagBuilder> {

  protected abstract T initialize();

  protected Stream<DynamicTest> flagAddedIfEnabled(
      String name,
      String flag,
      BiFunction<T, Boolean, ?> setter
  ) {
    return Stream.of(
        DynamicTest.dynamicTest(
            String.format("'%s' is set when %s enabled", flag, name),
            () -> {
              var flagBuilder = initialize();
              setter.apply(flagBuilder, true);
              assertThat(flagBuilder.build()).singleElement().isEqualTo(flag);
            }
        ),
        DynamicTest.dynamicTest(
            String.format("'%s' is not set when %s disabled", flag, name),
            () -> {
              var flagBuilder = initialize();
              setter.apply(flagBuilder, false);
              assertThat(flagBuilder.build()).isEmpty();
            }
        )
    );
  }

  protected Stream<DynamicTest> flagAddedIfDisabled(
      String name,
      String flag,
      BiFunction<T, Boolean, ?> setter
  ) {
    return Stream.of(
        DynamicTest.dynamicTest(
            String.format("'%s' is not set when %s enabled", flag, name),
            () -> {
              var flagBuilder = initialize();
              setter.apply(flagBuilder, true);
              assertThat(flagBuilder.build()).isEmpty();
            }
        ),
        DynamicTest.dynamicTest(
            String.format("'%s' set when %s disabled", flag, name),
            () -> {
              var flagBuilder = initialize();
              setter.apply(flagBuilder, false);
              assertThat(flagBuilder.build()).singleElement().isEqualTo(flag);
            }
        )
    );
  }

  @SafeVarargs
  protected final <U> Stream<DynamicTest> argAddedIfProvided(
      String name,
      String flag,
      BiFunction<T, U, ?> setter,
      Function<U, String> toString,
      U... values
  ) {
    return Stream
        .of(values)
        .map(value -> DynamicTest.dynamicTest(
            String.format(
                "'%s %s' is set when %s is provided with value '%s'",
                flag, value, name, value
            ),
            () -> {
              var flagBuilder = initialize();
              setter.apply(flagBuilder, value);
              assertThat(flagBuilder.build()).containsExactly(flag, toString.apply(value));
            }
        ));
  }

  @SafeVarargs
  protected final <U> Stream<DynamicTest> flagWithArgsSetIfProvided(
      String name,
      String flag,
      BiFunction<T, List<U>, ?> setter,
      Function<U, String> toString,
      U... values
  ) {
    var cases = Stream.<DynamicTest>builder();
    for (var end = 0; end < values.length + 1; ++end) {
      var args = Arrays.combineOneOrMore(values).subList(0, end);
      var expected = args
          .stream()
          .map(toString)
          .map(flag::concat)
          .collect(Collectors.toList());

      var caseName = String.format(
          "%s should be set when %s is provided with %s",
          expected,
          name,
          args
      );

      cases.add(DynamicTest.dynamicTest(caseName, () -> {
        var flagBuilder = initialize();
        setter.apply(flagBuilder, args);
        assertThat(flagBuilder.build()).containsExactlyElementsOf(expected);
      }));
    }

    return cases.build();
  }

  protected final Stream<DynamicTest> otherArgsAddedWhenProvided(
      BiFunction<T, List<String>, ?> setter,
      String... values
  ) {
    var cases = Stream.<DynamicTest>builder();
    for (var end = 0; end < values.length + 1; ++end) {
      var args = Arrays.combineOneOrMore(values).subList(0, end);

      var caseName = String.format("%s should be set when provided", args);

      cases.add(DynamicTest.dynamicTest(caseName, () -> {
        var flagBuilder = initialize();
        setter.apply(flagBuilder, args);
        assertThat(flagBuilder.build()).containsExactlyElementsOf(args);
      }));
    }

    return cases.build();
  }
}
