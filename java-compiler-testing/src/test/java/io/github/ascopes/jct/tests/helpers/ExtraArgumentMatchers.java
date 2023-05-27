/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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

import static org.mockito.ArgumentMatchers.argThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.mockito.ArgumentMatcher;

/**
 * Extra Mockito argument matchers.
 *
 * @author Ashley Scopes
 */
public final class ExtraArgumentMatchers {

  private ExtraArgumentMatchers() {
    throw new UnsupportedOperationException("static-only class");
  }

  @SafeVarargs
  public static <T> T hasGenericType(T... varargs) {
    if (varargs == null || varargs.length != 0) {
      throw new IllegalArgumentException("Must not provide any varargs to this call");
    }

    return argThat(arg -> arg != null
        && varargs.getClass().getComponentType().isAssignableFrom(arg.getClass()));
  }

  @SafeVarargs
  public static <E, T extends Iterable<E>> T containsExactlyElements(E... expected) {
    return containsExactlyElements(Set.of(expected));
  }

  public static <E, T extends Iterable<E>> T containsExactlyElements(Collection<E> expected) {
    return argThat(new ArgumentMatcher<>() {
      @Override
      public String toString() {
        return "containsExactlyElements(" + expected + ")";
      }

      @Override
      public boolean matches(T actualIterable) {
        if (actualIterable == null) {
          return false;
        }

        var actualElements = new ArrayList<E>();
        actualIterable.forEach(actualElements::add);

        // All expected are in actual
        for (var expectedElement : expected) {
          if (!actualElements.contains(expectedElement)) {
            throw new IllegalArgumentException(
                "Expected element " + expectedElement + " was not in the actual collection"
            );
          }
        }

        // All actual are in expected.
        var expectedSet = Set.copyOf(expected);
        for (var actualElement : actualElements) {
          if (!expectedSet.contains(actualElement)) {
            throw new IllegalArgumentException(
                "Actual element " + actualElement + " was not in the expected elements array"
            );
          }
        }

        return true;
      }
    });
  }
}
