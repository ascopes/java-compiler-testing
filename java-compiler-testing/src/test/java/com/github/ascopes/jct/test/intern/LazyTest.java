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
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.ascopes.jct.intern.Lazy;
import com.github.ascopes.jct.test.helpers.ConcurrentRuns;
import com.github.ascopes.jct.test.helpers.ThreadPool;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link Lazy} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Lazy tests")
@Execution(ExecutionMode.CONCURRENT)
@SuppressWarnings("unchecked")
class LazyTest {

  @DisplayName("Initializing with a null supplier throws a NullPointerException")
  @Test
  void initializingWithNullSupplierThrowsNullPointerException() {
    thenCode(() -> new Lazy<>(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("access() returns the cached result")
  @Test
  void accessReturnsTheCachedResult() {
    // Given
    var value = new Object();

    var supplier = (Supplier<Object>) mock(Supplier.class);
    when(supplier.get()).thenReturn(value);

    var lazy = new Lazy<>(supplier);

    // When
    var firstResult = lazy.access();
    var secondResult = lazy.access();
    var thirdResult = lazy.access();

    // Then
    verify(supplier, times(1)).get();
    then(firstResult).isSameAs(value);
    then(secondResult).isSameAs(value);
    then(thirdResult).isSameAs(value);
  }

  @DisplayName("access() synchronizes correctly")
  @ConcurrentRuns
  @ParameterizedTest(name = "access() synchronizes correctly for {0} concurrent reads")
  @Timeout(10)
  void accessSynchronizesCorrectly(int concurrency) {
    try (var executor = new ThreadPool(concurrency)) {
      // Given
      var value = new Object();

      var supplier = (Supplier<Object>) mock(Supplier.class);
      when(supplier.get()).thenReturn(value);

      var lazy = new Lazy<>(supplier);

      Callable<Object> accession = () -> {
        var skew = (Math.random() - 0.5) * 50;
        Thread.sleep(100 + (int) skew);
        return lazy.access();
      };

      var tasks = Stream
          .generate(() -> accession)
          .limit(concurrency)
          .collect(Collectors.toList());

      // When
      var results = executor.awaitingAll(tasks).join();

      // Then
      verify(supplier, times(1)).get();

      then(results)
          .hasSize(concurrency)
          .allMatch(value::equals);
    }
  }

  @DisplayName("access returns a new value when the lazy is destroyed")
  @Test
  void accessReturnsNewValueWhenLazyIsDestroyed() {
    // Given
    final var firstValue = new Object();
    final var secondValue = new Object();

    final var supplier = (Supplier<Object>) mock(Supplier.class);
    when(supplier.get()).thenReturn(firstValue, secondValue);

    final var lazy = new Lazy<>(supplier);

    // When
    final var firstResult = lazy.access();
    final var secondResult = lazy.access();
    lazy.destroy();
    final var thirdResult = lazy.access();
    final var fourthResult = lazy.access();

    // Then
    verify(supplier, times(2)).get();
    then(firstResult).isSameAs(firstValue);
    then(secondResult).isSameAs(firstValue);
    then(thirdResult).isSameAs(secondValue);
    then(fourthResult).isSameAs(secondValue);
  }

  @DisplayName("toString() returns the expected values when initialized")
  @MethodSource("toStringInitializedCases")
  @ParameterizedTest(name = "toString() returns \"{1}\" for object \"{0}\" when initialized")
  void toStringReturnsExpectedValuesWhenInitialized(Object value, String expected) {
    // Given
    var supplier = (Supplier<Object>) mock(Supplier.class);
    when(supplier.get()).thenReturn(value);
    var lazy = new Lazy<>(supplier);
    lazy.access();

    // When
    var actual = lazy.toString();

    // Then
    then(actual).isEqualTo(expected);
  }

  @DisplayName("toString() returns the expected values when uninitialized")
  @Test
  void toStringReturnsExpectedValuesWhenUninitialized() {
    // Given
    var supplier = (Supplier<Object>) mock(Supplier.class);
    when(supplier.get()).thenReturn(new Object());
    var lazy = new Lazy<>(supplier);
    lazy.destroy();

    // When
    var actual = lazy.toString();

    // Then
    then(actual).isEqualTo("Lazy{data=<uninitialized>}");
  }

  static Stream<Arguments> toStringInitializedCases() {
    return Stream.of(
        Arguments.of(null, "Lazy{data=null}"),
        Arguments.of(new Something(), "Lazy{data=Something{}}"),
        Arguments.of("Hello, World!", "Lazy{data=\"Hello, World!\"}")
    );
  }

  static class Something {

    @Override
    public String toString() {
      return "Something{}";
    }
  }
}
