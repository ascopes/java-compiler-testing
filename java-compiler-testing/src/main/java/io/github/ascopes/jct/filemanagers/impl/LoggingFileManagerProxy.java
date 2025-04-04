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

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.utils.LoomPolyfill;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A proxy that wraps a {@link JctFileManager} in a proxy that can log all interactions with the
 * JavaFileManager, along with a corresponding stacktrace.
 *
 * <p>This is useful for diagnosing difficult-to-find errors being produced by {@code javac}
 * during testing, however, it may produce a hefty performance overhead when in use.
 *
 * <p>All logs are emitted with the {@code DEBUG} logging level.
 *
 * <p>Since v2.0.0, this class is now an internal class that is not part of the
 * public API.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class LoggingFileManagerProxy implements InvocationHandler {

  private final Logger logger;
  private final JctFileManager inner;
  private final boolean stackTraces;

  private LoggingFileManagerProxy(JctFileManager inner, boolean stackTraces) {
    // Instance scoped for testing purposes.
    logger = LoggerFactory.getLogger(LoggingFileManagerProxy.class);
    this.inner = inner;
    this.stackTraces = stackTraces;
  }

  /**
   * Invoke the given call.
   *
   * @param proxy  the proxy.
   * @param method the method.
   * @param args   the arguments.
   * @return the result.
   * @throws Throwable any exception that is thrown.
   */
  @Nullable
  @Override
  public Object invoke(Object proxy, Method method, Object @Nullable ... args) throws Throwable {
    if (method.getName().equals("toString")) {
      return toString();
    }

    var thread = LoomPolyfill.getCurrentThread();
    var threadId = LoomPolyfill.getThreadId(thread);

    var returnType = method.getReturnType().getSimpleName();
    var methodName = method.getName();
    var paramStr = Stream
        .of(method.getParameters())
        .map(Parameter::getType)
        .map(Class::getSimpleName)
        .collect(Collectors.joining(", "));

    // When no arguments are passed, the args array is
    // null rather than a zero length array (i.e. Object[0]).
    var argsStr = args == null ? "" : Stream
        .of(args)
        .map(Objects::toString)
        .collect(Collectors.joining(", "));

    logger
        .atDebug()
        .setMessage(">>> [thread={}] {} {}({}) called with ({}){}")
        .addArgument(threadId)
        .addArgument(returnType)
        .addArgument(methodName)
        .addArgument(paramStr)
        .addArgument(argsStr)
        .addArgument(stackTraceFormatter(thread.getStackTrace()))
        .log();

    try {
      var result = method.invoke(inner, args);

      if (method.getReturnType().equals(void.class)) {
        logger
            .atDebug()
            .setMessage("<<< [thread={}] {} {}({}) completed")
            .addArgument(threadId)
            .addArgument(returnType)
            .addArgument(methodName)
            .addArgument(paramStr)
            .log();
      } else {
        logger
            .atDebug()
            .setMessage("<<< [thread={}] {} {}({}) returned {}")
            .addArgument(threadId)
            .addArgument(returnType)
            .addArgument(methodName)
            .addArgument(paramStr)
            .addArgument(result)
            .log();
      }

      return result;

    } catch (ReflectiveOperationException ex) {
      logger
          .atDebug()
          .setMessage("!!! [thread={}] {} {}({}) threw exception")
          .addArgument(threadId)
          .addArgument(returnType)
          .addArgument(methodName)
          .addArgument(paramStr)
          .setCause(ex.getCause())
          .log();

      throw ex.getCause();

    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("inner", inner)
        .attribute("stackTraces", stackTraces)
        .toString();
  }

  private Supplier<String> stackTraceFormatter(StackTraceElement[] stackTrace) {
    if (!stackTraces) {
      return () -> "";
    }

    return () -> Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());
  }

  /**
   * Wrap the given {@link JctFileManager} in a proxy that logs any calls.
   *
   * @param manager     the manager to wrap.
   * @param stackTraces {@code true} to dump stacktraces on each interception, or {@code false} to
   *                    omit them.
   * @return the proxy {@link JctFileManager} to use.
   */
  public static JctFileManager wrap(JctFileManager manager, boolean stackTraces) {
    return (JctFileManager) Proxy.newProxyInstance(
        JctFileManager.class.getClassLoader(),
        new Class<?>[]{JctFileManager.class},
        new LoggingFileManagerProxy(manager, stackTraces)
    );
  }
}
