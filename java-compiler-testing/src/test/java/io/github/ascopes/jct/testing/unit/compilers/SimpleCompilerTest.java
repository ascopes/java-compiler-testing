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

package io.github.ascopes.jct.testing.unit.compilers;

import static io.github.ascopes.jct.testing.helpers.MoreMocks.mockCast;
import static io.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static io.github.ascopes.jct.testing.helpers.MoreMocks.stubCast;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.compilers.AlreadyUsedCompilerException;
import io.github.ascopes.jct.compilers.Compiler.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.compilers.Compiler.CompilerConfigurer;
import io.github.ascopes.jct.compilers.Compiler.Logging;
import io.github.ascopes.jct.compilers.FlagBuilder;
import io.github.ascopes.jct.compilers.SimpleCompilationFactory;
import io.github.ascopes.jct.compilers.SimpleCompiler;
import io.github.ascopes.jct.paths.PathLocationRepository;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.testing.helpers.ReflectiveAccess;
import io.github.ascopes.jct.testing.helpers.TypeRef;
import io.github.ascopes.jct.testing.unit.compilers.SimpleCompilerTest.AttrTestPack.NullTests;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Answers;
import org.mockito.Mockito;

/**
 * {@link SimpleCompiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("SimpleCompiler tests")
class SimpleCompilerTest {

  @DisplayName("AlreadyUsedCompilerException is thrown on second call to compile()")
  @Test
  void alreadyUsedCompilerExceptionIsThrown() {
	  try (var compilationFactory = mockConstruction(SimpleCompilationFactory.class)) {
	    // Given
	    var jsr199Compiler = stub(JavaCompiler.class);
	    var flagBuilder = stub(FlagBuilder.class);
	   var compiler = new StubbedCompiler("foobar", jsr199Compiler, flagBuilder);

	    // When
	    compiler.compile();
	      
	    // Then
	    assertThatThrownBy(
	    		() -> compiler.compile()
	    ).isInstanceOf(AlreadyUsedCompilerException.class)
	        	    .hasMessageContaining("There has been a second call to compile() in this Compiler");
	  }
  }
	
  @DisplayName("name cannot be null")
  @Test
  void nameCannotBeNull() {
    // Then
    assertThatThrownBy(
        () -> new StubbedCompiler(
            null,
            stub(JavaCompiler.class),
            stub(FlagBuilder.class)
        )
    ).isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @DisplayName("jsr199Compiler cannot be null")
  @Test
  void jsr199CompilerCannotBeNull() {
    // Then
    assertThatThrownBy(
        () -> new StubbedCompiler(
            "foobar",
            null,
            stub(FlagBuilder.class)
        )
    ).isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("jsr199Compiler");
  }


  @DisplayName("flagBuilder cannot be null")
  @Test
  void flagBuilderCannotBeNull() {
    // Then
    assertThatThrownBy(
        () -> new StubbedCompiler(
            "foobar",
            stub(JavaCompiler.class),
            null
        )
    ).isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("flagBuilder");
  }

  @DisplayName("getFlagBuilder() should get the flag builder")
  @Test
  void getFlagBuilderShouldGetTheFlagBuilder() {
    // Given
    var expectedFlagBuilder = stub(FlagBuilder.class);
    var compiler = new StubbedCompiler("foo", stub(JavaCompiler.class), expectedFlagBuilder);

    // When
    var actualFlagBuilder = compiler.getFlagBuilder();

    // Then
    assertThat(actualFlagBuilder).isSameAs(expectedFlagBuilder);
  }

  @DisplayName("getJsr199Compiler() should get the JSR-199 compiler")
  @Test
  void getJsr199CompilerShouldGetTheJsr199Compiler() {
    // Given
    var expectedCompiler = stub(JavaCompiler.class);
    var compiler = new StubbedCompiler("foo", expectedCompiler, stub(FlagBuilder.class));

    // When
    var actualCompiler = compiler.getJsr199Compiler();

    // Then
    assertThat(actualCompiler).isSameAs(expectedCompiler);
  }

  @DisplayName("getName() should get the name of the compiler")
  @Test
  void getNameShouldGetTheNameOfTheCompiler() {
    // Given
    var expectedName = "Roy Rodgers McFreely with ID " + UUID.randomUUID();
    var compiler = new StubbedCompiler(
        expectedName,
        stub(JavaCompiler.class),
        stub(FlagBuilder.class)
    );

    // When
    var actualName = compiler.getName();

    // Then
    assertThat(actualName).isEqualTo(expectedName);
  }

  @SuppressWarnings("unchecked")
  @DisplayName("compile calls performEntireCompilation()")
  @Test
  void compileCallsPerformEntireCompilation() {
    // Given
    try (var compilationFactory = mockConstruction(SimpleCompilationFactory.class)) {
      var jsr199Compiler = stub(JavaCompiler.class);
      var flagBuilder = stub(FlagBuilder.class);
      var compiler = new StubbedCompiler("foobar", jsr199Compiler, flagBuilder);

      // When
      compiler.compile();

      // Then
      assertThat(compilationFactory.constructed())
          .singleElement()
          .extracting(factory -> (SimpleCompilationFactory<StubbedCompiler>) factory)
          .satisfies(factory -> verify(factory).compile(compiler, jsr199Compiler, flagBuilder));
    }
  }

  @DisplayName("Applying a configurer invokes the configurer with the compiler")
  @Test
  void applyingConfigurerInvokesConfigurerWithCompiler() throws Exception {
    // Given
    var compiler = new StubbedCompiler();
    var configurer = mockCast(new TypeRef<CompilerConfigurer<StubbedCompiler, Exception>>() {});

    // When
    var result = compiler.configure(configurer);

    // Then
    assertThat(result).isSameAs(compiler);
    then(configurer).should().configure(compiler);
  }

  @DisplayName("addPaths should pass the parameters to the file repository")
  @Test
  void addPathsDelegatesToFileRepository() {
    // Given
    var constructor = Mockito.mockConstruction(
        PathLocationRepository.class,
        withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS)
    );

    try (constructor) {
      var compiler = new StubbedCompiler();
      var location = stub(Location.class);
      var paths = stubCast(new TypeRef<Collection<? extends Path>>() {});

      // When
      compiler.addPaths(location, paths);

      // Then
      assertThat(constructor.constructed())
          .singleElement()
          .satisfies(
              repo -> verify(repo).getOrCreateManager(location),
              repo -> verify(repo.getOrCreateManager(location)).addPaths(paths)
          );
    }
  }

  @DisplayName("addRamPaths should pass the parameters to the file repository")
  @Test
  void addRamPathsDelegatesToFileRepository() {
    // Given
    var constructor = Mockito.mockConstruction(
        PathLocationRepository.class,
        withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS)
    );

    try (constructor) {
      var compiler = new StubbedCompiler();
      var location = stub(Location.class);
      var paths = stubCast(new TypeRef<Collection<? extends RamPath>>() {});

      // When
      compiler.addRamPaths(location, paths);

      // Then
      assertThat(constructor.constructed())
          .singleElement()
          .satisfies(
              repo -> verify(repo).getOrCreateManager(location),
              repo -> verify(repo.getOrCreateManager(location)).addRamPaths(paths)
          );
    }
  }

  @DisplayName("getPathLocationRepository() should get the PathLocationRepository")
  @Test
  void getPathLocationRepositoryShouldGetThePathLocationRepository() {
    // Given
    var compiler = new StubbedCompiler();
    var expectedPathLocationRepository = ReflectiveAccess.getField(
        compiler,
        "fileRepository",
        PathLocationRepository.class
    );

    // When
    var actualPathLocationRepository = compiler.getPathLocationRepository();

    // Then
    assertThat(actualPathLocationRepository).isSameAs(expectedPathLocationRepository);
  }

  @DisplayName("getFileCharset and fileCharset tests")
  @TestFactory
  AttrTestPack<?> fileCharsetWorksCorrectly() {
    return new AttrTestPack<>(
        "fileCharset",
        SimpleCompiler::getFileCharset,
        SimpleCompiler::fileCharset,
        NullTests.EXPECT_DISALLOW,
        StandardCharsets.UTF_8,
        StandardCharsets.US_ASCII,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.UTF_16
    );
  }

  @DisplayName("isVerbose and verbose tests")
  @TestFactory
  AttrTestPack<?> verboseWorksCorrectly() {
    return AttrTestPack.forBooleanAttr(
        "verbose",
        StubbedCompiler::isVerbose,
        StubbedCompiler::verbose,
        StubbedCompiler.DEFAULT_VERBOSE
    );
  }

  @DisplayName("isPreviewFeatures and previewFeatures tests")
  @TestFactory
  AttrTestPack<?> previewFeaturesTests() {
    return AttrTestPack.forBooleanAttr(
        "previewFeatures",
        StubbedCompiler::isPreviewFeatures,
        StubbedCompiler::previewFeatures,
        StubbedCompiler.DEFAULT_PREVIEW_FEATURES
    );
  }

  @DisplayName("isShowWarnings and showWarnings tests")
  @TestFactory
  AttrTestPack<?> showWarningsTests() {
    return AttrTestPack.forBooleanAttr(
        "showWarnings",
        StubbedCompiler::isShowWarnings,
        StubbedCompiler::showWarnings,
        StubbedCompiler.DEFAULT_SHOW_WARNINGS
    );
  }

  @DisplayName("isShowDeprecationWarnings and showDeprecationWarnings tests")
  @TestFactory
  AttrTestPack<?> showDeprecationWarningsTests() {
    return AttrTestPack.forBooleanAttr(
        "showDeprecationWarnings",
        StubbedCompiler::isShowDeprecationWarnings,
        StubbedCompiler::showDeprecationWarnings,
        StubbedCompiler.DEFAULT_SHOW_DEPRECATION_WARNINGS
    );
  }

  @DisplayName("isFailOnWarnings and failOnWarnings tests")
  @TestFactory
  AttrTestPack<?> failOnWarningsTests() {
    return AttrTestPack.forBooleanAttr(
        "failOnWarnings",
        StubbedCompiler::isFailOnWarnings,
        StubbedCompiler::failOnWarnings,
        StubbedCompiler.DEFAULT_FAIL_ON_WARNINGS
    );
  }

  @DisplayName("getAnnotationProcessorOptions and addAnnotationProcessorOptions tests")
  @TestFactory
  CollectionAttrTestPack<?, ?> annotationProcessorOptionsWorksCorrectly() {
    return new CollectionAttrTestPack<>(
        "annotationProcessorOptions",
        ArrayList::new,
        () -> UUID.randomUUID().toString(),
        StubbedCompiler::getAnnotationProcessorOptions,
        StubbedCompiler::addAnnotationProcessorOptions,
        Collections.emptyList()
    );
  }

  @DisplayName("getAnnotationProcessors and addAnnotationProcessors tests")
  @TestFactory
  CollectionAttrTestPack<?, ?> annotationProcessorsWorksCorrectly() {
    return new CollectionAttrTestPack<>(
        "annotationProcessors",
        LinkedHashSet::new,
        () -> stub(Processor.class),
        StubbedCompiler::getAnnotationProcessors,
        StubbedCompiler::addAnnotationProcessors,
        Collections.emptySet()
    );
  }

  @DisplayName("getCompilerOptions and addCompilerOptions tests")
  @TestFactory
  CollectionAttrTestPack<?, ?> compilerOptionsWorksCorrectly() {
    return new CollectionAttrTestPack<>(
        "compilerOptions",
        ArrayList::new,
        () -> UUID.randomUUID().toString(),
        StubbedCompiler::getCompilerOptions,
        StubbedCompiler::addCompilerOptions,
        Collections.emptyList()
    );
  }

  @DisplayName("getRuntimeOptions and addRuntimeOptions tests")
  @TestFactory
  CollectionAttrTestPack<?, ?> runtimeOptionsWorksCorrectly() {
    return new CollectionAttrTestPack<>(
        "runtimeOptions",
        ArrayList::new,
        () -> UUID.randomUUID().toString(),
        StubbedCompiler::getRuntimeOptions,
        StubbedCompiler::addRuntimeOptions,
        Collections.emptyList()
    );
  }

  @DisplayName("getRelease and release tests")
  @TestFactory
  AttrTestPack<?> releaseWorksCorrectly() {
    return new JavaVersionAttrTestPack(
        "release",
        StubbedCompiler::getRelease,
        StubbedCompiler::release,
        (compiler, release) -> dynamicTest(
            "setting a release clears the source version",
            () -> {
              // Given
              compiler.source("10");

              // When
              compiler.release(release);

              // Then
              assertThat(compiler.getSource()).isNotPresent();
            }
        ),
        (compiler, release) -> dynamicTest(
            "setting a release clears the target version",
            () -> {
              // Given
              compiler.target("10");

              // When
              compiler.release(release);

              // Then
              assertThat(compiler.getTarget()).isNotPresent();
            }
        )
    );
  }

  @DisplayName("getSource and source tests")
  @TestFactory
  AttrTestPack<?> sourceWorksCorrectly() {
    return new JavaVersionAttrTestPack(
        "source",
        StubbedCompiler::getSource,
        StubbedCompiler::source,
        (compiler, source) -> dynamicTest(
            "setting a source version clears the release version",
            () -> {
              // Given
              compiler.release("10");

              // When
              compiler.source(source);

              // Then
              assertThat(compiler.getRelease()).isNotPresent();
            }
        )
    );
  }

  @DisplayName("getTarget and target tests")
  @TestFactory
  AttrTestPack<?> targetWorksCorrectly() {
    return new JavaVersionAttrTestPack(
        "target",
        StubbedCompiler::getTarget,
        StubbedCompiler::target,
        (compiler, target) -> dynamicTest(
            "setting a target version clears the release version",
            () -> {
              // Given
              compiler.release("10");

              // When
              compiler.target(target);

              // Then
              assertThat(compiler.getRelease()).isNotPresent();
            }
        )
    );
  }

  @DisplayName("isInheritClassPath and inheritClassPath tests")
  @TestFactory
  AttrTestPack<?> inheritClassPathWorksAsExpected() {
    return AttrTestPack.forBooleanAttr(
        "inheritClassPath",
        StubbedCompiler::isInheritClassPath,
        StubbedCompiler::inheritClassPath,
        StubbedCompiler.DEFAULT_INHERIT_CLASS_PATH
    );
  }

  @DisplayName("isInheritModulePath and inheritModulePath tests")
  @TestFactory
  AttrTestPack<?> inheritModulePathWorksAsExpected() {
    return AttrTestPack.forBooleanAttr(
        "inheritModulePath",
        StubbedCompiler::isInheritModulePath,
        StubbedCompiler::inheritModulePath,
        StubbedCompiler.DEFAULT_INHERIT_MODULE_PATH
    );
  }

  @DisplayName("isInheritPlatformClassPath and inheritPlatformClassPath tests")
  @TestFactory
  AttrTestPack<?> inheritPlatformClassPathWorksAsExpected() {
    return AttrTestPack.forBooleanAttr(
        "inheritModulePath",
        StubbedCompiler::isInheritPlatformClassPath,
        StubbedCompiler::inheritPlatformClassPath,
        StubbedCompiler.DEFAULT_INHERIT_PLATFORM_CLASS_PATH
    );
  }

  @DisplayName("isInheritSystemModulePath and inheritSystemModulePath tests")
  @TestFactory
  AttrTestPack<?> inheritSystemModulePathWorksAsExpected() {
    return AttrTestPack.forBooleanAttr(
        "inheritSystemModulePath",
        StubbedCompiler::isInheritSystemModulePath,
        StubbedCompiler::inheritSystemModulePath,
        StubbedCompiler.DEFAULT_INHERIT_SYSTEM_MODULE_PATH
    );
  }

  @DisplayName("getLocale and locale tests")
  @TestFactory
  AttrTestPack<?> localeWorksAsExpected() {
    return new AttrTestPack<>(
        "locale",
        StubbedCompiler::getLocale,
        StubbedCompiler::locale,
        NullTests.EXPECT_DISALLOW,
        StubbedCompiler.DEFAULT_LOCALE,
        Locale.UK,
        Locale.ENGLISH,
        Locale.CANADA_FRENCH,
        Locale.JAPANESE
    );
  }

  @DisplayName("getLogCharset and logCharset tests")
  @TestFactory
  AttrTestPack<?> logCharsetWorksAsExpected() {
    return new AttrTestPack<>(
        "logCharset",
        StubbedCompiler::getLogCharset,
        StubbedCompiler::logCharset,
        NullTests.EXPECT_DISALLOW,
        StubbedCompiler.DEFAULT_LOG_CHARSET,
        StandardCharsets.UTF_8,
        StandardCharsets.US_ASCII,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.UTF_16
    );
  }

  @DisplayName("getFileManagerLogging and fileManagerLogging tests")
  @TestFactory
  AttrTestPack<?> fileManagerLoggingWorksAsExpected() {
    return AttrTestPack.forEnum(
        "fileManagerLogging",
        StubbedCompiler::getFileManagerLogging,
        StubbedCompiler::fileManagerLogging,
        NullTests.EXPECT_DISALLOW,
        StubbedCompiler.DEFAULT_FILE_MANAGER_LOGGING,
        Logging.class
    );
  }

  @DisplayName("getDiagnosticLogging and diagnosticLogging tests")
  @TestFactory
  AttrTestPack<?> diagnosticLoggingWorksAsExpected() {
    return AttrTestPack.forEnum(
        "diagnosticLogging",
        StubbedCompiler::getDiagnosticLogging,
        StubbedCompiler::diagnosticLogging,
        NullTests.EXPECT_DISALLOW,
        StubbedCompiler.DEFAULT_DIAGNOSTIC_LOGGING,
        Logging.class
    );
  }

  @DisplayName("getAnnotationProcessorDiscovery and annotationProcessorDiscovery tests")
  @TestFactory
  AttrTestPack<?> annotationProcessorDiscoveryWorksAsExpected() {
    return AttrTestPack.forEnum(
        "annotationProcessorDiscovery",
        StubbedCompiler::getAnnotationProcessorDiscovery,
        StubbedCompiler::annotationProcessorDiscovery,
        NullTests.EXPECT_DISALLOW,
        StubbedCompiler.DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY,
        AnnotationProcessorDiscovery.class
    );
  }

  @DisplayName("toString() returns the name of the compiler")
  @Test
  void toStringReturnsTheNameOfTheCompiler() {
    // Given
    var expectedName = UUID.randomUUID().toString();
    var compiler = new StubbedCompiler(
        expectedName,
        stub(JavaCompiler.class),
        stub(FlagBuilder.class)
    );

    // Then
    assertThat(compiler)
        .hasToString(expectedName);
  }

  ///////////////////////////
  /// Utilities and stubs ///
  ///////////////////////////

  @SuppressWarnings("SameParameterValue")
  static class AttrTestPack<T> implements Iterable<DynamicTest> {

    final String name;
    final Function<? super StubbedCompiler, T> getter;
    final BiFunction<? super StubbedCompiler, T, ? extends StubbedCompiler> setter;
    final NullTests nullTests;
    final T defaultValue;
    final T[] valuesToTest;

    @SafeVarargs
    AttrTestPack(
        String name,
        Function<? super StubbedCompiler, T> getter,
        BiFunction<? super StubbedCompiler, T, ? extends StubbedCompiler> setter,
        NullTests nullTests,
        T defaultValue,
        T... valuesToTest
    ) {
      this.name = name;
      this.getter = getter;
      this.setter = setter;
      this.nullTests = nullTests;
      this.defaultValue = defaultValue;
      this.valuesToTest = valuesToTest;
    }

    @Override
    public Iterator<DynamicTest> iterator() {
      var tests = new ArrayList<DynamicTest>();

      Stream
          .of(valuesToTest)
          .flatMap(value -> Stream.of(
              canSetValue(value),
              setterReturnsCompiler(value)
          ))
          .forEach(tests::add);

      tests.add(defaultValueIsExpected());

      switch (nullTests) {
        case EXPECT_ALLOW:
          tests.add(setterAllowsNulls());
          break;

        case EXPECT_DISALLOW:
          tests.add(setterDisallowsNulls());
          break;

        default:
          // No additional tests.
          break;
      }

      return tests.iterator();
    }

    private DynamicTest canSetValue(T expected) {
      return dynamicTest(
          "I can set " + name + " to " + expected,
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // When
            setter.apply(compiler, expected);
            var actual = getter.apply(compiler);

            // Then
            assertThat(actual).isEqualTo(expected);
          }
      );
    }

    private DynamicTest setterReturnsCompiler(T expected) {
      return dynamicTest(
          "Setter for " + name + " returns compiler when provided with value " + expected,
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // When
            var returnedCompiler = setter.apply(compiler, expected);

            // Then
            assertThat(returnedCompiler).isEqualTo(compiler);
          }
      );
    }

    private DynamicTest defaultValueIsExpected() {
      return dynamicTest(
          name + " defaults to value " + defaultValue,
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // When
            var actual = getter.apply(compiler);

            // Then
            assertThat(actual).isEqualTo(defaultValue);
          }
      );
    }

    private DynamicTest setterAllowsNulls() {
      return dynamicTest(
          "Setter for " + name + " allows null values being set",
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // Then
            assertThatCode(() -> setter.apply(compiler, null)).doesNotThrowAnyException();
            assertThat(getter.apply(compiler)).isNull();
          }
      );
    }

    private DynamicTest setterDisallowsNulls() {
      return dynamicTest(
          "Setter for " + name + " disallows null values being set",
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // Then
            assertThatThrownBy(() -> setter.apply(compiler, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage(name);
          }
      );
    }

    static AttrTestPack<Boolean> forBooleanAttr(
        String name,
        Function<? super StubbedCompiler, Boolean> getter,
        BiFunction<? super StubbedCompiler, Boolean, ? extends StubbedCompiler> setter,
        boolean defaultValue
    ) {
      return new AttrTestPack<>(
          name,
          getter,
          setter,
          NullTests.SKIP,
          defaultValue,
          true,
          false
      );
    }

    static <E extends Enum<E>> AttrTestPack<E> forEnum(
        String name,
        Function<? super StubbedCompiler, E> getter,
        BiFunction<? super StubbedCompiler, E, ? extends StubbedCompiler> setter,
        NullTests nullTests,
        E defaultValue,
        Class<E> type
    ) {
      return new AttrTestPack<>(
          name,
          getter,
          setter,
          nullTests,
          defaultValue,
          type.getEnumConstants()
      );
    }

    enum NullTests {
      EXPECT_ALLOW,
      EXPECT_DISALLOW,
      SKIP,
    }
  }

  /**
   * Common tests to apply to any attributes that allow adding elements to a collection.
   *
   * <p>This object can be returned from a test method annotated with {@link TestFactory}. JUnit
   * will execute the tests for you using this.
   *
   * @param <T> the element type.
   * @param <U> the collection type to expect from the getter.
   * @author Ashley Scopes
   */
  static class CollectionAttrTestPack<T, U extends Collection<T>> implements Iterable<DynamicTest> {

    private final String name;
    private final Supplier<? extends Collection<T>> collectionSupplier;
    private final Supplier<? extends T> elementSupplier;
    private final Function<? super StubbedCompiler, U> getter;
    private final BiFunction<? super StubbedCompiler, ? super Collection<T>, StubbedCompiler> adder;
    private final U defaultValue;

    CollectionAttrTestPack(
        String name,
        Supplier<? extends Collection<T>> collectionSupplier,
        Supplier<? extends T> elementSupplier,
        Function<? super StubbedCompiler, U> getter,
        BiFunction<? super StubbedCompiler, ? super Collection<T>, StubbedCompiler> adder,
        U defaultValue
    ) {
      this.name = name;
      this.collectionSupplier = collectionSupplier;
      this.elementSupplier = elementSupplier;
      this.getter = getter;
      this.adder = adder;
      this.defaultValue = defaultValue;
    }

    @Override
    public Iterator<DynamicTest> iterator() {
      var tests = new ArrayList<DynamicTest>();

      for (int consecutiveCalls = 1; consecutiveCalls <= 5; ++consecutiveCalls) {
        tests.add(createAddItemsTest(consecutiveCalls));
      }

      tests.add(unmodifiableGetterTest());
      tests.add(getterReturnsSnapshotTest());
      tests.add(defaultValueIsExpected());
      tests.add(adderDisallowsNullIterablesTest());
      tests.add(adderDisallowsNullElementsTest());
      return tests.iterator();
    }

    private DynamicTest createAddItemsTest(int consecutiveCalls) {
      return dynamicTest(
          "I can add multiple items successfully in " + consecutiveCalls + " call(s)",
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // When
            var expected = new ArrayList<T>();
            for (int call = 1; call <= consecutiveCalls; ++call) {
              var next = Stream
                  .generate(elementSupplier)
                  .limit(3)
                  .collect(Collectors.toCollection(collectionSupplier));
              expected.addAll(next);
              adder.apply(compiler, next);
            }
            var actual = getter.apply(compiler);

            // Then (we vary this depending on the type we are observing)
            if (actual instanceof List<?>) {
              assertThat(actual)
                  .hasSize(actual.size())
                  .containsExactlyElementsOf(expected);
            } else if (actual instanceof Set<?>) {
              assertThat(actual)
                  .hasSize(actual.size())
                  .containsExactlyInAnyOrderElementsOf(actual);
            } else {
              throw new UnsupportedOperationException(
                  "Please implement checks for testing " + actual.getClass().getName()
              );
            }
          }
      );
    }

    private DynamicTest unmodifiableGetterTest() {
      return dynamicTest(
          "Getter for " + name + " returns an unmodifiable result",
          () -> {
            // Given
            var compiler = new StubbedCompiler();
            var expected = collectionSupplier.get();
            for (int i = 0; i < 3; ++i) {
              expected.add(elementSupplier.get());
              expected.add(elementSupplier.get());
            }

            // When
            adder.apply(compiler, expected);
            var actual = getter.apply(compiler);

            // Then
            assertThat(actual).isUnmodifiable();
          }
      );
    }

    private DynamicTest getterReturnsSnapshotTest() {
      return dynamicTest(
          "Getter for " + name + " returns a snapshot result",
          () -> {
            // Given
            var compiler = new StubbedCompiler();
            var firstExpected = collectionSupplier.get();
            for (int i = 0; i < 3; ++i) {
              firstExpected.add(elementSupplier.get());
            }
            var secondExpected = collectionSupplier.get();
            for (int i = 0; i < 3; ++i) {
              secondExpected.add(elementSupplier.get());
            }

            // When
            adder.apply(compiler, firstExpected);
            var firstActual = getter.apply(compiler);
            adder.apply(compiler, secondExpected);
            var secondActual = getter.apply(compiler);

            // Then
            assertThat(firstActual)
                .hasSize(3)
                .isNotEqualTo(secondActual);

            assertThat(secondActual)
                .hasSize(6);
          }
      );
    }

    private DynamicTest defaultValueIsExpected() {
      return dynamicTest(
          name + " defaults to " + defaultValue,
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // When
            var actual = getter.apply(compiler);

            // Then
            assertThat(actual).isEqualTo(defaultValue);
          }
      );
    }

    private DynamicTest adderDisallowsNullIterablesTest() {
      return dynamicTest(
          "Adder for " + name + " disallows null iterables being added",
          () -> {
            // Given
            var compiler = new StubbedCompiler();

            // Then
            assertThatThrownBy(() -> adder.apply(compiler, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage(name);
          }
      );
    }

    private DynamicTest adderDisallowsNullElementsTest() {
      return dynamicTest(
          "Adder for " + name + " disallows null elements being added",
          () -> {
            // Given
            var elements = collectionSupplier.get();
            elements.add(elementSupplier.get());
            elements.add(elementSupplier.get());
            elements.add(elementSupplier.get());
            elements.add(null);
            elements.add(elementSupplier.get());

            var compiler = new StubbedCompiler();

            // Then
            assertThatThrownBy(() -> adder.apply(compiler, elements))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("%s[%d]", name, 3);
          }
      );
    }
  }

  /**
   * Common tests to apply to any attributes that hold a version number for the Java spec to use.
   *
   * <p>This object can be returned from a test method annotated with {@link TestFactory}. JUnit
   * will execute the tests for you using this.
   *
   * @author Ashley Scopes
   */
  static class JavaVersionAttrTestPack extends AttrTestPack<String> {

    private final BiFunction<StubbedCompiler, String, DynamicTest>[] additionalTests;

    @SafeVarargs
    JavaVersionAttrTestPack(
        String name,
        Function<? super StubbedCompiler, Optional<String>> getter,
        BiFunction<? super StubbedCompiler, String, StubbedCompiler> setter,
        BiFunction<StubbedCompiler, String, DynamicTest>... additionalTests
    ) {
      super(
          name,
          getter.andThen(opt -> opt.orElse(null)),
          setter,
          NullTests.EXPECT_ALLOW,
          null,
          "7",
          "8",
          "9",
          "10",
          "11"
      );
      this.additionalTests = additionalTests;
    }

    @Override
    public Iterator<DynamicTest> iterator() {
      var tests = new ArrayList<DynamicTest>();
      super.iterator().forEachRemaining(tests::add);

      for (var test : additionalTests) {
        for (var valueToTest : valuesToTest) {
          tests.add(test.apply(new StubbedCompiler(), valueToTest));
        }
      }

      return tests.iterator();
    }
  }

  static class StubbedCompiler extends SimpleCompiler<StubbedCompiler> {

    StubbedCompiler() {
      this("stubbed", stubCast(new TypeRef<>() {}), stubCast(new TypeRef<>() {}));
    }

    StubbedCompiler(String name, JavaCompiler jsr199Compiler, FlagBuilder flagBuilder) {
      super(name, jsr199Compiler, flagBuilder);
    }
  }
}
