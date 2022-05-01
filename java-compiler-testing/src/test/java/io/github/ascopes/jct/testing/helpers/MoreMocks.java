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

package io.github.ascopes.jct.testing.helpers;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Objects;
import org.mockito.ArgumentMatcher;
import org.mockito.MockSettings;

/**
 * Helper to create lightweight stubs and mocks.
 *
 * @author Ashley Scopes
 */
public final class MoreMocks {

  private MoreMocks() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Create a mock from a type reference.
   *
   * @param typeRef type reference to mock.
   * @param <T>     the type reference type.
   * @return the mock.
   */
  public static <T> T mockCast(TypeRef<T> typeRef) {
    return mockCast(typeRef, withSettings());
  }

  /**
   * Create a mock from a type reference.
   *
   * @param typeRef  type reference to mock.
   * @param settings the settings to use.
   * @param <T>      the type reference type.
   * @return the mock.
   */
  public static <T> T mockCast(TypeRef<T> typeRef, MockSettings settings) {
    return mock(typeRef.getType(), settings);
  }

  /**
   * Create a stub from a class.
   *
   * @param type type to stub.
   * @param <T>  the class type.
   * @return the stub.
   */
  public static <T> T stub(Class<T> type) {
    return stub(type, withSettings());
  }

  /**
   * Create a stub from a class.
   *
   * @param type     type to stub.
   * @param settings the settings to use.
   * @param <T>      the class type.
   * @return the stub.
   */
  public static <T> T stub(Class<T> type, MockSettings settings) {
    return mock(type, settings.stubOnly());
  }

  /**
   * Create a stub from a type reference.
   *
   * @param typeRef type reference to stub.
   * @param <T>     the type reference type.
   * @return the stub.
   */
  public static <T> T stubCast(TypeRef<T> typeRef) {
    return stubCast(typeRef, withSettings());
  }

  /**
   * Create a stub from a type reference.
   *
   * @param typeRef  type reference to stub.
   * @param settings the settings to use.
   * @param <T>      the type reference type.
   * @return the stub.
   */
  public static <T> T stubCast(TypeRef<T> typeRef, MockSettings settings) {
    return stub(typeRef.getType(), settings);
  }

  /**
   * Argument matcher that checks the toString of the argument matches a value.
   *
   * @param repr value to match.
   * @param <T>  argument matcher type.
   * @return the matcher.
   */
  public static <T> T hasToString(String repr) {
    var matchesCriteria = new ArgumentMatcher<T>() {
      @Override
      public boolean matches(T t) {
        return Objects.toString(t).equals(repr);
      }
    };
    return argThat(matchesCriteria);
  }
}
