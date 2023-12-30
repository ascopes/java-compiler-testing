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
package io.github.ascopes.jct.tests.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.Lazy.ThrowingConsumer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link Lazy} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Lazy tests")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Time-sensitive tests")
@SuppressWarnings("unchecked")
class LazyTest {

  @DisplayName("Initialising with a null supplier throws a NullPointerException")
  @Test
  @Timeout(15)
  void initialisingWithNullSupplierThrowsNullPointerException() {
    thenCode(() -> new Lazy<>(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("access() returns the cached result")
  @RepeatedTest(5)
  @Timeout(15)
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

  @DisplayName("access() synchronizes correctly on initial accesses")
  @MethodSource("concurrentRepeatCases")
  @ParameterizedTest(name = "for {0} concurrent read(s)")
  @Timeout(15)
  void accessSynchronizesCorrectlyOnInitialAccesses(int concurrency) {
    // This is closeable in Java 19, but not before.
    @SuppressWarnings("resource")
    var executor = Executors.newFixedThreadPool(concurrency);

    try {
      // Given
      var value = new Object();

      Supplier<Object> supplier = mock();

      when(supplier.get()).then(ctx -> {
        // Lagging here should make the inner condition in the acquire block hit both control
        // paths.
        var skew = (Math.random() - 0.5) * 50;
        Thread.sleep(100 + (int) skew);
        return value;
      });

      var lazy = new Lazy<>(supplier);

      Function<CompletableFuture<Object>, Callable<Object>> accession = future -> () -> {
        var skew = (Math.random() - 0.5) * 50;
        Thread.sleep(100 + (int) skew);
        var result = lazy.access();

        assertThat(result).isSameAs(value);

        future.complete(result);
        return result;
      };

      // When
      CompletableFuture
          .allOf(Stream
              .generate(CompletableFuture::new)
              .limit(concurrency)
              .peek(future -> executor.submit(accession.apply(future)))
              .toArray(size -> new CompletableFuture<?>[size]))
          .join();

      // Then
      verify(supplier, times(1)).get();
    } finally {
      executor.shutdownNow();
    }
  }

  @DisplayName("access() synchronizes correctly on subsequent accesses")
  @MethodSource("concurrentRepeatCases")
  @ParameterizedTest(name = "for {0} concurrent read(s)")
  @Timeout(15)
  void accessSynchronizesCorrectlyOnSubsequentAccesses(int concurrency) {
    // This is closeable in Java 19, but not before.
    @SuppressWarnings("resource")
    var executor = Executors.newFixedThreadPool(concurrency);

    try {
      // Given
      var value = new Object();

      Supplier<Object> supplier = mock();

      when(supplier.get()).thenReturn(value);

      var lazy = new Lazy<>(supplier);

      Function<CompletableFuture<Object>, Callable<Object>> accession = future -> () -> {
        var skew = (Math.random() - 0.5) * 50;
        Thread.sleep(100 + (int) skew);
        var result = lazy.access();

        assertThat(result).isSameAs(value);

        future.complete(result);
        return result;
      };

      // When
      CompletableFuture
          .allOf(Stream
              .generate(CompletableFuture::new)
              .limit(concurrency)
              .peek(future -> executor.submit(accession.apply(future)))
              .toArray(size -> new CompletableFuture<?>[size]))
          .join();

      // Then
      verify(supplier, times(1)).get();
    } finally {
      executor.shutdownNow();
    }
  }

  @DisplayName("access() returns a new value when the lazy is destroyed")
  @RepeatedTest(5)
  @Timeout(15)
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
  @MethodSource("toStringInitialisedCases")
  @ParameterizedTest(name = "with \"{0}\" expected to return \"{1}\"")
  @Timeout(15)
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
  @RepeatedTest(5)
  @Timeout(15)
  void toStringReturnsExpectedValuesWhenUninitialized() {
    // Given
    var supplier = (Supplier<Object>) mock(Supplier.class);
    when(supplier.get()).thenReturn(new Object());
    var lazy = new Lazy<>(supplier);
    lazy.destroy();

    // When
    var actual = lazy.toString();

    // Then
    then(actual).isEqualTo("Lazy{data=null}");
  }

  @DisplayName("ifInitialized() calls the callable when initialized")
  @RepeatedTest(5)
  @Timeout(15)
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

  @DisplayName("ifInitialized() handles race conditions")
  @MethodSource("concurrentRepeatCases")
  @ParameterizedTest(name = "for concurrency = {0}")
  @Timeout(15)
  void ifInitializedHandlesRaceConditionsCorrectly(int concurrency) {
    // Given
    var initializer = (Supplier<Object>) mock(Supplier.class);
    when(initializer.get()).thenReturn(new Object());
    var lazy = spy(new Lazy<>(initializer));

    // When
    lazy.access();

    var futures = Stream
        .generate(() -> CompletableFuture.runAsync(() -> lazy.ifInitialized(value -> {
          try {
            // Lagging here should make the inner condition in the acquire block hit both control
            // paths.
            Thread.sleep(500);
            lazy.destroy();
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        })))
        .limit(concurrency)
        .toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(futures).join();

    // Then
    // Should not ever result in more than one call here if locking works correctly.
    verify(lazy, times(1)).destroy();
  }

  @DisplayName("ifInitialized() propagates exceptions when initialized")
  @RepeatedTest(5)
  @Timeout(15)
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
  @RepeatedTest(5)
  @Timeout(15)
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

  static Stream<Arguments> toStringInitialisedCases() {
    return Stream.of(
        Arguments.of(new Something(), "Lazy{data=Something{}}"),
        Arguments.of("Hello, World!", "Lazy{data=\"Hello, World!\"}")
    );
  }

  static IntStream concurrentRepeatCases() {
    return IntStream.of(2, 3, 5, 10, 20, 30);
  }

  static class Something {

    @Override
    public String toString() {
      return "Something{}";
    }
  }
}
