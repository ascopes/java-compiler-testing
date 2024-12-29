/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.assertions;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link StackTraceAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("StackTraceAssert tests")
class StackTraceAssertTest {

  @DisplayName("Assertions are performed on the stacktrace")
  @Test
  void assertionsArePerformedOnTheStackTrace() {
    // Given
    var stackTraceArray = new StackTraceElement[]{
        new StackTraceElement("java.lang.Foo", "doSomething", "Foo.java", 123),
        new StackTraceElement("java.lang.Foo", "doSomethingElse", "Foo.java", 456),
        new StackTraceElement("java.lang.Foo", "doSomethingAgain", "Foo.java", 789),
        new StackTraceElement("java.lang.Foo", "doAnotherThing", "Foo.java", 101112)
    };

    var stackTrace = Arrays.asList(stackTraceArray);

    // When
    var assertions = new StackTraceAssert(stackTrace);

    // Then
    assertions
        .isSameAs(stackTrace)
        .containsExactly(stackTraceArray)
        .first()
        .isSameAs(stackTraceArray[0]);
  }

  @DisplayName("Assertions are performed on sublists of the stacktrace")
  @Test
  void assertionsArePerformedOnTheStackTraceSublists() {
    // Given
    var stackTraceArray = new StackTraceElement[]{
        new StackTraceElement("java.lang.Foo", "doSomething", "Foo.java", 123),
        new StackTraceElement("java.lang.Foo", "doSomethingElse", "Foo.java", 456),
        new StackTraceElement("java.lang.Foo", "doSomethingAgain", "Foo.java", 789),
        new StackTraceElement("java.lang.Foo", "doAnotherThing", "Foo.java", 101112)
    };

    var stackTrace = Arrays.asList(stackTraceArray);

    // When
    var assertions = new StackTraceAssert(stackTrace);

    // Then
    assertions
        .filteredOn(frame -> frame.getMethodName().startsWith("doSomething"))
        .containsExactlyElementsOf(stackTrace.subList(0, 3))
        .first()
        .isSameAs(stackTraceArray[0]);
  }
}
