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
package io.github.ascopes.jct.testing.unit.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.testing.helpers.ConcurrentRuns;
import io.github.ascopes.jct.testing.helpers.ThreadPool;
import io.github.ascopes.jct.testing.helpers.ThreadPool.RunTestsInIsolation;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.Lazy.ThrowingConsumer;
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
  @ParameterizedTest(name = "for {0} concurrent read(s)")
  @RunTestsInIsolation
  @Timeout(30)
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
  @ParameterizedTest(name = "with \"{0}\" expected to return \"{1}\"")
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
    then(actual).isEqualTo("Lazy{data=null, initialized=false}");
  }

  @DisplayName("ifInitialized() calls the callable when initialized")
  @Test
  void ifInitializedCallsTheCallableWhenInitialized() {
    // Given
    var initializer = (Supplier<Object>) mock(Supplier.class);
    when(initializer.get()).thenReturn(new Object());
    var lazy = new Lazy<>(initializer);
    var callback = (ThrowingConsumer<Object, RuntimeException>) mock(ThrowingConsumer.class);

    // When
    var actual = lazy.access();
    lazy.ifInitialized(callback);

    // Then
    verify(callback).consume(actual);
  }

  @DisplayName("ifInitialized() propagates exceptions when initialized")
  @Test
  void ifInitializedPropagatesExceptions() {
    // Given
    var initializer = (Supplier<Object>) mock(Supplier.class);
    when(initializer.get()).thenReturn(new Object());
    var lazy = new Lazy<>(initializer);
    var callback = (ThrowingConsumer<Object, RuntimeException>) mock(ThrowingConsumer.class);
    var ex = new IllegalArgumentException("bang bang");
    doThrow(ex).when(callback).consume(any());

    // When
    var actual = lazy.access();
    assertThatThrownBy(() -> lazy.ifInitialized(callback))
        .isSameAs(ex);

    // Then
    verify(callback).consume(actual);
  }

  @DisplayName("ifInitialized() does not call the callable when not initialized")
  @Test
  void ifInitializedDoesNotCallTheCallableWhenNotInitialized() {
    // Given
    var initializer = (Supplier<Object>) mock(Supplier.class);
    when(initializer.get()).thenReturn(new Object());
    var lazy = new Lazy<>(initializer);
    var callback = (ThrowingConsumer<Object, RuntimeException>) mock(ThrowingConsumer.class);

    // When
    lazy.access();
    lazy.destroy();
    lazy.ifInitialized(callback);

    // Then
    verifyNoInteractions(callback);
  }

  static Stream<Arguments> toStringInitializedCases() {
    return Stream.of(
        Arguments.of(null, "Lazy{data=null, initialized=true}"),
        Arguments.of(new Something(), "Lazy{data=Something{}, initialized=true}"),
        Arguments.of("Hello, World!", "Lazy{data=\"Hello, World!\", initialized=true}")
    );
  }

  static class Something {

    @Override
    public String toString() {
      return "Something{}";
    }
  }
}
