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
package io.github.ascopes.jct.junit;

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctCompilerConfigurer.JctSimpleCompilerConfigurer;
import io.github.ascopes.jct.ex.JctJunitConfigurerException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.opentest4j.TestAbortedException;

/**
 * Base for defining a compiler-supplying arguments provider for Junit Jupiter parameterised test
 * support.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public abstract class AbstractCompilersProvider implements ArgumentsProvider {

  // Configured values by JUnit 5. Volatile in case JUnit ever does this from a different
  // thread in the future.
  private volatile int minVersion;
  private volatile int maxVersion;

  @Nullable
  private volatile Class<? extends JctSimpleCompilerConfigurer>[] configurerClasses;

  /**
   * Initialise this provider.
   */
  protected AbstractCompilersProvider() {
    minVersion = 0;
    maxVersion = Integer.MAX_VALUE;
    configurerClasses = null;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return IntStream
        .rangeClosed(minVersion, maxVersion)
        .mapToObj(this::compilerForVersion)
        .peek(this::applyConfigurers)
        .map(Arguments::of);
  }

  /**
   * Configure this provider with parameters from annotations.
   *
   * @param min               the inclusive minimum compiler version to use.
   * @param max               the inclusive maximum compiler version to use.
   * @param modules           whether the compiler version must support modules.
   * @param configurerClasses the configurer classes to apply to each compiler.
   */
  protected final void configure(
      int min,
      int max,
      boolean modules,
      Class<? extends JctSimpleCompilerConfigurer>[] configurerClasses
  ) {
    min = Math.max(min, minSupportedVersion(modules));
    max = Math.min(max, maxSupportedVersion(modules));

    if (max < 8 || min < 8) {
      throw new IllegalArgumentException("Cannot use a Java version less than Java 8");
    }

    if (min > max) {
      throw new IllegalArgumentException(
          "Cannot set min version to a version higher than the max version"
      );
    }

    minVersion = min;
    maxVersion = max;

    this.configurerClasses = requireNonNullValues(configurerClasses, "configurerClasses");
  }

  /**
   * Initialise a new compiler on the given release.
   *
   * @param release the release version to use.
   * @return the compiler.
   */
  protected abstract JctCompiler<?, ?> compilerForVersion(int release);

  /**
   * Get the minimum supported compiler version.
   *
   * @param modules whether to require module support or not.
   * @return the minimum supported compiler version.
   */
  protected abstract int minSupportedVersion(boolean modules);

  /**
   * Get the maximum supported compiler version.
   *
   * @param modules whether to require module support or not.
   * @return the minimum supported compiler version.
   */
  protected abstract int maxSupportedVersion(@SuppressWarnings("unused") boolean modules);

  private void applyConfigurers(JctCompiler<?, ?> compiler) {
    var classes = requireNonNull(configurerClasses);

    for (var configurerClass : classes) {
      var configurer = initialiseConfigurer(configurerClass);

      try {
        configurer.configure(compiler);
      } catch (TestAbortedException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new JctJunitConfigurerException(
            "Failed to configure compiler with configurer class " + configurerClass.getName(),
            ex
        );
      }
    }
  }

  private JctSimpleCompilerConfigurer initialiseConfigurer(
      Class<? extends JctSimpleCompilerConfigurer> configurerClass
  ) {
    Constructor<? extends JctSimpleCompilerConfigurer> constructor;

    try {
      constructor = configurerClass.getDeclaredConstructor();
      // Force-enable reflective access. If the user is using a SecurityManager for any reason then
      // tough luck. JVM go bang.
      constructor.setAccessible(true);

    } catch (NoSuchMethodException ex) {
      throw new JctJunitConfigurerException(
          "No no-args constructor was found for configurer class " + configurerClass.getName(),
          ex
      );
    }

    try {
      return constructor.newInstance();
    } catch (ReflectiveOperationException ex) {
      if (ex instanceof InvocationTargetException
          && ex.getCause() instanceof TestAbortedException) {

        // Aborting the test from the constructor should be equally valid, so propagate this
        // exception as a special edge case. Throw a new instance to do this to prevent the
        // stacktrace getting a circular reference.
        var newEx = new TestAbortedException(ex.getCause().getMessage());
        newEx.addSuppressed(ex);
        throw newEx;
      }

      throw new JctJunitConfigurerException(
          "Failed to initialise a new instance of configurer class " + configurerClass.getName(),
          ex
      );
    }
  }

}
