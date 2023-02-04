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
package io.github.ascopes.jct.tests.unit.compilers;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someBoolean;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someFlags;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someInt;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelease;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static io.github.ascopes.jct.tests.helpers.GenericMock.mockRaw;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.AbstractJctCompiler;
import io.github.ascopes.jct.compilers.CompilationMode;
import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import io.github.ascopes.jct.compilers.JctFlagBuilder;
import io.github.ascopes.jct.compilers.JctFlagBuilderFactory;
import io.github.ascopes.jct.compilers.Jsr199CompilerFactory;
import io.github.ascopes.jct.compilers.impl.JctCompilationFactoryImpl;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.JctFileManagerFactory;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import org.assertj.core.api.AbstractObjectAssert;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedConstruction.Context;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link AbstractJctCompiler}.
 *
 * @author Ashley Scopes
 */
@DisplayName("AbstractJctCompiler tests")
@ExtendWith(MockitoExtension.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class AbstractJctCompilerTest {

  @Mock(answer = Answers.RETURNS_MOCKS)
  JctFlagBuilderFactory flagBuilderFactory;

  @Mock(answer = Answers.RETURNS_MOCKS)
  Jsr199CompilerFactory jsr199CompilerFactory;

  @Mock(answer = Answers.RETURNS_MOCKS)
  JctFileManagerFactory fileManagerFactory;

  String name;
  String defaultRelease;
  CompilerImpl compiler;

  @BeforeEach
  void setUp() {
    name = someText();
    defaultRelease = Integer.toString(someInt(11, 21));
    compiler = spy(new CompilerImpl(name, defaultRelease));
  }

  @DisplayName("AbstractJctCompiler constructor tests")
  @Nested
  @SuppressWarnings("ConstantConditions")
  @Order(Integer.MIN_VALUE)
  class ConstructorTest {

    @DisplayName("constructor raises a NullPointerException if name is null")
    @Test
    void constructorRaisesNullPointerExceptionIfNameIsNull() {
      // Then
      assertThatThrownBy(() -> new CompilerImpl(null, defaultRelease))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("name");
    }

    @DisplayName("constructor initialises name correctly")
    @Test
    void constructorInitialisesNameCorrectly() {
      // Then
      assertThatCompilerField("name")
          .isSameAs(name);
    }

    @DisplayName("constructor initialises annotationProcessors to empty list")
    @Test
    void constructorInitialisesAnnotationProcessorsToEmptyList() {
      // Then
      assertThatCompilerField("annotationProcessors")
          .asList()
          .isEmpty();
    }

    @DisplayName("constructor initialises annotationProcessorOptions to empty list")
    @Test
    void constructorInitialisesAnnotationProcessorOptionsToEmptyList() {
      // Then
      assertThatCompilerField("annotationProcessorOptions")
          .asList()
          .isEmpty();
    }

    @DisplayName("constructor initialises compilerOptions to empty list")
    @Test
    void constructorInitialisesCompilerOptionsToEmptyList() {
      // Then
      assertThatCompilerField("compilerOptions")
          .asList()
          .isEmpty();
    }

    @DisplayName("constructor initialises showWarnings to default value")
    @Test
    void constructorInitialisesShowWarningsToDefaultValue() {
      // Then
      assertThatCompilerField("showWarnings")
          .isEqualTo(JctCompiler.DEFAULT_SHOW_WARNINGS);
    }

    @DisplayName("constructor initialises showDeprecationWarnings to default value")
    @Test
    void constructorInitialisesShowDeprecationWarningsToDefaultValue() {
      // Then
      assertThatCompilerField("showDeprecationWarnings")
          .isEqualTo(JctCompiler.DEFAULT_SHOW_DEPRECATION_WARNINGS);
    }

    @DisplayName("constructor initialises failOnWarnings to default value")
    @Test
    void constructorInitialisesFailOnWarningsToDefaultValue() {
      // Then
      assertThatCompilerField("failOnWarnings")
          .isEqualTo(JctCompiler.DEFAULT_FAIL_ON_WARNINGS);
    }

    @DisplayName("constructor initialises compilationMode to default value")
    @Test
    void constructorInitialisesCompilationModeToDefaultValue() {
      // Then
      assertThatCompilerField("compilationMode")
          .isEqualTo(JctCompiler.DEFAULT_COMPILATION_MODE);
    }

    @DisplayName("constructor initialises locale to default value")
    @Test
    void constructorInitialisesLocaleToDefaultValue() {
      // Then
      assertThatCompilerField("locale")
          .isEqualTo(JctCompiler.DEFAULT_LOCALE);
    }

    @DisplayName("constructor initialises logCharset to default value")
    @Test
    void constructorInitialisesLogCharsetToDefaultValue() {
      // Then
      assertThatCompilerField("logCharset")
          .isEqualTo(JctCompiler.DEFAULT_LOG_CHARSET);
    }

    @DisplayName("constructor initialises verbose to default value")
    @Test
    void constructorInitialisesVerboseDefaultValue() {
      // Then
      assertThatCompilerField("verbose")
          .isEqualTo(JctCompiler.DEFAULT_VERBOSE);
    }

    @DisplayName("constructor initialises previewFeatures to default value")
    @Test
    void constructorInitialisesPreviewFeaturesToDefaultValue() {
      // Then
      assertThatCompilerField("previewFeatures")
          .isEqualTo(JctCompiler.DEFAULT_PREVIEW_FEATURES);
    }

    @DisplayName("constructor initialises release to null")
    @Test
    void constructorInitialisesReleaseToNull() {
      // Then
      assertThatCompilerField("release").isNull();
    }

    @DisplayName("constructor initialises source to null")
    @Test
    void constructorInitialisesSourceToNull() {
      // Then
      assertThatCompilerField("source").isNull();
    }

    @DisplayName("constructor initialises target to null")
    @Test
    void constructorInitialisesTargetToNull() {
      // Then
      assertThatCompilerField("target").isNull();
    }

    @DisplayName("constructor initialises diagnosticLoggingMode to default value")
    @Test
    void constructorInitialisesDiagnosticLoggingModeToDefaultValue() {
      // Then
      assertThatCompilerField("diagnosticLoggingMode")
          .isEqualTo(JctCompiler.DEFAULT_DIAGNOSTIC_LOGGING_MODE);
    }

    @DisplayName("constructor initialises fixJvmModulePathMismatch to default value")
    @Test
    void constructorInitialisesFixJvmModulePathMismatchToDefaultValue() {
      // Then
      assertThatCompilerField("fixJvmModulePathMismatch")
          .isEqualTo(JctCompiler.DEFAULT_FIX_JVM_MODULE_PATH_MISMATCH);
    }

    @DisplayName("constructor initialises inheritClassPath to default value")
    @Test
    void constructorInitialisesInheritClassPathToDefaultValue() {
      // Then
      assertThatCompilerField("inheritModulePath")
          .isEqualTo(JctCompiler.DEFAULT_INHERIT_CLASS_PATH);
    }

    @DisplayName("constructor initialises inheritModulePath to default value")
    @Test
    void constructorInitialisesInheritModulePathToDefaultValue() {
      // Then
      assertThatCompilerField("inheritModulePath")
          .isEqualTo(JctCompiler.DEFAULT_INHERIT_MODULE_PATH);
    }

    @DisplayName("constructor initialises inheritPlatformClassPath to default value")
    @Test
    void constructorInitialisesInheritPlatformClassPathToDefaultValue() {
      // Then
      assertThatCompilerField("inheritPlatformClassPath")
          .isEqualTo(JctCompiler.DEFAULT_INHERIT_PLATFORM_CLASS_PATH);
    }

    @DisplayName("constructor initialises inheritSystemModulePath to default value")
    @Test
    void constructorInitialisesInheritSystemModulePathToDefaultValue() {
      // Then
      assertThatCompilerField("inheritSystemModulePath")
          .isEqualTo(JctCompiler.DEFAULT_INHERIT_SYSTEM_MODULE_PATH);
    }

    @DisplayName("constructor initialises fileManagerLoggingMode to default value")
    @Test
    void constructorInitialisesFileManagerLoggingModeToDefaultValue() {
      // Then
      assertThatCompilerField("fileManagerLoggingMode")
          .isEqualTo(JctCompiler.DEFAULT_FILE_MANAGER_LOGGING_MODE);
    }

    @DisplayName("constructor initialises annotationProcessorDiscovery to default value")
    @Test
    void constructorInitialisesAnnotationProcessorDiscoveryToDefaultValue() {
      // Then
      assertThatCompilerField("annotationProcessorDiscovery")
          .isEqualTo(JctCompiler.DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY);
    }
  }

  @ExtendWith(MockitoExtension.class)
  @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
  abstract class AbstractCompileTestTemplate {

    @Mock
    Workspace workspace;

    List<String> flags;

    @Mock
    JctFileManager fileManager;

    @Mock(answer = Answers.RETURNS_SELF)
    JctFlagBuilder flagBuilder;

    @Mock
    JavaCompiler jsr199Compiler;

    @Mock
    JctCompilation compilation;

    MockedConstruction<JctCompilationFactoryImpl> compilationFactoryConstructor;

    BiConsumer<JctCompilationFactoryImpl, Context> compilationFactoryMockConfigurer;

    @BeforeEach
    void setUp() {
      flags = someFlags();
      when(flagBuilder.build()).thenReturn(flags);
      compilationFactoryConstructor = mockConstruction(
          JctCompilationFactoryImpl.class,
          // Curry the call to allow other tests to override the behaviour in their "given" phase.
          (factory, compiler) -> compilationFactoryMockConfigurer.accept(factory, compiler)
      );

      when(flagBuilderFactory.createFlagBuilder()).thenReturn(flagBuilder);
      when(jsr199CompilerFactory.createCompiler()).thenReturn(jsr199Compiler);
      when(fileManagerFactory.createFileManager(any())).thenReturn(fileManager);

      // Default implementation. We can override this to make it throw exceptions, etc.
      compilationFactoryMockConfigurer =
          (factory, ctx) -> when(factory.createCompilation(any(), any(), any(), any()))
              .thenReturn(compilation);
    }

    @AfterEach
    void tearDown() {
      compilationFactoryConstructor.closeOnDemand();
    }

    abstract JctCompilation doCompile();

    @Nullable
    abstract Collection<String> classNames();

    @DisplayName(".compile(...) interacts with the internal factories as expected")
    @Test
    void compileInteractsWithInternalFactoriesAsExpected() {
      // When
      doCompile();

      // Then
      verify(fileManagerFactory).createFileManager(workspace);
      verifyNoMoreInteractions(fileManagerFactory);

      verify(flagBuilderFactory).createFlagBuilder();
      verifyNoMoreInteractions(flagBuilderFactory);

      verify(flagBuilder).build();

      verify(jsr199CompilerFactory).createCompiler();
      verifyNoMoreInteractions(jsr199CompilerFactory);

      assertThat(compilationFactoryConstructor.constructed()).hasSize(1);

      var compilationFactory = compilationFactoryConstructor.constructed().get(0);
      verify(compilationFactory)
          .createCompilation(flags, fileManager, jsr199Compiler, classNames());
      verifyNoMoreInteractions(compilationFactory);
    }

    @DisplayName(".compile(...) returns the expected compilation")
    @Test
    void compileReturnsTheExpectedCompilation() {
      // When
      var actualCompilation = doCompile();

      // Then
      assertThat(actualCompilation).isSameAs(compilation);
    }

    @DisplayName(".compile(...) propagates exceptions thrown by the compilation process")
    @Test
    void compilePropagatesExceptionsThrownByTheCompilationProcess() {
      // Given
      var expectedException = new JctCompilerException(
          "Compiler is broken, time to move to the forest and widdle spoons for a living",
          new IllegalStateException("This shouldn't be happening!")
      );

      compilationFactoryMockConfigurer =
          (factory, ctx) -> when(factory.createCompilation(any(), any(), any(), any()))
              .thenThrow(expectedException);

      // Then
      assertThatThrownBy(this::doCompile)
          .isSameAs(expectedException);
    }

    @DisplayName(".compile(...) rethrows filemanager closure exceptions")
    @Test
    void compileRethrowsFileManagerClosureExceptions() throws IOException {
      // Given
      var expectedException = new IOException("file manager do be heckin broken");
      doThrow(expectedException).when(fileManager).close();

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to close file manager. This is probably a bug, so please report it.")
          .hasCause(expectedException);
    }
  }

  @DisplayName("AbstractJctCompiler#compile(Workspace) tests")
  @Nested
  class CompileAllTest extends AbstractCompileTestTemplate {

    @Override
    JctCompilation doCompile() {
      return compiler.compile(workspace);
    }

    @Nullable
    @Override
    Collection<String> classNames() {
      return null;
    }
  }

  @DisplayName("AbstractJctCompiler#compile(Workspace, Collection<String>) tests")
  @Nested
  class CompileClassNamesTest extends AbstractCompileTestTemplate {

    @Mock
    Collection<String> classNames;

    @Override
    JctCompilation doCompile() {
      return compiler.compile(workspace, classNames);
    }

    @Override
    Collection<String> classNames() {
      return classNames;
    }
  }

  @DisplayName("AbstractJctCompiler#configure tests")
  @Nested
  class ConfigureTest {

    @DisplayName(".configure(...) raises a NullPointerException if the input is null")
    @Test
    @SuppressWarnings("ConstantConditions")
    void configureRaisesNullPointerExceptionOnNullInput() {
      // Then
      assertThatThrownBy(() -> compiler.configure(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("configurer");
    }

    @DisplayName(".configure(...) invokes the configurer on the compiler")
    @Test
    void configureInvokesConfigurerOnTheCompiler() throws Throwable {
      // Given
      var configurer = mockRaw(JctCompilerConfigurer.class)
          .<JctCompilerConfigurer<?>>upcastedTo()
          .build();

      // When
      compiler.configure(configurer);

      // Then
      verify(configurer).configure(compiler);
      verifyNoMoreInteractions(configurer);
    }

    @DisplayName(".configure(...) throws the given exception type")
    @ValueSource(classes = {
        RuntimeException.class,
        IOException.class,
        FileSystemException.class,
        NoSuchFileException.class,
        UnsupportedEncodingException.class,
        IndexOutOfBoundsException.class,
        SecurityException.class,
        ReflectiveOperationException.class,
        IllegalAccessException.class,
        NoSuchFieldException.class,
        NoSuchMethodException.class,
        IllegalArgumentException.class,
        IllegalThreadStateException.class,
        NoClassDefFoundError.class,
        ClassNotFoundException.class,
        ThreadDeath.class,
    })
    @ParameterizedTest(name = "for exception of type {0}")
    void configurePropagatesAnyException(Class<? extends Exception> exceptionCls) throws Throwable {
      // Given
      var configurer = mockRaw(JctCompilerConfigurer.class)
          .<JctCompilerConfigurer<?>>upcastedTo()
          .build();

      doThrow(exceptionCls).when(configurer).configure(any());

      // Then
      assertThatThrownBy(() -> compiler.configure(configurer))
          .isInstanceOf(exceptionCls);
    }

    @DisplayName(".configure(...) returns the compiler object")
    @Test
    void configureReturnsTheCompiler() throws Throwable {
      // Given
      var configurer = mockRaw(JctCompilerConfigurer.class)
          .<JctCompilerConfigurer<?>>upcastedTo()
          .build();

      // When
      var result = compiler.configure(configurer);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getName() returns the name")
  @Test
  void getNameReturnsName() {
    // Then
    assertThat(compiler.getName()).isEqualTo(name);
  }

  @DisplayName("AbstractJctCompiler#name tests")
  @Nested
  class NameTest {

    @DisplayName(".name(...) sets the name")
    @ValueSource(strings = {"foo", "bar baz", "bork"})
    @ParameterizedTest(name = "for name = {0}")
    void nameSetsTheName(String name) {
      // When
      compiler.name(name);

      // Then
      assertThatCompilerField("name").isEqualTo(name);
    }

    @DisplayName(".name(...) returns the compiler")
    @Test
    void nameReturnsTheCompiler() {
      // When
      var result = compiler.name("foo");

      // Then
      assertThat(result).isSameAs(compiler);
    }

    @DisplayName(".name(null) throws a NullPointerException")
    @SuppressWarnings("ConstantConditions")
    @Test
    void passingNullToNameThrowsNullPointerException() {
      // Then
      assertThatThrownBy(() -> compiler.name(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("name");
    }
  }

  @DisplayName(".isVerbose() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for verbose = {0}")
  void isVerboseReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("verbose", expected);

    // Then
    assertThat(compiler.isVerbose()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#verbose tests")
  @Nested
  class VerboseTests {

    @DisplayName(".verbose(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for verbose = {0}")
    void verboseSetsExpectedValue(boolean expected) {
      // When
      compiler.verbose(expected);

      // Then
      assertThatCompilerField("verbose").isEqualTo(expected);
    }

    @DisplayName(".verbose(...) returns the compiler")
    @Test
    void verboseReturnsTheCompiler() {
      // When
      var result = compiler.verbose(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isPreviewFeatures() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for previewFeatures = {0}")
  void isPreviewFeaturesReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("previewFeatures", expected);

    // Then
    assertThat(compiler.isPreviewFeatures()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#previewFeatures tests")
  @Nested
  class PreviewFeaturesTests {

    @DisplayName(".previewFeatures(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for previewFeatures = {0}")
    void previewFeaturesSetsExpectedValue(boolean expected) {
      // When
      compiler.previewFeatures(expected);

      // Then
      assertThatCompilerField("previewFeatures").isEqualTo(expected);
    }

    @DisplayName(".previewFeatures(...) returns the compiler")
    @Test
    void previewFeaturesReturnsTheCompiler() {
      // When
      var result = compiler.previewFeatures(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isShowWarnings() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for showWarnings = {0}")
  void isShowWarningsReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("showWarnings", expected);

    // Then
    assertThat(compiler.isShowWarnings()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#showWarnings tests")
  @Nested
  class ShowWarningsTests {

    @DisplayName(".showWarnings(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for showWarnings = {0}")
    void showWarningsSetsExpectedValue(boolean expected) {
      // When
      compiler.showWarnings(expected);

      // Then
      assertThatCompilerField("showWarnings").isEqualTo(expected);
    }

    @DisplayName(".showWarnings(...) returns the compiler")
    @Test
    void showWarningsReturnsTheCompiler() {
      // When
      var result = compiler.showWarnings(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isShowDeprecationWarnings() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for showDeprecationWarnings = {0}")
  void isShowDeprecationWarningsReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("showDeprecationWarnings", expected);

    // Then
    assertThat(compiler.isShowDeprecationWarnings()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#showDeprecationWarnings tests")
  @Nested
  class ShowDeprecationWarningsTests {

    @DisplayName(".showDeprecationWarnings(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for showDeprecationWarnings = {0}")
    void showDeprecationWarningsSetsExpectedValue(boolean expected) {
      // When
      compiler.showDeprecationWarnings(expected);

      // Then
      assertThatCompilerField("showDeprecationWarnings").isEqualTo(expected);
    }

    @DisplayName(".showDeprecationWarnings(...) returns the compiler")
    @Test
    void showDeprecationWarningsReturnsTheCompiler() {
      // When
      var result = compiler.showDeprecationWarnings(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isFailOnWarnings() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for failOnWarnings = {0}")
  void isFailOnWarningsReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("failOnWarnings", expected);

    // Then
    assertThat(compiler.isFailOnWarnings()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#failOnWarnings tests")
  @Nested
  class FailOnWarningsTests {

    @DisplayName(".failOnWarnings(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for failOnWarnings = {0}")
    void failOnWarningsSetsExpectedValue(boolean expected) {
      // When
      compiler.failOnWarnings(expected);

      // Then
      assertThatCompilerField("failOnWarnings").isEqualTo(expected);
    }

    @DisplayName(".failOnWarnings(...) returns the compiler")
    @Test
    void failOnWarningsReturnsTheCompiler() {
      // When
      var result = compiler.failOnWarnings(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getCompilationMode() returns the expected values")
  @EnumSource(CompilationMode.class)
  @ParameterizedTest(name = "for compilationMode = {0}")
  void getCompilationModeReturnsExpectedValue(CompilationMode expected) {
    // Given
    setFieldOnCompiler("compilationMode", expected);

    // Then
    assertThat(compiler.getCompilationMode()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#compilationMode tests")
  @Nested
  class CompilationModeTests {

    @DisplayName(".compilationMode(...) sets the expected values")
    @EnumSource(CompilationMode.class)
    @ParameterizedTest(name = "for compilationMode = {0}")
    void compilationModeSetsExpectedValue(CompilationMode expected) {
      // When
      compiler.compilationMode(expected);

      // Then
      assertThatCompilerField("compilationMode").isEqualTo(expected);
    }

    @DisplayName(".compilationMode(...) returns the compiler")
    @Test
    void compilationModeReturnsTheCompiler() {
      // When
      var result = compiler.compilationMode(CompilationMode.COMPILATION_AND_ANNOTATION_PROCESSING);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getAnnotationProcessorOptions returns a copy of the expected value")
  @Test
  void getAnnotationProcessorOptionsReturnsCopyOfTheExpectedValue() {
    // Given
    // Arrays#asList returns a mutable copy which is important as List#copyOf will not copy
    // immutable lists made from List#of, as an internal optimisation.
    var fieldValue = Arrays.asList("foo", "bar", "baz");
    setFieldOnCompiler("annotationProcessorOptions", fieldValue);

    // When
    var actual = compiler.getAnnotationProcessorOptions();

    // Then
    assertThat(actual)
        .isEqualTo(fieldValue)
        .isNotSameAs(fieldValue);
  }

  @DisplayName("AbstractJctCompiler#addAnnotationProcessorOptions tests")
  @Nested
  class AddAnnotationProcessorOptionsTest {

    @DisplayName(".addAnnotationProcessorOptions(...) adds the expected values")
    @Test
    void addAnnotationProcessorOptionsAddsTheExpectedValues() {
      // Given
      final var first = List.of("foo", "bar", "baz");
      final var second = List.of("baz", "bork", "qux", "quxx");
      final var third = List.of("eggs", "spam");
      final var joined = concat(first, second, third);

      // When
      compiler.addAnnotationProcessorOptions(first);
      compiler.addAnnotationProcessorOptions(second);
      compiler.addAnnotationProcessorOptions(third);

      // Then
      assertThatCompilerField("annotationProcessorOptions")
          .isEqualTo(joined);
    }

    @DisplayName(".addAnnotationProcessorOptions(...) returns the compiler")
    @Test
    void addAnnotationProcessorOptionsReturnsTheCompiler() {
      // When
      var result = compiler.addAnnotationProcessorOptions(List.of("foo", "bar"));

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getAnnotationProcessors returns a copy of the expected value")
  @Test
  void getAnnotationProcessorsReturnsCopyOfTheExpectedValue() {
    // Given
    // Arrays#asList returns a mutable copy which is important as List#copyOf will not copy
    // immutable lists made from List#of, as an internal optimisation.
    var first = mock(Processor.class);
    var second = mock(Processor.class);
    var third = mock(Processor.class);
    var fourth = mock(Processor.class);
    var fifth = mock(Processor.class);
    var sixth = mock(Processor.class);

    var fieldValue = Arrays.asList(first, second, third, fourth, fifth, sixth);
    setFieldOnCompiler("annotationProcessors", fieldValue);

    // When
    var actual = compiler.getAnnotationProcessors();

    // Then
    assertThat(actual)
        .isEqualTo(fieldValue)
        .isNotSameAs(fieldValue);
  }

  @DisplayName("AbstractJctCompiler#addAnnotationProcessors tests")
  @Nested
  class AddAnnotationProcessorsTest {

    @DisplayName(".addAnnotationProcessors(...) adds the expected values")
    @Test
    void addAnnotationProcessorsAddsTheExpectedValues() {
      // Given
      final var first = mock(Processor.class);
      final var second = mock(Processor.class);
      final var third = mock(Processor.class);
      final var fourth = mock(Processor.class);
      final var fifth = mock(Processor.class);
      final var sixth = mock(Processor.class);
      final var all = List.of(first, second, third, fourth, fifth, sixth);

      // When
      compiler.addAnnotationProcessors(List.of(first, second));
      compiler.addAnnotationProcessors(List.of(third, fourth, fifth));
      compiler.addAnnotationProcessors(List.of(sixth));

      // Then
      assertThatCompilerField("annotationProcessors")
          .isEqualTo(all);
    }

    @DisplayName(".addAnnotationProcessors(...) returns the compiler")
    @Test
    void addAnnotationProcessorsReturnsTheCompiler() {
      // When
      var result = compiler.addAnnotationProcessors(List.of(mock(Processor.class)));

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getCompilerOptions returns a copy of the expected value")
  @Test
  void getCompilerOptionsReturnsCopyOfTheExpectedValue() {
    // Given
    // Arrays#asList returns a mutable copy which is important as List#copyOf will not copy
    // immutable lists made from List#of, as an internal optimisation.
    var fieldValue = Arrays.asList("foo", "bar", "baz");
    setFieldOnCompiler("compilerOptions", fieldValue);

    // When
    var actual = compiler.getCompilerOptions();

    // Then
    assertThat(actual)
        .isEqualTo(fieldValue)
        .isNotSameAs(fieldValue);
  }

  @DisplayName("AbstractJctCompiler#addCompilerOptions tests")
  @Nested
  class AddCompilerOptionsTest {

    @DisplayName(".addCompilerOptions(...) adds the expected values")
    @Test
    void addCompilerOptionsAddsTheExpectedValues() {
      // Given
      final var first = List.of("foo", "bar", "baz");
      final var second = List.of("baz", "bork", "qux", "quxx");
      final var third = List.of("eggs", "spam");
      final var joined = concat(first, second, third);

      // When
      compiler.addCompilerOptions(first);
      compiler.addCompilerOptions(second);
      compiler.addCompilerOptions(third);

      // Then
      assertThatCompilerField("compilerOptions")
          .isEqualTo(joined);
    }

    @DisplayName(".addCompilerOptions(...) returns the compiler")
    @Test
    void addCompilerOptionsReturnsTheCompiler() {
      // When
      var result = compiler.addCompilerOptions(List.of("foo", "bar"));

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getEffectiveRelease() returns the expected values")
  @CsvSource({
      "10,   , 12, 10",
      "  , 11, 12, 11",
      "  ,   , 12, 12",
  })
  @ParameterizedTest(name = "for release = {0}, target = {1}, defaultRelease = {2}, expect = {3}")
  void getEffectiveReleaseReturnsExpectedValues(
      @Nullable String release,
      @Nullable String target,
      String defaultRelease,
      String expectedEffectiveRelease
  ) {
    // Given
    var compiler = new CompilerImpl("test", defaultRelease);
    compiler.release(release);
    compiler.target(target);

    // When
    var actualEffectiveRelease = compiler.getEffectiveRelease();

    // Then
    assertThat(actualEffectiveRelease).isEqualTo(expectedEffectiveRelease);
  }

  @DisplayName(".getRelease() returns the expected values")
  @NullAndEmptySource
  @ValueSource(strings = {"8", "9", "11", "17", "21"})
  @ParameterizedTest(name = "for release = {0}")
  void getReleaseReturnsTheExpectedValue(String expected) {
    // Given
    setFieldOnCompiler("release", expected);

    // Then
    assertThat(compiler.getRelease()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#release tests")
  @Nested
  class ReleaseTests {

    @DisplayName(".release(...) sets the expected values")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for release = {0}")
    void releaseSetsExpectedValue(String expected) {
      // When
      compiler.release(expected);

      // Then
      assertThatCompilerField("release").isEqualTo(expected);
    }

    @DisplayName(".release(...) clears the source field")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for source = {0}")
    void releaseClearsTheSource(String source) {
      // Given
      setFieldOnCompiler("source", source);

      // When
      compiler.release("15");

      // Then
      assertThatCompilerField("source").isNull();
    }

    @DisplayName(".release(...) clears the target field")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for target = {0}")
    void releaseClearsTheTarget(String target) {
      // Given
      setFieldOnCompiler("target", target);

      // When
      compiler.release("15");

      // Then
      assertThatCompilerField("target").isNull();
    }

    @DisplayName(".release(...) returns the compiler")
    @Test
    void releaseReturnsTheCompiler() {
      // When
      var result = compiler.release("1234");

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getSource() returns the expected values")
  @NullAndEmptySource
  @ValueSource(strings = {"8", "9", "11", "17", "21"})
  @ParameterizedTest(name = "for source = {0}")
  void getSourceReturnsTheExpectedValue(String expected) {
    // Given
    setFieldOnCompiler("source", expected);

    // Then
    assertThat(compiler.getSource()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#source tests")
  @Nested
  class SourceTests {

    @DisplayName(".source(...) sets the expected values")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for source = {0}")
    void sourceSetsExpectedValue(String expected) {
      // When
      compiler.source(expected);

      // Then
      assertThatCompilerField("source").isEqualTo(expected);
    }

    @DisplayName(".source(...) clears the release field")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for release = {0}")
    void sourceClearsTheSource(String release) {
      // Given
      setFieldOnCompiler("release", release);

      // When
      compiler.source("15");

      // Then
      assertThatCompilerField("release").isNull();
    }

    @DisplayName(".source(...) returns the compiler")
    @Test
    void sourceReturnsTheCompiler() {
      // When
      var result = compiler.source("1234");

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getTarget() returns the expected values")
  @NullAndEmptySource
  @ValueSource(strings = {"8", "9", "11", "17", "21"})
  @ParameterizedTest(name = "for target = {0}")
  void getTargetReturnsTheExpectedValue(String expected) {
    // Given
    setFieldOnCompiler("target", expected);

    // Then
    assertThat(compiler.getTarget()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#target tests")
  @Nested
  class TargetTests {

    @DisplayName(".target(...) sets the expected values")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for target = {0}")
    void targetSetsExpectedValue(String expected) {
      // When
      compiler.target(expected);

      // Then
      assertThatCompilerField("target").isEqualTo(expected);
    }

    @DisplayName(".target(...) clears the release field")
    @NullAndEmptySource
    @ValueSource(strings = {"8", "9", "11", "17", "21"})
    @ParameterizedTest(name = "for release = {0}")
    void targetClearsTheTarget(String release) {
      // Given
      setFieldOnCompiler("release", release);

      // When
      compiler.target("15");

      // Then
      assertThatCompilerField("release").isNull();
    }

    @DisplayName(".target(...) returns the compiler")
    @Test
    void targetReturnsTheCompiler() {
      // When
      var result = compiler.target("1234");

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isFixJvmModulePathMismatch() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for fixJvmModulePathMismatch = {0}")
  void isFixJvmModulePathMismatchReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("fixJvmModulePathMismatch", expected);

    // Then
    assertThat(compiler.isFixJvmModulePathMismatch()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#fixJvmModulePathMismatch tests")
  @Nested
  class FixJvmModulePathMismatchTests {

    @DisplayName(".fixJvmModulePathMismatch(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for fixJvmModulePathMismatch = {0}")
    void fixJvmModulePathMismatchSetsExpectedValue(boolean expected) {
      // When
      compiler.fixJvmModulePathMismatch(expected);

      // Then
      assertThatCompilerField("fixJvmModulePathMismatch").isEqualTo(expected);
    }

    @DisplayName(".fixJvmModulePathMismatch(...) returns the compiler")
    @Test
    void fixJvmModulePathMismatchReturnsTheCompiler() {
      // When
      var result = compiler.fixJvmModulePathMismatch(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isInheritClassPath() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for inheritClassPath = {0}")
  void isInheritClassPathReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("inheritClassPath", expected);

    // Then
    assertThat(compiler.isInheritClassPath()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#inheritClassPath tests")
  @Nested
  class InheritClassPathTests {

    @DisplayName(".inheritClassPath(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for inheritClassPath = {0}")
    void inheritClassPathSetsExpectedValue(boolean expected) {
      // When
      compiler.inheritClassPath(expected);

      // Then
      assertThatCompilerField("inheritClassPath").isEqualTo(expected);
    }

    @DisplayName(".inheritClassPath(...) returns the compiler")
    @Test
    void inheritClassPathReturnsTheCompiler() {
      // When
      var result = compiler.inheritClassPath(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isInheritModulePath() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for inheritModulePath = {0}")
  void isInheritModulePathReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("inheritModulePath", expected);

    // Then
    assertThat(compiler.isInheritModulePath()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#inheritModulePath tests")
  @Nested
  class InheritModulePathTests {

    @DisplayName(".inheritModulePath(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for inheritModulePath = {0}")
    void inheritModulePathSetsExpectedValue(boolean expected) {
      // When
      compiler.inheritModulePath(expected);

      // Then
      assertThatCompilerField("inheritModulePath").isEqualTo(expected);
    }

    @DisplayName(".inheritModulePath(...) returns the compiler")
    @Test
    void inheritModulePathReturnsTheCompiler() {
      // When
      var result = compiler.inheritModulePath(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isInheritPlatformClassPath() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for inheritPlatformClassPath = {0}")
  void isInheritPlatformClassPathReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("inheritPlatformClassPath", expected);

    // Then
    assertThat(compiler.isInheritPlatformClassPath()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#inheritPlatformClassPath tests")
  @Nested
  class InheritPlatformClassPathTests {

    @DisplayName(".inheritPlatformClassPath(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for inheritPlatformClassPath = {0}")
    void inheritPlatformClassPathSetsExpectedValue(boolean expected) {
      // When
      compiler.inheritPlatformClassPath(expected);

      // Then
      assertThatCompilerField("inheritPlatformClassPath").isEqualTo(expected);
    }

    @DisplayName(".inheritPlatformClassPath(...) returns the compiler")
    @Test
    void inheritPlatformClassPathReturnsTheCompiler() {
      // When
      var result = compiler.inheritPlatformClassPath(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".isInheritSystemModulePath() returns the expected values")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for inheritSystemModulePath = {0}")
  void isInheritSystemModulePathReturnsExpectedValue(boolean expected) {
    // Given
    setFieldOnCompiler("inheritSystemModulePath", expected);

    // Then
    assertThat(compiler.isInheritSystemModulePath()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#inheritSystemModulePath tests")
  @Nested
  class InheritSystemModulePathTests {

    @DisplayName(".inheritSystemModulePath(...) sets the expected values")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for inheritSystemModulePath = {0}")
    void inheritSystemModulePathSetsExpectedValue(boolean expected) {
      // When
      compiler.inheritSystemModulePath(expected);

      // Then
      assertThatCompilerField("inheritSystemModulePath").isEqualTo(expected);
    }

    @DisplayName(".inheritSystemModulePath(...) returns the compiler")
    @Test
    void inheritSystemModulePathReturnsTheCompiler() {
      // When
      var result = compiler.inheritSystemModulePath(true);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getLocale() returns the expected values")
  @MethodSource("locales")
  @ParameterizedTest(name = "for locale = {0}")
  void getLocaleReturnsExpectedValue(Locale expected) {
    // Given
    setFieldOnCompiler("locale", expected);

    // Then
    assertThat(compiler.getLocale()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#locale tests")
  @Nested
  class LocaleTests {

    @DisplayName(".locale(...) sets the expected values")
    @MethodSource("io.github.ascopes.jct.tests.unit.compilers.AbstractJctCompilerTest#locales")
    @ParameterizedTest(name = "for locale = {0}")
    void localeSetsExpectedValue(Locale expected) {
      // When
      compiler.locale(expected);

      // Then
      assertThatCompilerField("locale").isEqualTo(expected);
    }

    @DisplayName(".locale(...) throws a NullPointerException if the locale is null")
    @SuppressWarnings("ConstantConditions")
    @Test
    void localeThrowsNullPointerExceptionIfNull() {
      // Then
      assertThatThrownBy(() -> compiler.locale(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("locale");
    }

    @DisplayName(".locale(...) returns the compiler")
    @Test
    void localeReturnsTheCompiler() {
      // When
      var result = compiler.locale(Locale.ROOT);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getLogCharset() returns the expected values")
  @MethodSource("charsets")
  @ParameterizedTest(name = "for logCharset = {0}")
  void getLogCharsetReturnsExpectedValue(Charset expected) {
    // Given
    setFieldOnCompiler("logCharset", expected);

    // Then
    assertThat(compiler.getLogCharset()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#logCharset tests")
  @Nested
  class LogCharsetTests {

    @DisplayName(".logCharset(...) sets the expected values")
    @MethodSource("io.github.ascopes.jct.tests.unit.compilers.AbstractJctCompilerTest#charsets")
    @ParameterizedTest(name = "for logCharset = {0}")
    void logCharsetSetsExpectedValue(Charset expected) {
      // When
      compiler.logCharset(expected);

      // Then
      assertThatCompilerField("logCharset").isEqualTo(expected);
    }

    @DisplayName(".logCharset(...) throws a NullPointerException if the logCharset is null")
    @SuppressWarnings("ConstantConditions")
    @Test
    void logCharsetThrowsNullPointerExceptionIfNull() {
      // Then
      assertThatThrownBy(() -> compiler.logCharset(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("logCharset");
    }

    @DisplayName(".logCharset(...) returns the compiler")
    @Test
    void logCharsetReturnsTheCompiler() {
      // When
      var result = compiler.logCharset(Charset.defaultCharset());

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getFileManagerLoggingMode() returns the expected values")
  @EnumSource(LoggingMode.class)
  @ParameterizedTest(name = "for fileManagerLoggingMode = {0}")
  void getFileManagerLoggingModeReturnsExpectedValue(LoggingMode expected) {
    // Given
    setFieldOnCompiler("fileManagerLoggingMode", expected);

    // Then
    assertThat(compiler.getFileManagerLoggingMode()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#fileManagerLoggingMode tests")
  @Nested
  class FileManagerLoggingModeTests {

    @DisplayName(".fileManagerLoggingMode(...) sets the expected values")
    @EnumSource(LoggingMode.class)
    @ParameterizedTest(name = "for fileManagerLoggingMode = {0}")
    void fileManagerLoggingModeSetsExpectedValue(LoggingMode expected) {
      // When
      compiler.fileManagerLoggingMode(expected);

      // Then
      assertThatCompilerField("fileManagerLoggingMode").isEqualTo(expected);
    }

    @DisplayName(".fileManagerLoggingMode(...) throws a NullPointerException if "
        + "fileManagerLoggingMode is null")
    @SuppressWarnings("ConstantConditions")
    @Test
    void fileManagerLoggingModeThrowsNullPointerExceptionIfNull() {
      // Then
      assertThatThrownBy(() -> compiler.fileManagerLoggingMode(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fileManagerLoggingMode");
    }

    @DisplayName(".fileManagerLoggingMode(...) returns the compiler")
    @Test
    void fileManagerLoggingModeReturnsTheCompiler() {
      // When
      var result = compiler.fileManagerLoggingMode(LoggingMode.ENABLED);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getDiagnosticLoggingMode() returns the expected values")
  @EnumSource(LoggingMode.class)
  @ParameterizedTest(name = "for diagnosticLoggingMode = {0}")
  void getDiagnosticLoggingModeReturnsExpectedValue(LoggingMode expected) {
    // Given
    setFieldOnCompiler("diagnosticLoggingMode", expected);

    // Then
    assertThat(compiler.getDiagnosticLoggingMode()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#diagnosticLoggingMode tests")
  @Nested
  class DiagnosticLoggingModeTests {

    @DisplayName(".diagnosticLoggingMode(...) sets the expected values")
    @EnumSource(LoggingMode.class)
    @ParameterizedTest(name = "for diagnosticLoggingMode = {0}")
    void diagnosticLoggingModeSetsExpectedValue(LoggingMode expected) {
      // When
      compiler.diagnosticLoggingMode(expected);

      // Then
      assertThatCompilerField("diagnosticLoggingMode").isEqualTo(expected);
    }

    @DisplayName(".diagnosticLoggingMode(...) throws a NullPointerException "
        + "if diagnosticLoggingMode is null")
    @SuppressWarnings("ConstantConditions")
    @Test
    void diagnosticLoggingModeThrowsNullPointerExceptionIfNull() {
      // Then
      assertThatThrownBy(() -> compiler.diagnosticLoggingMode(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("diagnosticLoggingMode");
    }

    @DisplayName(".diagnosticLoggingMode(...) returns the compiler")
    @Test
    void diagnosticLoggingModeReturnsTheCompiler() {
      // When
      var result = compiler.diagnosticLoggingMode(LoggingMode.ENABLED);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".getAnnotationProcessorDiscovery() returns the expected values")
  @EnumSource(AnnotationProcessorDiscovery.class)
  @ParameterizedTest(name = "for annotationProcessorDiscovery = {0}")
  void getAnnotationProcessorDiscoveryReturnsExpectedValue(AnnotationProcessorDiscovery expected) {
    // Given
    setFieldOnCompiler("annotationProcessorDiscovery", expected);

    // Then
    assertThat(compiler.getAnnotationProcessorDiscovery()).isEqualTo(expected);
  }

  @DisplayName("AbstractJctCompiler#annotationProcessorDiscovery tests")
  @Nested
  class AnnotationProcessorDiscoveryTests {

    @DisplayName(".annotationProcessorDiscovery(...) sets the expected values")
    @EnumSource(AnnotationProcessorDiscovery.class)
    @ParameterizedTest(name = "for annotationProcessorDiscovery = {0}")
    void annotationProcessorDiscoverySetsExpectedValue(AnnotationProcessorDiscovery expected) {
      // When
      compiler.annotationProcessorDiscovery(expected);

      // Then
      assertThatCompilerField("annotationProcessorDiscovery").isEqualTo(expected);
    }

    @DisplayName(".annotationProcessorDiscovery(...) throws a NullPointerException "
        + "if annotationProcessorDiscovery is null")
    @SuppressWarnings("ConstantConditions")
    @Test
    void annotationProcessorDiscoveryThrowsNullPointerExceptionIfNull() {
      // Then
      assertThatThrownBy(() -> compiler.annotationProcessorDiscovery(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("annotationProcessorDiscovery");
    }

    @DisplayName(".annotationProcessorDiscovery(...) returns the compiler")
    @Test
    void annotationProcessorDiscoveryReturnsTheCompiler() {
      // When
      var result = compiler.annotationProcessorDiscovery(AnnotationProcessorDiscovery.ENABLED);

      // Then
      assertThat(result).isSameAs(compiler);
    }
  }

  @DisplayName(".toString() should return the name")
  @Test
  void toStringShouldReturnTheName() {
    // Then
    assertThat(compiler).hasToString(name);
  }

  @DisplayName(".getCompilationFactory() should build a new compilation factory impl")
  @Test
  void getCompilationFactoryBuildsCompilationFactoryImpl() {
    // Given
    try (var factoryCls = mockConstruction(JctCompilationFactoryImpl.class)) {
      // When
      var actualFactory = compiler.getCompilationFactory();

      // Then
      assertThat(factoryCls.constructed())
          .singleElement()
          // .satisfies to swap expected/actual
          .satisfies(expectedFactory -> assertThat(actualFactory).isSameAs(expectedFactory));
    }
  }

  @DisplayName(".buildFlags(...) applies the flags to the flag builder and builds them")
  @Test
  void buildFlagsBuildsTheExpectedFlags() {
    // Given
    var flagBuilder = mock(JctFlagBuilder.class, Answers.RETURNS_SELF);

    var annotationProcessorOptions = setFieldOnCompiler("annotationProcessorOptions", someFlags());
    var showDeprecationWarnings = setFieldOnCompiler("showDeprecationWarnings", someBoolean());
    var failOnWarnings = setFieldOnCompiler("failOnWarnings", someBoolean());
    var compilerOptions = setFieldOnCompiler("compilerOptions", someFlags());
    var previewFeatures = setFieldOnCompiler("previewFeatures", someBoolean());
    var release = setFieldOnCompiler("release", someRelease());
    var source = setFieldOnCompiler("source", someRelease());
    var target = setFieldOnCompiler("target", someRelease());
    var verbose = setFieldOnCompiler("verbose", someBoolean());
    var showWarnings = setFieldOnCompiler("showWarnings", someBoolean());

    var expectedFlags = someFlags();
    when(flagBuilder.build()).thenReturn(expectedFlags);

    // When
    var actualFlags = compiler.buildFlags(flagBuilder);

    // Then
    verify(flagBuilder).annotationProcessorOptions(same(annotationProcessorOptions));
    verify(flagBuilder).showDeprecationWarnings(eq(showDeprecationWarnings));
    verify(flagBuilder).failOnWarnings(eq(failOnWarnings));
    verify(flagBuilder).compilerOptions(same(compilerOptions));
    verify(flagBuilder).previewFeatures(eq(previewFeatures));
    verify(flagBuilder).release(eq(release));
    verify(flagBuilder).source(eq(source));
    verify(flagBuilder).target(eq(target));
    verify(flagBuilder).verbose(eq(verbose));
    verify(flagBuilder).showWarnings(eq(showWarnings));
    verify(flagBuilder).build();

    assertThat(actualFlags).isEqualTo(expectedFlags);
  }

  /////////////////////
  /// Param sources ///
  /////////////////////

  static Stream<Locale> locales() {
    return Stream.of(
        Locale.ROOT,
        Locale.UK,
        Locale.US,
        Locale.JAPAN,
        Locale.JAPANESE
    );
  }

  static Stream<Charset> charsets() {
    return Stream.of(
        Charset.defaultCharset(),
        StandardCharsets.UTF_8,
        StandardCharsets.US_ASCII,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.UTF_16BE
    );
  }

  ////////////////////////////////
  /// Helper methods and types ///
  ////////////////////////////////

  class CompilerImpl extends AbstractJctCompiler<CompilerImpl> {

    private final String defaultRelease;

    CompilerImpl(
        String name,
        String defaultRelease
    ) {
      super(name);
      this.defaultRelease = defaultRelease;
    }

    @Override
    public String getDefaultRelease() {
      return defaultRelease;
    }

    @Override
    public JctFlagBuilderFactory getFlagBuilderFactory() {
      return flagBuilderFactory;
    }

    @Override
    public Jsr199CompilerFactory getCompilerFactory() {
      return jsr199CompilerFactory;
    }

    @Override
    public JctFileManagerFactory getFileManagerFactory() {
      return fileManagerFactory;
    }

    @Override
    public List<String> buildFlags(JctFlagBuilder flagBuilder) {
      // Promote this method to be public for testing.
      return super.buildFlags(flagBuilder);
    }
  }

  @SafeVarargs
  final <T> List<T> concat(List<T> first, List<T>... more) {
    var newList = new ArrayList<>(first);
    for (var next : more) {
      newList.addAll(next);
    }
    return newList;
  }

  ///
  /// Reflection logic in tests is nasty, but this helps keep the tests for the constructor,
  /// accessors, and mutators separate.
  /// Since the AbstractJctCompiler is mostly just a fat POJO, I will allow it this time.
  ///

  AbstractObjectAssert<?, ?> assertThatCompilerField(String field) {
    try {
      var fieldObj = AbstractJctCompiler.class.getDeclaredField(field);
      fieldObj.setAccessible(true);
      var fieldValue = fieldObj.get(compiler);
      return assertThat(fieldValue)
          .as("CompilerImpl.%s (%s)", field, fieldValue);
    } catch (ReflectiveOperationException ex) {
      return fail("Failed to extract field " + field, ex);
    }
  }

  <T> T setFieldOnCompiler(String field, T value) {
    try {
      var fieldObj = AbstractJctCompiler.class.getDeclaredField(field);
      fieldObj.setAccessible(true);
      fieldObj.set(compiler, value);
      return value;
    } catch (ReflectiveOperationException ex) {
      return fail("Failed to set field " + field, ex);
    }
  }
}
