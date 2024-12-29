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
package io.github.ascopes.jct.filemanagers.impl;

import static io.github.ascopes.jct.fixtures.Fixtures.oneOf;
import static io.github.ascopes.jct.fixtures.Fixtures.someBoolean;
import static io.github.ascopes.jct.fixtures.Fixtures.someInt;
import static io.github.ascopes.jct.fixtures.Fixtures.someLong;
import static io.github.ascopes.jct.fixtures.Fixtures.someRealStackTrace;
import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.fixtures.Slf4jLoggerFake;
import io.github.ascopes.jct.utils.LoomPolyfill;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * {@link LoggingFileManagerProxy} tests.
 *
 * <p>Note that the dynamic nature of this class makes these tests a bit funky with a lot
 * of Mockito voodoo.
 *
 * @author Ashley Scopes
 */
@DisplayName("LoggingFileManagerProxy tests")
@ExtendWith(MockitoExtension.class)
@Isolated("Messes with proxies and side effects")
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggingFileManagerProxyTest {

  long threadId;
  StackTraceElement[] stackTrace;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Thread thread;

  Slf4jLoggerFake slf4jLoggerFake;

  @BeforeEach
  void setUp() {
    threadId = someLong(66_666);
    stackTrace = someRealStackTrace();

    when(thread.getStackTrace())
        .thenReturn(stackTrace);

    slf4jLoggerFake = new Slf4jLoggerFake();
  }

  @DisplayName("toString() returns the expected result")
  @Test
  void toStringReturnsTheExpectedResult() {
    // Given
    var stackTraces = someBoolean();
    var impl = mock(JctFileManager.class);
    var proxy = LoggingFileManagerProxy.wrap(impl, stackTraces);

    // Then
    assertThat(proxy)
        .extracting(Object::toString)
        .asString()
        .isEqualTo(
            "LoggingFileManagerProxy{inner=%s, stackTraces=%s}",
            impl,
            stackTraces
        );
  }

  @DisplayName("The proxy methods return the result of calling the real implementation")
  @MethodSource("proxiedMethods")
  @ParameterizedTest(name = "for method {0}")
  void proxyMethodsReturnImplementationResult(String ignored, Method method) throws Throwable {
    // Given
    var expectedParams = mockParams(method);
    var expectedResult = isVoidReturnType(method) ? null : mockReturnType(method);
    var impl = mock(JctFileManager.class, (ctx) -> expectedResult);
    var proxy = LoggingFileManagerProxy.wrap(impl, someBoolean());

    // When
    var actualResult = method.invoke(proxy, expectedParams);

    // Then
    method.invoke(verify(impl), expectedParams);
    assertThat(actualResult).satisfiesAnyOf(
        actual -> assertThat(actual).isSameAs(expectedResult),
        actual -> assertThat(actual).isEqualTo(expectedResult)
    );
    verifyNoMoreInteractions(impl);
  }

  @DisplayName("The proxy methods propagate exceptions thrown by the implementation")
  @MethodSource("proxiedMethods")
  @ParameterizedTest(name = "for method {0}")
  void proxyMethodsPropagateImplementationExceptions(
      String ignored,
      Method method
  ) throws Throwable {
    // Given
    var expectedParams = mockParams(method);
    var expectedException = new RuntimeException("Chill out! " + someText());
    var impl = mock(JctFileManager.class, (ctx) -> {
      throw expectedException;
    });
    var proxy = LoggingFileManagerProxy.wrap(impl, someBoolean());

    // Then
    assertThatThrownBy(() -> method.invoke(proxy, expectedParams))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(expectedException);

    method.invoke(verify(impl), expectedParams);
    verifyNoMoreInteractions(impl);
  }

  @DisplayName("Method invocations are logged without stacktraces")
  @MethodSource("proxiedMethods")
  @ParameterizedTest(name = "for method {0}")
  void methodInvocationsGetLoggedWithoutStacktraces(String ignored, Method method)
      throws Throwable {

    try (
        var loomPolyfillMockedStatic = mockStatic(LoomPolyfill.class);
        var loggerFactoryMockedStatic = mockStatic(LoggerFactory.class)
    ) {
      loomPolyfillMockedStatic.when(LoomPolyfill::getCurrentThread)
          .thenReturn(thread);
      loomPolyfillMockedStatic.when(() -> LoomPolyfill.getThreadId(thread))
          .thenReturn(threadId);
      loggerFactoryMockedStatic.when(() -> LoggerFactory.getLogger(any(Class.class)))
          .thenReturn(slf4jLoggerFake);

      // Given
      var params = mockParams(method);
      var impl = mock(JctFileManager.class, Answers.RETURNS_DEEP_STUBS);
      var proxy = LoggingFileManagerProxy.wrap(impl, false);

      // When
      method.invoke(proxy, params);

      // Then
      slf4jLoggerFake.assertThatEntryLogged(
          Level.DEBUG,
          null,
          ">>> [thread={}] {} {}({}) called with ({}){}",
          threadId,
          method.getReturnType().getSimpleName(),
          method.getName(),
          Stream.of(method.getParameterTypes())
              .map(Class::getSimpleName)
              .collect(Collectors.joining(", ")),
          Stream.of(params)
              .map(Objects::toString)
              .collect(Collectors.joining(", ")),
          ""
      );
    }
  }

  @DisplayName("Method invocations are logged with stacktraces")
  @MethodSource("proxiedMethods")
  @ParameterizedTest(name = "for method {0}")
  void methodInvocationsGetLoggedWithStacktraces(String ignored, Method method) throws Throwable {
    // Given
    try (
        var loomPolyfillMockedStatic = mockStatic(LoomPolyfill.class);
        var loggerFactoryMockedStatic = mockStatic(LoggerFactory.class)
    ) {
      loomPolyfillMockedStatic.when(LoomPolyfill::getCurrentThread)
          .thenReturn(thread);
      loomPolyfillMockedStatic.when(() -> LoomPolyfill.getThreadId(thread))
          .thenReturn(threadId);
      loggerFactoryMockedStatic.when(() -> LoggerFactory.getLogger(any(Class.class)))
          .thenReturn(slf4jLoggerFake);
      when(thread.getStackTrace())
          .thenReturn(stackTrace);

      var params = mockParams(method);
      var impl = mock(JctFileManager.class, Answers.RETURNS_DEEP_STUBS);
      var proxy = LoggingFileManagerProxy.wrap(impl, true);

      // When
      method.invoke(proxy, params);

      // Then
      slf4jLoggerFake.assertThatEntryLogged(
          Level.DEBUG,
          null,
          ">>> [thread={}] {} {}({}) called with ({}){}",
          threadId,
          method.getReturnType().getSimpleName(),
          method.getName(),
          Stream.of(method.getParameterTypes())
              .map(Class::getSimpleName)
              .collect(Collectors.joining(", ")),
          Stream.of(params)
              .map(Objects::toString)
              .collect(Collectors.joining(", ")),
          Stream.of(stackTrace)
              .map(Objects::toString)
              .map("\n\t"::concat)
              .collect(Collectors.joining())
      );
    }
  }

  @DisplayName("Method results are logged")
  @MethodSource("proxiedMethods")
  @ParameterizedTest(name = "for method {0}")
  void methodResultsGetLogged(String ignored, Method method) throws Throwable {
    // Given
    try (
        var loomPolyfillMockedStatic = mockStatic(LoomPolyfill.class);
        var loggerFactoryMockedStatic = mockStatic(LoggerFactory.class)
    ) {
      loomPolyfillMockedStatic.when(LoomPolyfill::getCurrentThread)
          .thenReturn(thread);
      loomPolyfillMockedStatic.when(() -> LoomPolyfill.getThreadId(thread))
          .thenReturn(threadId);
      loggerFactoryMockedStatic.when(() -> LoggerFactory.getLogger(any(Class.class)))
          .thenReturn(slf4jLoggerFake);

      var params = mockParams(method);
      var expectedResult = isVoidReturnType(method) ? null : mockReturnType(method);
      var impl = mock(JctFileManager.class, (ctx) -> expectedResult);
      var proxy = LoggingFileManagerProxy.wrap(impl, someBoolean());

      // When
      method.invoke(proxy, params);

      // Then
      if (isVoidReturnType(method)) {
        slf4jLoggerFake.assertThatEntryLogged(
            Level.DEBUG,
            null,
            "<<< [thread={}] {} {}({}) completed",
            threadId,
            method.getReturnType().getSimpleName(),
            method.getName(),
            Stream.of(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "))
        );
      } else {
        slf4jLoggerFake.assertThatEntryLogged(
            Level.DEBUG,
            null,
            "<<< [thread={}] {} {}({}) returned {}",
            threadId,
            method.getReturnType().getSimpleName(),
            method.getName(),
            Stream.of(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ")),
            expectedResult
        );
      }
    }
  }

  @DisplayName("Method exceptions are logged")
  @MethodSource("proxiedMethods")
  @ParameterizedTest(name = "for method {0}")
  void methodExceptionsGetLogged(String ignored, Method method) {
    // Given
    try (
        var loomPolyfillMockedStatic = mockStatic(LoomPolyfill.class);
        var loggerFactoryMockedStatic = mockStatic(LoggerFactory.class)
    ) {
      loomPolyfillMockedStatic.when(LoomPolyfill::getCurrentThread)
          .thenReturn(thread);
      loomPolyfillMockedStatic.when(() -> LoomPolyfill.getThreadId(thread))
          .thenReturn(threadId);
      loggerFactoryMockedStatic.when(() -> LoggerFactory.getLogger(any(Class.class)))
          .thenReturn(slf4jLoggerFake);
      var params = mockParams(method);

      var stackTrace = someRealStackTrace();
      var exception = new RuntimeException("Bang bang bang");
      exception.setStackTrace(stackTrace);

      var impl = mock(JctFileManager.class, ctx -> {
        throw exception;
      });
      var proxy = LoggingFileManagerProxy.wrap(impl, someBoolean());

      // When
      assertThatThrownBy(() -> method.invoke(proxy, params))
          .isNotNull();

      // Then
      slf4jLoggerFake.assertThatEntryLogged(
          Level.DEBUG,
          exception,
          "!!! [thread={}] {} {}({}) threw exception",
          threadId,
          method.getReturnType().getSimpleName(),
          method.getName(),
          Stream.of(method.getParameterTypes())
              .map(Class::getSimpleName)
              .collect(Collectors.joining(", "))
      );
    }
  }

  //////////////////////////////////////////////////////
  /// Proxy access stuff for tests. Here be dragons. ///
  //////////////////////////////////////////////////////

  static Stream<Arguments> proxiedMethods() {
    var inheritedMethods = Stream
        .of(JctFileManager.class.getMethods())
        .filter(not(inherited -> inherited.getDeclaringClass().equals(JctFileManager.class)));

    var declaredMethods = Stream
        .of(JctFileManager.class.getDeclaredMethods());

    return Stream
        .concat(declaredMethods, inheritedMethods)
        .filter(not(Method::isSynthetic))
        .filter(not(m -> m.getName().equals("toString")))
        .map(m -> arguments(signature(m), m));
  }

  static String signature(Method method) {
    var sb = new StringBuilder()
        .append(method.getReturnType().getSimpleName())
        .append(" ")
        .append(method.getName())
        .append("(");

    for (var i = 0; i < method.getParameterCount(); ++i) {
      if (i > 0) {
        sb.append(", ");
      }

      var param = method.getParameters()[i];
      sb.append(param.getType().getSimpleName())
          .append(" ")
          .append(param.getName());
    }

    return sb
        .append(")")
        .toString();
  }

  @Nullable
  static Object mockReturnType(Method method) {
    return mockElement(method.getReturnType());
  }

  static Object[] mockParams(Method method) {
    var params = method.getParameters();
    var instances = new Object[method.getParameterCount()];

    for (var i = 0; i < instances.length; ++i) {
      instances[i] = mockElement(params[i].getType());
    }

    return instances;
  }

  static boolean isVoidReturnType(Method method) {
    var type = method.getReturnType();
    return type.equals(Void.class) || type.equals(void.class);
  }

  @Nullable
  static Object mockElement(Class<?> type) {
    if (type.isArray()) {
      var items = Array.newInstance(type.getComponentType(), someInt(1, 5));
      for (var i = 0; i < Array.getLength(items); ++i) {
        Array.set(items, i, mockElement(type.getComponentType()));
      }
      return items;
    }
    if (type.equals(Byte.class) || type.equals(byte.class)) {
      return (byte) someInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }
    if (type.equals(Short.class) || type.equals(short.class)) {
      return (short) someInt(Short.MIN_VALUE, Short.MAX_VALUE);
    }
    if (type.equals(Integer.class) || type.equals(int.class)) {
      return someInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    if (type.equals(Long.class) || type.equals(long.class)) {
      return someLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }
    if (type.equals(Character.class) || type.equals(char.class)) {
      return (char) someInt(Character.MIN_VALUE, Character.MAX_VALUE);
    }
    if (type.equals(Boolean.class) || type.equals(boolean.class)) {
      return someBoolean();
    }
    if (type.equals(String.class) || type.equals(CharSequence.class)) {
      return someText();
    }
    if (Type.class.isAssignableFrom(type)) {
      return oneOf(
          StringBuilder.class, List.class, TestFactory.class, String.class, Object.class
      );
    }
    return mock(type, withSettings()
        .strictness(Strictness.LENIENT)
        .defaultAnswer(Answers.RETURNS_DEEP_STUBS));
  }
}
