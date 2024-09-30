/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.ex.JctJunitConfigurerException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Base for defining a compiler-supplying arguments-provider for JUnit Jupiter parameterised test
 * support.
 *
 * <p>Each implementation is expected to provide:
 *
 * <ul>
 *   <li>A method {@link #initializeNewCompiler} that returns new instances of a
 *       {@link JctCompiler};</li>
 *   <li>A minimum acceptable language level for the compiler, as an integer;</li>
 *   <li>A maximum acceptable language level for the compiler, as an integer;</li>
 *   <li>An implementation of {@link AnnotationConsumer} that consumes the desired
 *       annotation. The details of the annotation should be extracted and a call
 *       to {@link #configure} should be made.</li>
 * </ul>
 *
 * <p>An example annotation would look like the following:
 *
 * <pre><code>
 * {@literal @ArgumentsSource(MyCompilersProvider.class)}
 * {@literal @ParameterizedTest(name = "for {0}")}
 * {@literal @Retention(RetentionPolicy.RUNTIME)}
 * {@literal @Tag("java-compiler-testing-test")}
 * {@literal @Target}({
 *     ElementType.ANNOTATION_TYPE,
 *     ElementType.METHOD,
 * })
 * {@literal @TestTemplate}
 * public {@literal @interface} MyCompilerTest {
 *     int minVersion() default Integer.MIN_VALUE;
 *     int maxVersion() default Integer.MAX_VALUE;
 *     Class&lt;? extends JctSimpleCompilerConfigurer&gt;[] configurers() default {};
 *     VersionStrategy versionStrategy() default VersionStrategy.RELEASE;
 * }
 * </code></pre>
 *
 * <p>...with the JUnit5 annotation provider being implemented as:
 *
 * <pre><code>
 * public final class MyCompilersProvider
 *     extends AbstractCompilersProvider
 *     implements AnnotationConsumer&lt;MyCompilerTest&gt; {
 *
 *   {@literal @Override}
 *   protected JctCompiler initializeNewCompiler() {
 *     return new MyCompilerImpl();
 *   }
 *
 *   {@literal @Override}
 *   protected int minSupportedVersion() {
 *     return 11;  // Support Java 11 as the minimum.
 *   }
 *
 *   {@literal @Override}
 *   protected int maxSupportedVersion() {
 *     return 19;  // Support Java 19 as the maximum.
 *   }
 *
 *   {@literal @Override}
 *   public void accept(MyCompilerTest annotation) {
 *     super.configure(
 *         annotation.minVersion(),
 *         annotation.maxVersion(),
 *         annotation.configurers(),
 *         annotation.versionStrategy(),
 *     );
 *   }
 * }
 * </code></pre>
 *
 * <p>This would enable you to define your test cases like so:
 *
 * <pre><code>
 * {@literal @MyCompilerTest(minVersion=13, maxVersion=17)}
 * void testSomething(JctCompiler compiler) {
 *   ...
 * }
 *
 * {@literal @MyCompilerTest(configurers=WerrorConfigurer.class)}
 * void testSomethingElse(JctCompiler compiler) {
 *   ...
 * }
 *
 * static class WerrorConfigurer implements JctCompilerConfigurer {
 *   {@literal @Override}
 *   public void configure(JctCompiler compiler) {
 *     compiler.failOnErrors(true);
 *   }
 * }
 * </code></pre>
 *
 * <p>Note that if you are running your tests within a JPMS module, you will need
 * to ensure that you declare your module to be {@code open} to {@code io.github.ascopes.jct},
 * otherwise this component will be unable to discover the constructor to initialise your configurer
 * correctly, and may raise an exception as a result.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class AbstractCompilersProvider implements ArgumentsProvider {

  // Values that are late-bound when configure() is called from the
  // AnnotationConsumer.
  private int minVersion;
  private int maxVersion;
  private Class<? extends JctCompilerConfigurer<?>>[] configurerClasses;
  private VersionStrategy versionStrategy;

  /**
   * Initialise this provider.
   */
  protected AbstractCompilersProvider() {
    minVersion = 0;
    maxVersion = Integer.MAX_VALUE;
    configurerClasses = emptyArray();
    versionStrategy = VersionStrategy.RELEASE;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return IntStream
        .rangeClosed(minVersion, maxVersion)
        .mapToObj(this::createCompilerForVersion)
        .peek(this::applyConfigurers)
        .map(Arguments::of);
  }

  /**
   * Configure this provider with parameters from annotations.
   *
   * <p>This is expected to be called from an implementation of {@link AnnotationConsumer}.
   *
   * <p>The minimum compiler version will be set to the {@code min} parameter, or
   * {@link #minSupportedVersion}, whichever is greater. This means annotations can pass
   * {@link Integer#MIN_VALUE} as a default value safely.
   *
   * <p>The maximum compiler version will be set to the {@code max} parameter, or
   * {@link #maxSupportedVersion}, whichever is smaller. This means annotations can pass
   * {@link Integer#MAX_VALUE} as a default value safely.
   *
   * <p>If implementations do not support specifying custom compiler configurers, then an empty
   * array must be passed for the {@code configurerClasses} parameter.
   *
   * <p>If implementations do not support changing the version strategy, then it is suggested to
   * pass {@link VersionStrategy#RELEASE} as the value for the {@code versionStrategy} parameter.
   *
   * @param min               the inclusive minimum compiler version to use.
   * @param max               the inclusive maximum compiler version to use.
   * @param configurerClasses the configurer classes to apply to each compiler.
   * @param versionStrategy   the version strategy to use.
   */
  protected final void configure(
      int min,
      int max,
      Class<? extends JctCompilerConfigurer<?>>[] configurerClasses,
      VersionStrategy versionStrategy
  ) {
    min = Math.max(min, minSupportedVersion());
    max = Math.min(max, maxSupportedVersion());

    if (max < 8 || min < 8) {
      throw new JctIllegalInputException("Cannot use a Java version less than Java 8");
    }

    if (min > max) {
      throw new JctIllegalInputException(
          "Cannot set min version to a version higher than the max version"
      );
    }

    minVersion = min;
    maxVersion = max;

    this.configurerClasses = requireNonNullValues(configurerClasses, "configurerClasses");
    this.versionStrategy = requireNonNull(versionStrategy, "versionStrategy");
  }

  /**
   * Initialise a new compiler.
   *
   * @return the compiler object.
   */
  protected abstract JctCompiler initializeNewCompiler();

  /**
   * Get the minimum supported compiler version.
   *
   * @return the minimum supported compiler version.
   * @since 1.0.0
   */
  protected abstract int minSupportedVersion();

  /**
   * Get the maximum supported compiler version.
   *
   * @return the minimum supported compiler version.
   * @since 1.0.0
   */
  protected abstract int maxSupportedVersion();

  private JctCompiler createCompilerForVersion(int version) {
    var compiler = initializeNewCompiler();
    versionStrategy.configureCompiler(compiler, version);
    return compiler;
  }

  private void applyConfigurers(JctCompiler compiler) {
    var classes = requireNonNull(configurerClasses);

    for (var configurerClass : classes) {
      var configurer = initializeConfigurer(configurerClass);

      try {
        configurer.configure(compiler);
  
      } catch (Exception ex) {
        if (isTestAbortedException(ex)) {
          throw (RuntimeException) ex;
        }

        throw new JctJunitConfigurerException(
            "Failed to configure compiler with configurer class " + configurerClass.getName(),
            ex
        );
      }
    }
  }

  private JctCompilerConfigurer<?> initializeConfigurer(
      Class<? extends JctCompilerConfigurer<?>> configurerClass
  ) {
    Constructor<? extends JctCompilerConfigurer<?>> constructor;

    try {
      constructor = configurerClass.getDeclaredConstructor();

    } catch (NoSuchMethodException ex) {
      throw new JctJunitConfigurerException(
          "No no-args constructor was found for configurer class " + configurerClass.getName(),
          ex
      );
    }

    try {
      // Force-enable reflective access. If the user is using a SecurityManager for any reason then
      // tough luck. JVM go bang.
      // If the module is not open to JCT, then we will get an InaccessibleObjectException that
      // we should wrap and rethrow.
      constructor.setAccessible(true);

    } catch (InaccessibleObjectException ex) {

      throw new JctJunitConfigurerException(
          "The constructor in " + configurerClass.getSimpleName() + " cannot be called from JCT."
              + "\n"
              + "This is likely because JPMS modules are in use and you have not granted "
              + "permission for JCT to access your classes reflectively."
              + "\n"
              + "To fix this, add the following line into your module-info.java within the "
              + "'module' block:"
              + "\n\n"
              + "    opens " + constructor.getDeclaringClass().getPackageName() + " to "
              + getClass().getModule().getName() + ";",
          ex
      );
    }

    try {
      return constructor.newInstance();

    } catch (ReflectiveOperationException ex) {
      if (ex instanceof InvocationTargetException) {
        var target = ((InvocationTargetException) ex).getTargetException();
        if (isTestAbortedException(target)) {
          target.addSuppressed(ex);
          throw (RuntimeException) target;
        }
      }

      throw new JctJunitConfigurerException(
          "Failed to initialise a new instance of configurer class " + configurerClass.getName(),
          ex
      );
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T>[] emptyArray() {
    return (Class<T>[]) new Class[0];
  }

  private static boolean isTestAbortedException(Throwable ex) {
    // Use string-based reflective lookup to prevent needing the OpenTest4J modules loaded at
    // runtime. We don't need to cover JUnit4 or TestNG here since this package specifically
    // deals with JUnit5 only.
    return ex.getClass().getName().equals("org.opentest4j.TestAbortedException");
  }
}
