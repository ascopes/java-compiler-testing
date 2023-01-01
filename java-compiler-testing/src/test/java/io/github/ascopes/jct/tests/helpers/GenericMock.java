/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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
package io.github.ascopes.jct.tests.helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import org.mockito.MockSettings;

/**
 * Helper to create generic mocks with Mockito.
 *
 * @author Ashley Scopes
 */
public final class GenericMock {

  private GenericMock() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Create a new builder for a generic mock.
   *
   * @param rawType the raw type.
   * @param <T>     the raw type.
   * @return the mock builder.
   */
  public static <T> Upcastable<T> mockRaw(Class<T> rawType) {
    return new GenericMockBuilder<>(rawType);
  }

  /**
   * Builder stage that allows upcasting.
   *
   * @param <T> the raw type.
   */
  public interface Upcastable<T> {

    /**
     * Upcast the raw type to the given generic type.
     *
     * @param <U> the generic type to upcast to.
     * @return the mock builder.
     */
    <U extends T> Buildable<U> upcastedTo();
  }

  /**
   * Builder stage that allows building the final mock.
   *
   * @param <T> the type of the mock.
   */
  public interface Buildable<T> {

    /**
     * Build the mock with default settings.
     *
     * @return the mock.
     */
    default T build() {
      return build(withSettings());
    }

    /**
     * Build the mock with the given settings.
     *
     * @param settings the mock settings to use.
     * @return the mock.
     */
    T build(MockSettings settings);
  }

  private static final class GenericMockBuilder<T> implements Upcastable<T>, Buildable<T> {

    private final Class<T> base;

    private GenericMockBuilder(Class<T> base) {
      this.base = base;
    }

    @Override
    public <U extends T> Buildable<U> upcastedTo() {
      @SuppressWarnings("unchecked")
      var upcastType = (Class<U>) base;

      return new GenericMockBuilder<>(upcastType);
    }

    @Override
    public T build(MockSettings settings) {
      return mock(base, settings);
    }
  }
}
