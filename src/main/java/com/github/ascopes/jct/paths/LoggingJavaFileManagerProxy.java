package com.github.ascopes.jct.paths;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A proxy that wraps a {@link JavaFileManager} in a proxy that can log all interactions with the
 * JavaFileManager, along with a corresponding stacktrace.
 *
 * <p>This is useful for diagnosing difficult-to-find errors being produced by {@code javac}
 * during testing.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class LoggingJavaFileManagerProxy implements InvocationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingJavaFileManagerProxy.class);

  private final JavaFileManager inner;
  private final boolean stackTraces;

  private LoggingJavaFileManagerProxy(JavaFileManager inner, boolean stackTraces) {
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
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getName().equals("toString") && method.getParameterCount() == 0) {
      return toString();
    }

    String extraInfo = "";
    if (stackTraces) {
      extraInfo = "\n" + Arrays.stream(Thread.currentThread().getStackTrace())
          .map(frame -> "\tat " + frame)
          .collect(Collectors.joining("\n"));
    }

    var argsStr = args == null ? "" : Arrays
        .stream(args)
        .map(Objects::toString)
        .collect(Collectors.joining(", "));

    LOGGER.info(">>> {}({}){} is invoked", method.getName(), argsStr, extraInfo);

    try {
      var result = method.invoke(inner, args);
      if (method.getReturnType().equals(void.class)) {
        LOGGER.info("<<< {}({}) completes", method.getName(), argsStr);
      } else {
        LOGGER.info("<<< {}({}) returns {}", method.getName(), argsStr, result);
      }
      return result;
    } catch (Throwable ex) {
      LOGGER.error("!!! {}({}) throws exception", method.getName(), argsStr, ex);
      throw ex;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return the string representation of this proxy object.
   */
  @Override
  public String toString() {
    return "TracingJavaFileManagerProxy{"
        + "inner=" + inner + ", "
        + "stackTraces=" + stackTraces
        + "}";
  }

  /**
   * Wrap the given {@link JavaFileManager} in a proxy that logs any calls.
   *
   * @param manager     the manager to wrap.
   * @param stackTraces {@code true} to dump stacktraces on each interception, or {@code false} to
   *                    omit them.
   * @return the proxy {@link JavaFileManager} to use.
   */
  public static JavaFileManager wrap(JavaFileManager manager, boolean stackTraces) {
    return (JavaFileManager) Proxy.newProxyInstance(
        JavaFileManager.class.getClassLoader(),
        new Class<?>[]{JavaFileManager.class},
        new LoggingJavaFileManagerProxy(manager, stackTraces)
    );
  }
}
