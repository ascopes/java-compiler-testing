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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.ascopes.jct.intern.EnumerationAdapter;
import com.github.ascopes.jct.testing.helpers.MoreMocks;
import com.github.ascopes.jct.testing.helpers.TypeRef;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


/**
 * {@link EnumerationAdapter} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("EnumerationAdapter tests")
class EnumerationAdapterTest {

  @DisplayName("Initializing with a null iterator throws a NullPointerException")
  @Test
  void initializingWithNullIteratorThrowsNullPointerException() {
    thenCode(() -> new EnumerationAdapter<>(null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("hasMoreElements() returns Iterator#hasNext()")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when hasNext() returns {0}")
  void hasMoreElementsReturnsTrueWhenIteratorHasNextIsTrue(boolean hasNext) {
    // Given
    var iterator = MoreMocks.mockCast(new TypeRef<Iterator<?>>() {
    });

    when(iterator.hasNext()).thenReturn(hasNext);
    var adapter = new EnumerationAdapter<>(iterator);

    // When
    var hasMoreElements = adapter.hasMoreElements();

    // Then
    verify(iterator, times(1)).hasNext();
    then(hasMoreElements).isEqualTo(hasNext);
  }

  @DisplayName("nextElement() returns the result of Iterator#next()")
  @Test
  void nextElementReturnsTheResultOfIteratorNext() {
    // Given
    var first = new Object();
    var second = new Object();
    var third = new Object();

    var iterator = MoreMocks.mockCast(new TypeRef<Iterator<Object>>() {
    });

    when(iterator.next()).thenReturn(first, second, third);

    var adapter = new EnumerationAdapter<>(iterator);

    // When
    var firstElement = adapter.nextElement();
    var secondElement = adapter.nextElement();
    var thirdElement = adapter.nextElement();

    // Then
    verify(iterator, times(3)).next();
    then(firstElement).isSameAs(first);
    then(secondElement).isSameAs(second);
    then(thirdElement).isSameAs(third);
  }

  @DisplayName("nextElement() propagates NoSuchElementException")
  @Test
  void nextElementPropagatesNoSuchElementException() {
    // Given
    var ex = new NoSuchElementException("Nothing left!").fillInStackTrace();

    var iterator = MoreMocks.mockCast(new TypeRef<Iterator<?>>() {
    });

    when(iterator.next()).thenThrow(ex);

    var adapter = new EnumerationAdapter<>(iterator);

    // Then
    thenCode(adapter::nextElement)
        .isInstanceOf(NoSuchElementException.class)
        .isSameAs(ex);
    verify(iterator, times(1)).next();
  }
}
