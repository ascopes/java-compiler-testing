/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.testing.unit.junit;

import static io.github.ascopes.jct.testing.helpers.GenericMock.mockRaw;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.THROWABLE;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctCompilerConfigurer.JctSimpleCompilerConfigurer;
import io.github.ascopes.jct.ex.JctJunitConfigurerException;
import io.github.ascopes.jct.junit.AbstractCompilersProvider;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.TestAbortedException;

/**
 * {@link AbstractCompilersProvider} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("AbstractCompilersProvider tests")
class AbstractCompilersProviderTest {

  @DisplayName("Configuring the provider with a version too low will use the minimum version")
  @Test
  void configuringTheProviderWithTooLowVersionWillUseTheMinimumVersion() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(5, 17);
    var compilers = provider.provideArguments(mock(ExtensionContext.class))
        .map(args -> (JctCompiler<?, ?>) args.get()[0])
        .collect(Collectors.toList());

    // Then
    assertThat(compilers)
        .as("compilers that were initialised (%s)", compilers)
        .hasSize(17 - 8 + 1);

    assertSoftly(softly -> {
      for (var i = 0; i < compilers.size(); ++i) {
        softly.assertThat(compilers)
            .as("compilers[%d]", i)
            .element(i)
            .as("compilers[%d].getRelease()", i)
            .extracting(JctCompiler::getRelease, STRING)
            .isEqualTo("%d", 8 + i);
      }
    });
  }

  @DisplayName("Configuring the provider with a version too high will use the maximum version")
  @Test
  void configuringTheProviderWithTooHighVersionWillUseTheMaximumVersion() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(8, 30);
    var compilers = provider.provideArguments(mock(ExtensionContext.class))
        .map(args -> (JctCompiler<?, ?>) args.get()[0])
        .collect(Collectors.toList());

    // Then
    assertThat(compilers)
        .as("compilers that were initialised (%s)", compilers)
        .hasSize(17 - 8 + 1);

    assertSoftly(softly -> {
      for (var i = 0; i < compilers.size(); ++i) {
        softly.assertThat(compilers)
            .as("compilers[%d]", i)
            .element(i)
            .as("compilers[%d].getRelease()", i)
            .extracting(JctCompiler::getRelease, STRING)
            .isEqualTo("%d", 8 + i);
      }
    });
  }

  @DisplayName("Configuring the provider with a version below Java 8 will raise an exception")
  @CsvSource({
      "7, 12",
      "5, 7",
  })
  @ParameterizedTest(name = "for min = {0} and max = {1}")
  void configuringTheProviderWithPreJava8WillRaiseException(int min, int max) {
    // Given
    var provider = new CompilersProviderImpl(3, 17);

    // Then
    assertThatThrownBy(() -> provider.configureInternals(min, max))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot use a Java version less than Java 8");
  }

  @DisplayName(
      "Configuring the provider with a min version above the max version will raise an exception"
  )
  @Test
  void configuringTheProviderWithMinAboveMaxWillRaiseException() {
    // Given
    var provider = new CompilersProviderImpl(5, 17);

    // Then
    assertThatThrownBy(() -> provider.configureInternals(11, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot set min version to a version higher than the max version");
  }

  @DisplayName("Configuring the provider with a valid version range will use that range")
  @Test
  void configuringTheProviderWithValidVersionRangeWillUseThatRange() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15);
    var compilers = provider.provideArguments(mock(ExtensionContext.class))
        .map(args -> (JctCompiler<?, ?>) args.get()[0])
        .collect(Collectors.toList());

    // Then
    assertThat(compilers)
        .as("compilers that were initialised (%s)", compilers)
        .hasSize(6);

    assertSoftly(softly -> {
      for (var i = 0; i < compilers.size(); ++i) {
        softly.assertThat(compilers)
            .as("compilers[%d]", i)
            .element(i)
            .as("compilers[%d].getRelease()", i)
            .extracting(JctCompiler::getRelease, STRING)
            .isEqualTo("%d", 10 + i);
      }
    });
  }

  @DisplayName("Configuring the provider with configurers will initialise those configurers")
  @Test
  void configuringTheProviderWithConfigurersWillUseConfigurers() {
    // Given
    try (
        var foo = mockConstruction(FooConfigurer.class);
        var bar = mockConstruction(BarConfigurer.class);
        var baz = mockConstruction(BazConfigurer.class)
    ) {
      var provider = new CompilersProviderImpl(8, 17);

      // When
      provider.configureInternals(
          10, 15, FooConfigurer.class, BarConfigurer.class, BazConfigurer.class
      );
      var compilers = provider.provideArguments(mock(ExtensionContext.class))
          .map(args -> (JctCompiler<?, ?>) args.get()[0])
          .collect(Collectors.toList());

      // Then
      assertSoftly(softly -> {
        softly.assertThat(compilers)
            .as("compilers")
            .hasSize(6);

        softly.assertThat(foo.constructed())
            .as("FooConfigurer.instances")
            .hasSize(6);

        softly.assertThat(bar.constructed())
            .as("BarConfigurer.instances")
            .hasSize(6);

        softly.assertThat(baz.constructed())
            .as("BazConfigurer.instances")
            .hasSize(6);
      });

      assertSoftly(softly -> {
        for (var i = 0; i < 6; ++i) {
          var compiler = compilers.get(i);

          softly.assertThat(foo.constructed())
              .as("FooConfigurer.instance[%d]", i)
              .element(i)
              .satisfies(cfg -> verify(cfg).configure(compiler));

          softly.assertThat(bar.constructed())
              .as("BarConfigurer.instance[%d]", i)
              .element(i)
              .satisfies(cfg -> verify(cfg).configure(compiler));

          softly.assertThat(baz.constructed())
              .as("BazConfigurer.instance[%d]", i)
              .element(i)
              .satisfies(cfg -> verify(cfg).configure(compiler));
        }
      });
    }
  }

  @DisplayName("Configurers that throw TestAbortedException in constructors will be propagated")
  @Test
  void configurersThrowingTestAbortedExceptionInConstructorsWillPropagate() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15, AbortedConstructorConfigurer.class);

    // Then
    assertThatThrownBy(() -> provider.provideArguments(mock(ExtensionContext.class)).toArray())
        .isInstanceOf(TestAbortedException.class)
        .hasMessage("aborted!")
        .as("suppressed causes")
        .extracting(Throwable::getSuppressed, array(Throwable[].class))
        .singleElement(THROWABLE)
        .isInstanceOf(InvocationTargetException.class)
        .hasRootCauseInstanceOf(TestAbortedException.class)
        .hasRootCauseMessage("aborted!");
  }

  @DisplayName("Configurers that throw TestAbortedException when configuring will be propagated")
  @Test
  void configurersThrowingTestAbortedExceptionWhenConfiguringWillPropagate() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15, AbortedConfigureConfigurer.class);

    // Then
    assertThatThrownBy(() -> provider.provideArguments(mock(ExtensionContext.class)).toArray())
        .isInstanceOf(TestAbortedException.class)
        .hasMessage("aborted!")
        .hasNoSuppressedExceptions();
  }

  @DisplayName("Configurers that throw exceptions in constructors will be propagated")
  @Test
  void configurersThrowingExceptionsInConstructorsWillPropagate() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15, ThrowingConstructorConfigurer.class);

    // Then
    assertThatThrownBy(() -> provider.provideArguments(mock(ExtensionContext.class)).toArray())
        .isInstanceOf(JctJunitConfigurerException.class)
        .hasMessage(
            "Failed to initialise a new instance of configurer class %s",
            ThrowingConstructorConfigurer.class.getName()
        )
        .hasCauseInstanceOf(InvocationTargetException.class)
        .hasRootCauseInstanceOf(RuntimeException.class)
        .hasRootCauseMessage("Some error here");
  }


  @DisplayName("Providing abstract configurers will produce exceptions")
  @Test
  void abstractConfigurersWillProduceExceptions() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15, AbstractConfigurer.class);

    // Then
    assertThatThrownBy(() -> provider.provideArguments(mock(ExtensionContext.class)).toArray())
        .isInstanceOf(JctJunitConfigurerException.class)
        .hasMessage(
            "Failed to initialise a new instance of configurer class %s",
            AbstractConfigurer.class.getName()
        )
        .hasCauseInstanceOf(InstantiationException.class);
  }

  @DisplayName("Configurers that throw exceptions when configuring will be propagated")
  @Test
  void configurersThrowingExceptionsWhenConfiguringWillPropagate() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15, ThrowingConfigureConfigurer.class);

    // Then
    assertThatThrownBy(() -> provider.provideArguments(mock(ExtensionContext.class)).toArray())
        .isInstanceOf(JctJunitConfigurerException.class)
        .hasMessage(
            //"Failed to initialise a new instance of configurer class %s",
            "Failed to configure compiler with configurer class %s",
            ThrowingConfigureConfigurer.class.getName()
        )
        .hasCauseInstanceOf(RuntimeException.class)
        .hasRootCauseMessage("Some error here");
  }

  @DisplayName("Configurers that have non default constructors will produce exceptions")
  @Test
  void configurersWithNonDefaultConstructorsWillProduceExceptions() {
    // Given
    var provider = new CompilersProviderImpl(8, 17);

    // When
    provider.configureInternals(10, 15, NonDefaultConstructorConfigurer.class);

    // Then
    assertThatThrownBy(() -> provider.provideArguments(mock(ExtensionContext.class)).toArray())
        .isInstanceOf(JctJunitConfigurerException.class)
        .hasMessage(
            "No no-args constructor was found for configurer class %s",
            NonDefaultConstructorConfigurer.class.getName()
        )
        .hasCauseInstanceOf(NoSuchMethodException.class);
  }

  ///
  /// Test data and classes to operate upon.
  ///

  static final class CompilersProviderImpl extends AbstractCompilersProvider {

    private final int minSupportedVersion;
    private final int maxSupportedVersion;

    public CompilersProviderImpl(int minSupportedVersion, int maxSupportedVersion) {
      this.minSupportedVersion = minSupportedVersion;
      this.maxSupportedVersion = maxSupportedVersion;
    }

    @SafeVarargs
    final void configureInternals(
        int min,
        int max,
        Class<? extends JctSimpleCompilerConfigurer>... configurerClasses
    ) {
      configure(min, max, false, configurerClasses);
    }

    @Override
    protected JctCompiler<?, ?> compilerForVersion(int release) {
      var mock = mockRaw(JctCompiler.class)
          .<JctCompiler<?, ?>>upcastedTo()
          .build();

      when(mock.getRelease()).thenReturn(Integer.toString(release));

      return mock;
    }

    @Override
    protected int minSupportedVersion(boolean modules) {
      return minSupportedVersion;
    }

    @Override
    protected int maxSupportedVersion(boolean modules) {
      return maxSupportedVersion;
    }
  }

  /**
   * A configurer.
   */
  static class FooConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    FooConfigurer() {
      // Nothing to do here.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing.
    }
  }

  /**
   * A configurer.
   */
  static class BarConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    public BarConfigurer() {
      // Nothing to do here.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing.
    }
  }

  /**
   * A configurer.
   */
  static class BazConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    BazConfigurer() {
      // Nothing to do here.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing.
    }
  }

  /**
   * A configurer.
   */
  static class AbortedConstructorConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    AbortedConstructorConfigurer() {
      throw new TestAbortedException("aborted!");
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing; unreachable.
    }
  }

  /**
   * A configurer.
   */
  static class AbortedConfigureConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    AbortedConfigureConfigurer() {
      // Nothing to do here.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      throw new TestAbortedException("aborted!");
    }
  }

  /**
   * A configurer.
   */
  static class ThrowingConstructorConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    ThrowingConstructorConfigurer() {
      throw new RuntimeException("Some error here");
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing; unreachable.
    }
  }

  /**
   * An abstract configurer.
   */
  abstract static class AbstractConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    AbstractConfigurer() {
      // Nothing to do here.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing.
    }
  }

  /**
   * A configurer.
   */
  static class ThrowingConfigureConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    ThrowingConfigureConfigurer() {
      // Nothing to do here.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      throw new RuntimeException("Some error here");
    }
  }

  /**
   * A configurer.
   */
  static class NonDefaultConstructorConfigurer implements JctSimpleCompilerConfigurer {

    /**
     * Initialise the configurer.
     */
    NonDefaultConstructorConfigurer(@SuppressWarnings("unused") Object unusedArgument) {
      // Need a constructor with a non-default signature for the test.
    }

    @Override
    public void configure(JctCompiler<?, ?> compiler) {
      // Do nothing.
    }
  }
}
