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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.utils.LoomPolyfill;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A proxy that wraps a {@link JctFileManager} in a proxy that can log all interactions with the
 * JavaFileManager, along with a corresponding stacktrace.
 *
 * <p>This is useful for diagnosing difficult-to-find errors being produced by {@code javac}
 * during testing, however, it may produce a hefty performance overhead when in use.
 *
 * <p>All logs are emitted with the {@code INFO} logging level.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@ThreadSafe
public final class LoggingFileManagerProxy implements InvocationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFileManagerProxy.class);

  private final JctFileManager inner;
  private final boolean stackTraces;
  private final ThreadLocal<Integer> stackDepth;

  private LoggingFileManagerProxy(JctFileManager inner, boolean stackTraces) {
    this.inner = inner;
    this.stackTraces = stackTraces;
    stackDepth = ThreadLocal.withInitial(() -> 0);
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
  @Override
  public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
    if (method.getName().equals("toString") && method.getParameterCount() == 0) {
      return toString();
    }

    var thread = Thread.currentThread();
    var threadId = LoomPolyfill.getThreadId(thread);

    var depth = incStackDepth();
    var returnType = method.getReturnType().getSimpleName();
    var methodName = method.getName();
    var paramStr = Arrays
        .stream(method.getParameters())
        .map(Parameter::getType)
        .map(Class::getSimpleName)
        .collect(Collectors.joining(", "));

    // When no arguments are passed, the args array is
    // null rather than a zero length array (i.e. Object[0]).
    var argsStr = args == null ? "" : Arrays
        .stream(args)
        .map(Objects::toString)
        .collect(Collectors.joining(", "));

    var extraInfo = "";

    if (stackTraces) {
      // skip top 2 frames to discard the call to
      // .getStackTrace(), and this proxy method call.
      extraInfo = Arrays
          .stream(thread.getStackTrace())
          .skip(2)
          .map(frame -> "\n\tat " + frame)
          .collect(Collectors.joining(""));
    }

    LOGGER.info(
        ">>> [thread={}, depth={}] {} {}({}) called with ({}){}",
        threadId,
        depth,
        returnType,
        methodName,
        paramStr,
        argsStr,
        extraInfo
    );

    try {
      var result = method.invoke(inner, args);

      if (method.getReturnType().equals(void.class)) {
        LOGGER.info(
            "<<< [thread={}, depth={}] {} {}({}) completed",
            threadId,
            depth,
            returnType,
            methodName,
            paramStr
        );

      } else {
        LOGGER.info(
            "<<< [thread={}, depth={}] {} {}({}) returned {}",
            threadId,
            depth,
            returnType,
            methodName,
            paramStr,
            result
        );
      }

      return result;

    } catch (ReflectiveOperationException ex) {
      // We always want the cause to get the real exception.
      // If, however, for any reason it is not present, then
      // it probably means something else went wrong, so report
      // it directly.
      Throwable cause;

      if (ex.getCause() == null) {
        cause = ex;
      } else {
        cause = ex.getCause();
        cause.addSuppressed(ex);
      }

      LOGGER.error(
          "!!! [thread={}, depth={}] {} {}({}) threw exception",
          threadId,
          depth,
          returnType,
          methodName,
          paramStr,
          cause
      );

      throw cause;

    } finally {
      decStackDepth();
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("inner", inner)
        .attribute("stackTraces", stackTraces)
        .toString();
  }

  private int incStackDepth() {
    var depth = stackDepth.get() + 1;
    stackDepth.set(depth);
    return depth;
  }

  private void decStackDepth() {
    var depth = stackDepth.get() - 1;
    stackDepth.set(depth);
  }

  /**
   * Wrap the given {@link JctFileManager} in a proxy that logs any calls.
   *
   * @param manager     the manager to wrap.
   * @param stackTraces {@code true} to dump stacktraces on each interception, or {@code false} to
   *                    omit them.
   * @return the proxy {@link JctFileManager} to use.
   */
  @WillNotClose
  public static JctFileManager wrap(
      @WillCloseWhenClosed JctFileManager manager,
      boolean stackTraces
  ) {
    return (JctFileManager) Proxy.newProxyInstance(
        JctFileManager.class.getClassLoader(),
        new Class<?>[]{JctFileManager.class},
        new LoggingFileManagerProxy(manager, stackTraces)
    );
  }
}
