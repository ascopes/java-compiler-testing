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
package io.github.ascopes.jct.tests.unit.compilers.impl;

import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildDiagnosticListener;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildFileManager;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildFlags;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildWriter;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.compile;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureAnnotationProcessorDiscovery;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureAnnotationProcessorPaths;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureClassPath;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureJvmSystemModules;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureModulePath;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configurePlatformClassPath;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureRequiredLocations;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureWorkspacePaths;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.containsModules;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.determineRelease;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.findCompilationUnitLocations;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.findCompilationUnits;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.performCompilerPass;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someBoolean;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someCharset;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someCompilationUnits;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someFlags;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someIoException;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someJavaFileObject;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLinesOfText;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocale;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someModuleReference;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePath;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePathRoot;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelease;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someTraceDiagnostics;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someUncheckedException;
import static io.github.ascopes.jct.tests.helpers.GenericMock.mockRaw;
import static io.github.ascopes.jct.utils.IterableUtils.flatten;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.compilers.CompilationMode;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctFlagBuilder;
import io.github.ascopes.jct.compilers.impl.JctCompilationImpl;
import io.github.ascopes.jct.compilers.impl.JctJsr199Interop;
import io.github.ascopes.jct.diagnostics.TeeWriter;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingFileManagerProxy;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.io.IOException;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

/**
 * {@link JctJsr199Interop} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctJsr199Interop tests")
class JctJsr199InteropTest implements UtilityClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return JctJsr199Interop.class;
  }

  /////////////////////
  /// .compile(...) ///
  /////////////////////

  @DisplayName("JctJsr199Interop#compile tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class CompileTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MockedStatic<JctJsr199Interop> staticMock;

    @Mock
    Workspace workspace;

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    JavaCompiler jsr199Compiler;

    @Mock
    JctFlagBuilder flagBuilder;

    @BeforeEach
    void setUp() {
      staticMock.when(() -> compile(any(), any(), any(), any(), any()))
          .thenCallRealMethod();
    }

    JctCompilationImpl doCompile(@Nullable Collection<String> classNames) {
      return compile(workspace, compiler, jsr199Compiler, flagBuilder, classNames);
    }

    @DisplayName("the writer is built using the compiler")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    @SuppressWarnings("resource")
    void writerIsBuiltUsingCompiler(@Nullable Collection<String> classes) {
      // When
      doCompile(classes);

      // Then
      staticMock.verify(() -> buildWriter(compiler));
    }

    @DisplayName("errors while building writers get re-raised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    @SuppressWarnings("resource")
    void errorsBuildingWritersAreReraised(@Nullable Collection<String> classes) {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildWriter(any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("the writer is NOT closed after usage")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    @SuppressWarnings("resource")
    void writerIsNotClosedAfterUsage(@Nullable Collection<String> classes) throws IOException {
      // DO NOT CLOSE THE WRITER, IT IS ATTACHED TO SYSTEM.OUT.
      // Closing SYSTEM.OUT causes IntelliJ to abort the entire test runner.
      //    See https://youtrack.jetbrains.com/issue/IDEA-120628
      // Other platforms may see other weird behaviour if we do this (Surefire, for example).

      // Given
      var writer = mock(TeeWriter.class);
      staticMock.when(() -> buildWriter(any())).thenReturn(writer);

      // When
      doCompile(classes);

      // Then
      verify(writer, never()).close();
    }

    @DisplayName("the file manager is built using the compiler and workspace")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void fileManagerIsBuiltUsingCompilerAndWorkspace(@Nullable Collection<String> classes) {
      // When
      doCompile(classes);

      // Then
      staticMock.verify(() -> buildFileManager(compiler, workspace));
    }

    @DisplayName("errors while building file managers get re-raised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void errorsBuildingFileManagersAreReraised(@Nullable Collection<String> classes) {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("the file manager is closed after usage")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void fileManagerIsClosedAfterUsage(@Nullable Collection<String> classes) {
      // Given
      var fileManager = mock(JctFileManagerImpl.class);
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenReturn(fileManager);

      // When
      doCompile(classes);

      // Then
      verify(fileManager).close();
    }

    @DisplayName("the flags are built using the compiler and flag builder")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void flagsAreBuiltUsingCompilerAndFlagBuilder(@Nullable Collection<String> classes) {
      // When
      doCompile(classes);

      // Then
      staticMock.verify(() -> buildFlags(compiler, flagBuilder));
    }

    @DisplayName("errors while building flags get re-raised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void errorsBuildingFlagsAreReraised(@Nullable Collection<String> classes) {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildFlags(any(), any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("the diagnostic listener is built using the compiler")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void diagnosticListenerIsBuiltUsingCompiler(@Nullable Collection<String> classes) {
      // When
      doCompile(classes);

      // Then
      staticMock.verify(() -> buildDiagnosticListener(compiler));
    }

    @DisplayName("errors while building diagnostic listeners get re-raised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void errorsBuildingDiagnosticListenersAreReraised(@Nullable Collection<String> classes) {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildDiagnosticListener(any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("compilation units are discovered using the file manager")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void compilationUnitsAreDiscoveredUsingTheFileManager(@Nullable Collection<String> classes) {
      // Given
      var fileManager = mock(JctFileManagerImpl.class);
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenReturn(fileManager);

      // When
      doCompile(classes);

      // Then
      staticMock.verify(() -> findCompilationUnits(fileManager));
    }

    @DisplayName("errors finding compilation units are reraised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void errorsFindingCompilationUnitsAreReraised(@Nullable Collection<String> classes) {
      // Given
      var ex = someIoException();
      staticMock.when(() -> findCompilationUnits(any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("performCompilerPass is called with the expected arguments")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    @SuppressWarnings("resource")
    void performCompilerPassCalledWithExpectedArguments(@Nullable Collection<String> classes) {
      // Given
      var flags = someFlags();
      staticMock.when(() -> buildFlags(any(), any()))
          .thenReturn(flags);

      var diagnosticListener = mockRaw(TracingDiagnosticListener.class)
          .<TracingDiagnosticListener<JavaFileObject>>upcastedTo()
          .build();
      staticMock.when(() -> buildDiagnosticListener(any()))
          .thenReturn(diagnosticListener);

      var writer = mock(TeeWriter.class);
      staticMock.when(() -> buildWriter(any()))
          .thenReturn(writer);

      var fileManager = mock(JctFileManagerImpl.class);
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenReturn(fileManager);

      var compilationUnits = someCompilationUnits();
      staticMock.when(() -> findCompilationUnits(any()))
          .thenReturn(compilationUnits);

      // When
      doCompile(classes);

      // Then
      staticMock.verify(() -> performCompilerPass(
          compiler,
          jsr199Compiler,
          writer,
          flags,
          fileManager,
          diagnosticListener,
          compilationUnits,
          classes
      ));
    }

    @DisplayName("errors performing the compilation pass units are reraised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void errorsPerformingTheCompilationPassAreReraised(@Nullable Collection<String> classes) {
      // Given
      var ex = someUncheckedException();
      staticMock
          .when(() -> performCompilerPass(any(), any(), any(), any(), any(), any(), any(), any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("compilation results are returned")
    @SuppressWarnings("resource")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for a compilation returning {0}")
    void compilationResultsAreReturned(boolean result) {
      // Given
      var failOnWarnings = someBoolean();
      when(compiler.isFailOnWarnings()).thenReturn(failOnWarnings);

      var flags = someFlags();
      staticMock.when(() -> buildFlags(any(), any()))
          .thenReturn(flags);

      var diagnosticListener = mockRaw(TracingDiagnosticListener.class)
          .<TracingDiagnosticListener<JavaFileObject>>upcastedTo()
          .build();
      staticMock.when(() -> buildDiagnosticListener(any()))
          .thenReturn(diagnosticListener);

      var diagnostics = someTraceDiagnostics();
      when(diagnosticListener.getDiagnostics())
          .thenReturn(diagnostics);

      var writer = mock(TeeWriter.class);
      staticMock.when(() -> buildWriter(any()))
          .thenReturn(writer);

      var outputLines = someLinesOfText();
      when(writer.toString())
          .thenReturn(outputLines);

      var fileManager = mock(JctFileManagerImpl.class);
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenReturn(fileManager);

      var compilationUnits = someCompilationUnits();
      staticMock.when(() -> findCompilationUnits(any()))
          .thenReturn(compilationUnits);

      staticMock
          .when(() -> performCompilerPass(any(), any(), any(), any(), any(), any(), any(), any()))
          .thenReturn(result);

      // When
      var compilation = doCompile(null);

      // Then
      assertThat(compilation)
          .as("compilation")
          .isNotNull();

      assertSoftly(softly -> {
        softly.assertThat(compilation.getCompilationUnits())
            .as(".compilationUnits")
            .isInstanceOf(Set.class)
            .containsExactlyInAnyOrderElementsOf(compilationUnits);

        softly.assertThat(compilation.getDiagnostics())
            .as(".diagnostics")
            .containsExactlyElementsOf(diagnostics);

        softly.assertThat(compilation.getFileManager())
            .as(".fileManager")
            .isSameAs(fileManager);

        softly.assertThat(compilation.getOutputLines())
            .as(".outputLines")
            .containsExactly(outputLines.split("\n"));

        softly.assertThat(compilation.isSuccessful())
            .as(".successful")
            .isEqualTo(result);

        softly.assertThat(compilation.isFailOnWarnings())
            .as(".failOnWarnings")
            .isEqualTo(failOnWarnings);
      });
    }

    @SuppressWarnings("resource")
    @DisplayName("errors extracting the writer lines are reraised")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void errorsExtractingWriterLinesAreReraised(@Nullable Collection<String> classes) {
      // Given
      var writer = mock(TeeWriter.class);
      staticMock.when(() -> buildWriter(any()))
          .thenReturn(writer);

      var ex = someUncheckedException();
      when(writer.toString())
          .thenThrow(ex);

      // Then
      assertThatThrownBy(() -> doCompile(classes))
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }
  }

  /////////////////////////
  /// .buildWriter(...) ///
  /////////////////////////

  @DisplayName("JctJsr199Interop#buildWriter tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class BuildWriterTest {

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    MockedStatic<TeeWriter> teeWriterCls;

    TeeWriter doBuildWriter() {
      return buildWriter(compiler);
    }

    @DisplayName(".buildWriter(...) initialises the writer and returns it")
    @Test
    void buildWriterInitialisesWriter() {
      // Given
      var charset = someCharset();
      when(compiler.getLogCharset()).thenReturn(charset);

      var expectedWriter = mock(TeeWriter.class);
      teeWriterCls.when(() -> TeeWriter.wrap(any(), any()))
          .thenReturn(expectedWriter);

      // When
      var actualWriter = doBuildWriter();

      // Then
      teeWriterCls.verify(() -> TeeWriter.wrap(charset, System.out));
      teeWriterCls.verifyNoMoreInteractions();
      assertThat(actualWriter).isSameAs(expectedWriter);
    }
  }

  ////////////////////////
  /// .buildFlags(...) ///
  ////////////////////////

  @DisplayName("JctJsr199Interop#buildFlags tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class BuildFlagsTest {

    List<String> expectedFlags;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    JctCompiler<?, ?> jctCompiler;

    @Mock(answer = Answers.RETURNS_SELF)
    JctFlagBuilder flagBuilder;

    @BeforeEach
    void setUp() {
      expectedFlags = someFlags();

      when(flagBuilder.build())
          .thenReturn(expectedFlags);
    }

    List<String> doBuildFlags() {
      return buildFlags(jctCompiler, flagBuilder);
    }

    @DisplayName(".buildFlags(...) builds the flags and returns them")
    @Test
    void buildFlagsBuildsTheFlags() {
      // When
      final var actualFlags = doBuildFlags();

      // Then
      verify(flagBuilder)
          .annotationProcessorOptions(jctCompiler.getAnnotationProcessorOptions());
      verify(flagBuilder)
          .showDeprecationWarnings(jctCompiler.isShowDeprecationWarnings());
      verify(flagBuilder)
          .failOnWarnings(jctCompiler.isFailOnWarnings());
      verify(flagBuilder)
          .compilerOptions(jctCompiler.getCompilerOptions());
      verify(flagBuilder)
          .previewFeatures(jctCompiler.isPreviewFeatures());
      verify(flagBuilder)
          .release(jctCompiler.getRelease());
      verify(flagBuilder)
          .source(jctCompiler.getSource());
      verify(flagBuilder)
          .target(jctCompiler.getTarget());
      verify(flagBuilder)
          .verbose(jctCompiler.isVerbose());
      verify(flagBuilder)
          .showWarnings(jctCompiler.isShowWarnings());
      verify(flagBuilder)
          .build();
      verifyNoMoreInteractions(flagBuilder);

      assertThat(actualFlags).isSameAs(expectedFlags);
    }
  }

  /////////////////////////
  /// .buildFileManager ///
  /////////////////////////

  @DisplayName("JctJsr199Interop#buildFileManager tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  @SuppressWarnings("resource")
  class BuildFileManagerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MockedStatic<JctJsr199Interop> staticMock;

    @Mock
    MockedStatic<JctFileManagerImpl> fileManagerStaticMock;

    @Mock
    MockedStatic<LoggingFileManagerProxy> proxyStaticMock;

    @Mock
    JctFileManagerImpl fileManager;

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    Workspace workspace;

    @BeforeEach
    void setUp() {
      staticMock.when(() -> JctJsr199Interop.buildFileManager(any(), any()))
          .thenCallRealMethod();
      fileManagerStaticMock.when(() -> JctFileManagerImpl.forRelease(any()))
          .thenReturn(fileManager);
      when(compiler.getFileManagerLoggingMode())
          .thenReturn(LoggingMode.DISABLED);
    }

    JctFileManager doBuild() {
      return buildFileManager(compiler, workspace);
    }

    @DisplayName("the release is determined using the compiler")
    @Test
    void releaseIsDeterminedUsingCompiler() {
      // When
      doBuild();

      // Then
      staticMock.verify(() -> determineRelease(compiler));
    }

    @DisplayName("the file manager is built using the determined release")
    @Test
    void fileManagerIsBuiltForTheDeterminedRelease() {
      // Given
      var release = someRelease();
      staticMock.when(() -> determineRelease(any()))
          .thenReturn(release);

      // When
      doBuild();

      // Then
      fileManagerStaticMock.verify(() -> JctFileManagerImpl.forRelease(release));
    }

    @DisplayName("file manager paths are configured in the correct order")
    @Test
    void fileManagerPathsAreConfiguredInTheCorrectOrder() {
      // Given
      var order = inOrder(JctJsr199Interop.class);

      // When
      doBuild();

      // Then
      order.verify(staticMock, () -> configureWorkspacePaths(workspace, fileManager));
      order.verify(staticMock, () -> configureClassPath(compiler, fileManager));
      order.verify(staticMock, () -> configureModulePath(compiler, fileManager));
      order.verify(staticMock, () -> configurePlatformClassPath(compiler, fileManager));
      order.verify(staticMock, () -> configureJvmSystemModules(compiler, fileManager));
      order.verify(staticMock, () -> configureAnnotationProcessorPaths(compiler, fileManager));
      order.verify(staticMock, () -> configureRequiredLocations(workspace, fileManager));
      order.verifyNoMoreInteractions();
    }

    @DisplayName("no proxy is used when file manager logging is disabled")
    @Test
    void noProxyIsUsedWhenFileManagerLoggingIsDisabled() {
      // Given
      when(compiler.getFileManagerLoggingMode()).thenReturn(LoggingMode.DISABLED);

      // When
      var actualFileManager = doBuild();

      // Then
      assertThat(actualFileManager).isSameAs(fileManager);
      proxyStaticMock.verifyNoInteractions();
    }

    @DisplayName("a proxy is used when file manager logging is enabled")
    @CsvSource({
        "ENABLED, false",
        "STACKTRACES, true",
    })
    @ParameterizedTest(name = "enable stacktraces = {1} when LoggingMode = {0}")
    void proxyIsUsedWhenFileManagerLoggingIsEnabled(
        LoggingMode loggingMode,
        boolean enableStacktraces
    ) {
      // Given
      when(compiler.getFileManagerLoggingMode()).thenReturn(loggingMode);
      var proxyFileManger = mock(JctFileManager.class);
      proxyStaticMock.when(() -> LoggingFileManagerProxy.wrap(any(), anyBoolean()))
          .thenReturn(proxyFileManger);

      // When
      var actualFileManager = doBuild();

      // Then
      proxyStaticMock.verify(() -> LoggingFileManagerProxy.wrap(fileManager, enableStacktraces));
      proxyStaticMock.verifyNoMoreInteractions();

      assertThat(actualFileManager)
          .isSameAs(proxyFileManger)
          .isNotSameAs(fileManager);
    }
  }

  /////////////////////////
  /// .determineRelease ///
  /////////////////////////

  @DisplayName("JctJsr199Interop#determineRelease tests")
  @Nested
  class DetermineReleaseTest {

    @DisplayName("The correct release should be determined")
    @CsvSource({
        // Preferring release
        "12,   ,   , 11, 12",
        "12,   ,   , 13, 12",
        "12,   ,   , 11, 12",
        "12, 14,   , 11, 12",
        "12, 13,   , 13, 12",
        "12, 10,   , 11, 12",
        "12,   ,  8, 11, 12",
        "12,   ,  9, 13, 12",
        "12,   , 10, 11, 12",
        "12, 14, 11, 11, 12",
        "12, 13, 13, 13, 12",
        "12, 10, 14, 11, 12",
        // Preferring target
        "  ,   ,  8, 11,  8",
        "  ,   ,  9, 13,  9",
        "  ,   , 10, 11, 10",
        "  , 14, 11, 10, 11",
        "  , 13, 13, 13, 13",
        "  , 10, 14, 11, 14",
        // Preferring default release
        "  ,   ,   , 13, 13",
        "  ,   ,   , 11, 11",
        "  , 14,   , 10, 10",
        "  , 13,   , 17, 17",
    })
    @ParameterizedTest(
        name = "for release = {0}, source = {1}, target = {2}, defaultRelease = {3}, expect {4}"
    )
    void theCorrectReleaseShouldBeDetermined(
        String release,
        String source,
        String target,
        String defaultRelease,
        String expectedRelease
    ) {
      // Given
      var compiler = mockRaw(JctCompiler.class)
          .<JctCompiler<?, ?>>upcastedTo()
          .build(withSettings().strictness(Strictness.LENIENT));
      when(compiler.getRelease()).thenReturn(release);
      when(compiler.getSource()).thenReturn(source);
      when(compiler.getTarget()).thenReturn(target);
      when(compiler.getDefaultRelease()).thenReturn(defaultRelease);

      // Then
      assertThat(determineRelease(compiler))
          .isEqualTo(expectedRelease);
    }
  }

  ////////////////////////////////
  /// .configureWorkspacePaths ///
  ////////////////////////////////

  @DisplayName("JctJsr199Interop#configureWorkspacePaths tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureWorkspacePathsTest {

    @Mock
    Workspace workspace;

    @Mock
    JctFileManagerImpl fileManager;

    void doConfigureWorkspacePaths() {
      configureWorkspacePaths(workspace, fileManager);
    }

    @DisplayName("all paths should be added to the file manager")
    @Test
    void allPathsShouldBeAddedToTheFileManager() {
      // Given
      var paths = Map.<Location, List<? extends PathRoot>>of(
          someLocation(), List.of(somePathRoot()),
          someLocation(), List.of(somePathRoot(), somePathRoot()),
          someLocation(), List.of(somePathRoot(), somePathRoot(), somePathRoot()),
          someLocation(), List.of(somePathRoot())
      );
      when(workspace.getAllPaths()).thenReturn(paths);

      // When
      doConfigureWorkspacePaths();

      // Then
      paths.forEach((location, roots) -> verify(fileManager).addPaths(location, roots));
    }
  }

  ///////////////////////////
  /// .configureClassPath ///
  ///////////////////////////

  @DisplayName("JctJsr199Interop#configureClassPath tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureClassPathTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    MockedStatic<JctJsr199Interop> staticMock;

    @Mock
    MockedStatic<SpecialLocationUtils> specialLocationUtils;

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    JctFileManagerImpl fileManager;

    MockedConstruction<WrappingDirectoryImpl> wrappingDirectory;

    @BeforeEach
    void setUp() {
      staticMock.when(() -> configureClassPath(any(), any()))
          .thenCallRealMethod();

      wrappingDirectory = mockConstruction(
          WrappingDirectoryImpl.class,
          (obj, ctx) -> when(obj.getPath()).thenReturn((Path) ctx.arguments().get(0))
      );
    }

    @AfterEach
    void tearDown() {
      wrappingDirectory.closeOnDemand();
    }

    void doConfigureClassPath() {
      configureClassPath(compiler, fileManager);
    }

    @DisplayName("Nothing is configured if classpath inheritance is disabled")
    @Test
    void nothingIsConfiguredIfClasspathInheritanceIsDisabled() {
      // Given
      when(compiler.isInheritClassPath()).thenReturn(false);

      // When
      doConfigureClassPath();

      // Then
      verifyNoInteractions(fileManager);
    }

    @DisplayName("Paths are registered when module path mismatch fixing is disabled")
    @Test
    void pathsAreRegisteredWhenModulePathMismatchFixingIsDisabled() {
      // Given
      var paths = Stream
          .generate(Fixtures::somePath)
          .limit(5)
          .collect(Collectors.toList());
      specialLocationUtils.when(SpecialLocationUtils::currentClassPathLocations)
          .thenReturn(paths);

      when(compiler.isInheritClassPath())
          .thenReturn(true);
      when(compiler.isFixJvmModulePathMismatch())
          .thenReturn(false);

      // When
      doConfigureClassPath();

      // Then
      var captor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);
      verify(fileManager, times(5))
          .addPath(same(StandardLocation.CLASS_PATH), captor.capture());
      assertThat(captor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(paths));
      verifyNoMoreInteractions(fileManager);
    }

    @DisplayName("Paths are registered when module path mismatch fixing is enabled")
    @Test
    void pathsAreRegisteredWhenModulePathMismatchFixingIsEnabled() {
      // Given
      var modulePaths = Stream
          .generate(Fixtures::somePath)
          .limit(3)
          .collect(Collectors.toList());

      var classPaths = Stream
          .generate(Fixtures::somePath)
          .limit(5)
          .collect(Collectors.toList());

      var paths = Stream.concat(modulePaths.stream(), classPaths.stream())
          .collect(Collectors.toList());

      specialLocationUtils.when(SpecialLocationUtils::currentClassPathLocations)
          .thenReturn(paths);

      when(compiler.isInheritClassPath())
          .thenReturn(true);
      when(compiler.isFixJvmModulePathMismatch())
          .thenReturn(true);

      classPaths.forEach(path -> staticMock.when(() -> containsModules(path)).thenReturn(false));
      modulePaths.forEach(path -> staticMock.when(() -> containsModules(path)).thenReturn(true));

      // When
      doConfigureClassPath();

      // Then
      var classPathCaptor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);
      verify(fileManager, times(8))
          .addPath(same(StandardLocation.CLASS_PATH), classPathCaptor.capture());
      assertThat(classPathCaptor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(paths));

      var modulePathCaptor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);
      verify(fileManager, times(3))
          .addPath(same(StandardLocation.MODULE_PATH), classPathCaptor.capture());
      assertThat(modulePathCaptor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(modulePaths));

      verifyNoMoreInteractions(fileManager);
    }
  }

  ////////////////////////
  /// .containsModules ///
  ////////////////////////

  @DisplayName("JctJsr199Interop#containsModules tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ContainsModulesTest {

    @Mock
    MockedStatic<ModuleFinder> moduleFinderStaticMock;

    @Mock
    ModuleFinder moduleFinder;

    @BeforeEach
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void setUp() {
      moduleFinderStaticMock.when(() -> ModuleFinder.of(any())).thenReturn(moduleFinder);
    }

    @DisplayName("ModuleFinder is initialised using the given path")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void moduleFinderIsInitialisedUsingTheGivenPath() {
      // Given
      var path = somePath();

      // When
      containsModules(path);

      // Then
      moduleFinderStaticMock.verify(() -> ModuleFinder.of(path));
      moduleFinderStaticMock.verifyNoMoreInteractions();
    }

    @DisplayName("Expect true when modules exist")
    @Test
    void expectTrueWhenModulesExist() {
      // Given
      when(moduleFinder.findAll()).thenReturn(Set.of(
          someModuleReference(),
          someModuleReference(),
          someModuleReference()
      ));

      // When
      var result = containsModules(somePath());

      // Then
      assertThat(result).isTrue();
    }

    @DisplayName("Expect false when modules do not exist")
    @Test
    void expectFalseWhenModulesDoNotExist() {
      // Given
      when(moduleFinder.findAll()).thenReturn(Set.of());

      // When
      var result = containsModules(somePath());

      // Then
      assertThat(result).isFalse();
    }

    @DisplayName("Expect false when errors resolving modules occur")
    @Test
    void expectFalseWhenErrorsResolvingModulesOccur() {
      // Given
      when(moduleFinder.findAll()).thenThrow(FindException.class);

      // When
      var result = containsModules(somePath());

      // Then
      assertThat(result).isFalse();
    }
  }

  ////////////////////////////
  /// .configureModulePath ///
  ////////////////////////////

  @DisplayName("JctJsr199Interop#configureModulePath tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureModulePathTest {

    @Mock
    MockedStatic<SpecialLocationUtils> specialLocationUtils;

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    JctFileManagerImpl fileManager;

    MockedConstruction<WrappingDirectoryImpl> wrappingDirectory;

    @BeforeEach
    void setUp() {
      wrappingDirectory = mockConstruction(
          WrappingDirectoryImpl.class,
          (obj, ctx) -> when(obj.getPath()).thenReturn((Path) ctx.arguments().get(0))
      );
    }

    @AfterEach
    void tearDown() {
      wrappingDirectory.closeOnDemand();
    }

    void doConfigureModulePath() {
      configureModulePath(compiler, fileManager);
    }

    @DisplayName("Nothing is configured if module path inheritance is disabled")
    @Test
    void nothingIsConfiguredIfModulePathInheritanceIsDisabled() {
      // Given
      when(compiler.isInheritModulePath()).thenReturn(false);

      // When
      doConfigureModulePath();

      // Then
      verifyNoInteractions(fileManager);
    }

    @DisplayName("Paths are registered")
    @Test
    void pathsAreRegistered() {
      // Given
      var paths = Stream
          .generate(Fixtures::somePath)
          .limit(5)
          .collect(Collectors.toList());

      specialLocationUtils.when(SpecialLocationUtils::currentModulePathLocations)
          .thenReturn(paths);

      when(compiler.isInheritModulePath())
          .thenReturn(true);

      // When
      doConfigureModulePath();

      // Then
      var modulePathCaptor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);
      var classPathCaptor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);

      verify(fileManager, times(5))
          .addPath(same(StandardLocation.MODULE_PATH), modulePathCaptor.capture());
      verify(fileManager, times(5))
          .addPath(same(StandardLocation.CLASS_PATH), classPathCaptor.capture());
      assertThat(modulePathCaptor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(paths));
      assertThat(classPathCaptor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(paths));
      verifyNoMoreInteractions(fileManager);
    }
  }

  ///////////////////////////////////
  /// .configurePlatformClassPath ///
  ///////////////////////////////////

  @DisplayName("JctJsr199Interop#configurePlatformClassPath tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigurePlatformClassPathTest {

    @Mock
    MockedStatic<SpecialLocationUtils> specialLocationUtils;

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    JctFileManagerImpl fileManager;

    MockedConstruction<WrappingDirectoryImpl> wrappingDirectory;

    @BeforeEach
    void setUp() {
      wrappingDirectory = mockConstruction(
          WrappingDirectoryImpl.class,
          (obj, ctx) -> when(obj.getPath()).thenReturn((Path) ctx.arguments().get(0))
      );
    }

    @AfterEach
    void tearDown() {
      wrappingDirectory.closeOnDemand();
    }

    void doConfigurePlatformClassPath() {
      configurePlatformClassPath(compiler, fileManager);
    }

    @DisplayName("Nothing is configured if platform classpath inheritance is disabled")
    @Test
    void nothingIsConfiguredIfPlatformClasspathInheritanceIsDisabled() {
      // Given
      when(compiler.isInheritPlatformClassPath()).thenReturn(false);

      // When
      doConfigurePlatformClassPath();

      // Then
      verifyNoInteractions(fileManager);
    }

    @DisplayName("Paths are registered")
    @Test
    void pathsAreRegistered() {
      // Given
      var paths = Stream
          .generate(Fixtures::somePath)
          .limit(5)
          .collect(Collectors.toList());

      specialLocationUtils.when(SpecialLocationUtils::currentPlatformClassPathLocations)
          .thenReturn(paths);

      when(compiler.isInheritPlatformClassPath())
          .thenReturn(true);

      // When
      doConfigurePlatformClassPath();

      // Then
      var captor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);

      verify(fileManager, times(5))
          .addPath(same(StandardLocation.PLATFORM_CLASS_PATH), captor.capture());
      assertThat(captor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(paths));
      verifyNoMoreInteractions(fileManager);
    }
  }

  //////////////////////////////////
  /// .configureJvmSystemModules ///
  //////////////////////////////////

  @DisplayName("JctJsr199Interop#configureJvmSystemModules tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureJvmSystemModulesTest {

    @Mock
    MockedStatic<SpecialLocationUtils> specialLocationUtils;

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    JctFileManagerImpl fileManager;

    MockedConstruction<WrappingDirectoryImpl> wrappingDirectory;

    @BeforeEach
    void setUp() {
      wrappingDirectory = mockConstruction(
          WrappingDirectoryImpl.class,
          (obj, ctx) -> when(obj.getPath()).thenReturn((Path) ctx.arguments().get(0))
      );
    }

    @AfterEach
    void tearDown() {
      wrappingDirectory.closeOnDemand();
    }

    void doConfigureJvmSystemModules() {
      configureJvmSystemModules(compiler, fileManager);
    }

    @DisplayName("Nothing is configured if system module inheritance is disabled")
    @Test
    void nothingIsConfiguredIfSystemModuleInheritanceIsDisabled() {
      // Given
      when(compiler.isInheritSystemModulePath()).thenReturn(false);

      // When
      doConfigureJvmSystemModules();

      // Then
      verifyNoInteractions(fileManager);
    }

    @DisplayName("Paths are registered")
    @Test
    void pathsAreRegistered() {
      // Given
      var paths = Stream
          .generate(Fixtures::somePath)
          .limit(5)
          .collect(Collectors.toList());

      specialLocationUtils.when(SpecialLocationUtils::javaRuntimeLocations)
          .thenReturn(paths);

      when(compiler.isInheritSystemModulePath())
          .thenReturn(true);

      // When
      doConfigureJvmSystemModules();

      // Then
      var captor = ArgumentCaptor.forClass(WrappingDirectoryImpl.class);

      verify(fileManager, times(5))
          .addPath(same(StandardLocation.SYSTEM_MODULES), captor.capture());
      assertThat(captor.getAllValues())
          .allSatisfy(pathRoot -> assertThat(pathRoot.getPath()).isIn(paths));
      verifyNoMoreInteractions(fileManager);
    }
  }

  //////////////////////////////////////////
  /// .configureAnnotationProcessorPaths ///
  //////////////////////////////////////////

  @DisplayName("JctJsr199Interop#configureAnnotationProcessorPaths tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureAnnotationProcessorPathsTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    JctCompiler<?, ?> compiler;

    @Mock
    JctFileManagerImpl fileManager;

    void doConfigureAnnotationProcessorPaths() {
      configureAnnotationProcessorPaths(compiler, fileManager);
    }

    @DisplayName("Ensure no containers are copied if annotation processing is disabled")
    @EnumSource(AnnotationProcessorDiscovery.class)
    @ParameterizedTest(name = "for discovery mode = {0}")
    void ensureNoOperationsWhenAnnotationProcessingDisabled(AnnotationProcessorDiscovery discovery) {
      // Given
      when(compiler.getCompilationMode())
          .thenReturn(CompilationMode.COMPILATION_ONLY);
      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(discovery);

      // When
      doConfigureAnnotationProcessorPaths();

      // Then
      verify(compiler).getCompilationMode();
      verifyNoMoreInteractions(compiler);
      verifyNoInteractions(fileManager);
    }

    @DisplayName("Ensure containers copied when AP discovery included with dependencies")
    @EnumSource(value = CompilationMode.class, names = "COMPILATION_ONLY", mode = Mode.EXCLUDE)
    @ParameterizedTest(name = "for compilation mode = {0}")
    void ensureContainersAreCopiedWhenApDiscoveryIncludedWithDependencies(CompilationMode mode) {
      // Given
      when(compiler.getCompilationMode())
          .thenReturn(mode);

      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(AnnotationProcessorDiscovery.INCLUDE_DEPENDENCIES);

      // When
      doConfigureAnnotationProcessorPaths();

      // Then
      verify(fileManager)
          .copyContainers(StandardLocation.CLASS_PATH, StandardLocation.ANNOTATION_PROCESSOR_PATH);
    }

    @DisplayName("Ensure ANNOTATION_PROCESSOR_PATH exists when AP discovery enabled")
    @CsvSource({
        "INCLUDE_DEPENDENCIES, COMPILATION_AND_ANNOTATION_PROCESSING",
        "INCLUDE_DEPENDENCIES, ANNOTATION_PROCESSING_ONLY",
        "ENABLED,              COMPILATION_AND_ANNOTATION_PROCESSING",
        "ENABLED,              ANNOTATION_PROCESSING_ONLY",
    })
    @ParameterizedTest(name = "when AnnotationProcessorDiscovery = {0} and compilation mode = {1}")
    void ensureAnnotationProcessorPathExistsWhenApDiscoveryEnabled(
        AnnotationProcessorDiscovery discovery,
        CompilationMode mode
    ) {
      // Given
      when(compiler.getCompilationMode())
          .thenReturn(mode);
      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(discovery);

      // When
      doConfigureAnnotationProcessorPaths();

      // Then
      verify(fileManager).ensureEmptyLocationExists(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    }

    @DisplayName("Ensure no changes if AP discovery is disabled")
    @EnumSource(value = CompilationMode.class, names = "COMPILATION_ONLY", mode = Mode.EXCLUDE)
    @ParameterizedTest(name = "for compilation mode = {0}")
    void ensureNoChangesIfApDiscoveryDisabled(CompilationMode compilationMode) {
      // Given
      when(compiler.getCompilationMode())
          .thenReturn(compilationMode);
      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(AnnotationProcessorDiscovery.DISABLED);

      // When
      doConfigureAnnotationProcessorPaths();

      // Then
      verifyNoInteractions(fileManager);
    }
  }

  ///////////////////////////////////
  /// .configureRequiredLocations ///
  ///////////////////////////////////

  @DisplayName("JctJsr199Interop#configureRequiredLocations tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureRequiredLocationsTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Workspace workspace;

    @Mock
    JctFileManagerImpl fileManager;

    void doConfigureRequiredLocations() {
      configureRequiredLocations(workspace, fileManager);
    }

    @DisplayName("The expected locations are created when not present")
    @EnumSource(
        value = StandardLocation.class,
        names = {
            "SOURCE_OUTPUT",
            "CLASS_OUTPUT",
            "NATIVE_HEADER_OUTPUT",
        }
    )
    @ParameterizedTest(name = "An empty path for {0} is created when it is not present")
    void expectedLocationsAreCreated(Location expectedLocation) {
      // Given
      when(fileManager.hasLocation(any())).thenReturn(true);
      when(fileManager.hasLocation(expectedLocation)).thenReturn(false);

      var expectedManagedDirectory = mock(ManagedDirectory.class);
      when(workspace.createPackage(expectedLocation)).thenReturn(expectedManagedDirectory);

      // When
      doConfigureRequiredLocations();

      // Then
      verify(workspace).createPackage(expectedLocation);
      verify(fileManager).addPath(expectedLocation, expectedManagedDirectory);
      verifyNoMoreInteractions(workspace);
    }

    @DisplayName("No locations are created if all are present")
    @Test
    void noLocationsAreCreatedIfAllArePresent() {
      // Given
      when(fileManager.hasLocation(any())).thenReturn(true);

      // When
      doConfigureRequiredLocations();

      // Then
      verifyNoInteractions(workspace);
      verify(fileManager, atLeastOnce()).hasLocation(any());
      verifyNoMoreInteractions(fileManager);
    }
  }

  /////////////////////////////
  /// .findCompilationUnits ///
  /////////////////////////////

  @DisplayName("JctJsr199Interop#findCompilationUnits tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class FindCompilationUnitsTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MockedStatic<JctJsr199Interop> staticMock;

    @Mock
    JctFileManagerImpl fileManager;

    @BeforeEach
    void setUp() {
      staticMock.when(() -> findCompilationUnits(any()))
          .thenCallRealMethod();
    }

    List<JavaFileObject> doFindCompilationUnits() throws IOException {
      return findCompilationUnits(fileManager);
    }

    @DisplayName("All compilation units are returned")
    @Test
    void allCompilationUnitsAreReturned() throws IOException {
      // Given
      var locationsAndFiles = Map.<Location, Iterable<JavaFileObject>>of(
          someLocation(), List.of(someJavaFileObject(), someJavaFileObject()),
          someLocation(), List.of(),
          someLocation(), List.of(someJavaFileObject(), someJavaFileObject(), someJavaFileObject()),
          someLocation(), List.of(someJavaFileObject(), someJavaFileObject(), someJavaFileObject()),
          someLocation(), List.of(someJavaFileObject())
      );

      staticMock.when(() -> findCompilationUnitLocations(any()))
          .thenReturn(List.copyOf(locationsAndFiles.keySet()));

      for (var location : locationsAndFiles.keySet()) {
        when(fileManager.list(same(location), any(), any(), anyBoolean()))
            .thenReturn(locationsAndFiles.get(location));
      }

      // When
      var actualFiles = doFindCompilationUnits();

      // Then
      for (var location : locationsAndFiles.keySet()) {
        verify(fileManager).list(location, "", Set.of(Kind.SOURCE), true);
      }

      assertThat(actualFiles)
          .containsExactlyInAnyOrderElementsOf(flatten(locationsAndFiles.values()));

      verifyNoMoreInteractions(fileManager);
    }
  }

  /////////////////////////////////////
  /// .findCompilationUnitLocations ///
  /////////////////////////////////////

  @DisplayName("JctJsr199Interop#findCompilationUnitLocations tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class FindCompilationUnitLocationsTest {

    @Mock
    JctFileManagerImpl fileManager;

    List<Location> doFindCompilationUnitLocations() throws IOException {
      return findCompilationUnitLocations(fileManager);
    }

    @DisplayName("Modules are returned if present")
    @Test
    void modulesAreReturnedIfPresent() throws IOException {
      // Given
      var module1 = someModuleLocation("module1");
      var module2 = someModuleLocation("module2");
      var module3 = someModuleLocation("module3");
      var module4 = someModuleLocation("module4");
      var module5 = someModuleLocation("module5");
      var module6 = someModuleLocation("module6");
      var module7 = someModuleLocation("module7");

      var listLocationsForModulesResult = List.<Set<Location>>of(
          Set.of(module1, module2, module3),
          Set.of(),
          Set.of(module4),
          Set.of(),
          Set.of(module5, module6),
          Set.of(module7)
      );

      when(fileManager.listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH))
          .thenReturn(listLocationsForModulesResult);

      // When
      var result = doFindCompilationUnitLocations();

      // Then
      verify(fileManager).listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH);
      verifyNoMoreInteractions(fileManager);
      assertThat(result)
          .containsExactlyInAnyOrder(module1, module2, module3, module4, module5, module6, module7);
    }

    @DisplayName("Source path is not returned if modules are present")
    @Test
    void sourcePathIsNotReturnedIfModulesArePresent() throws IOException {
      // Given
      var module1 = someModuleLocation("module1");
      var module2 = someModuleLocation("module2");
      var module3 = someModuleLocation("module3");
      var module4 = someModuleLocation("module4");
      var module5 = someModuleLocation("module5");
      var module6 = someModuleLocation("module6");
      var module7 = someModuleLocation("module7");

      var listLocationsForModulesResult = List.<Set<Location>>of(
          Set.of(module1, module2, module3),
          Set.of(),
          Set.of(module4),
          Set.of(),
          Set.of(module5, module6),
          Set.of(module7)
      );

      when(fileManager.listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH))
          .thenReturn(listLocationsForModulesResult);

      // When
      var result = doFindCompilationUnitLocations();

      // Then
      assertThat(result).doesNotContain(StandardLocation.SOURCE_PATH);
    }

    @DisplayName("Source path is returned if no modules are present")
    @Test
    void sourcePathIsReturnedIfNoModulesArePresent() throws IOException {
      when(fileManager.listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH))
          .thenReturn(List.of());

      // When
      var result = doFindCompilationUnitLocations();

      // Then
      assertThat(result)
          .hasSize(1)
          .containsExactly(StandardLocation.SOURCE_PATH);
    }

    ModuleLocation someModuleLocation(String name) {
      return new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, name);
    }
  }

  ////////////////////////////////
  /// .buildDiagnosticListener ///
  ////////////////////////////////

  @DisplayName("JctJsr199Interop#buildDiagnosticListener tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class BuildDiagnosticListenerTest {

    @Mock
    JctCompiler<?, ?> compiler;

    TracingDiagnosticListener<JavaFileObject> doBuildDiagnosticListener() {
      return buildDiagnosticListener(compiler);
    }

    @DisplayName("TracingDiagnosticListener is initialised with the expected arguments")
    @CsvSource({
        "STACKTRACES,  true,  true",
        "    ENABLED,  true, false",
        "   DISABLED, false, false"
    })
    @ParameterizedTest(name = "LoggingMode.{0} implies logging = {1}, stackTraces = {2}")
    void tracingDiagnosticListenerIsInitialisedWithExpectedArguments(
        LoggingMode loggingMode,
        boolean logging,
        boolean stackTraces
    ) {
      // Given
      when(compiler.getDiagnosticLoggingMode()).thenReturn(loggingMode);

      // When
      var listener = doBuildDiagnosticListener();

      // Then
      assertSoftly(softly -> {
        softly.assertThat(listener.isLoggingEnabled())
            .as("listener.isLoggingEnabled()")
            .isEqualTo(logging);
        softly.assertThat(listener.isStackTraceReportingEnabled())
            .as("listener.isStackTraceReportingEnabled()")
            .isEqualTo(stackTraces);
      });
    }
  }

  ////////////////////////////
  /// .performCompilerPass ///
  ////////////////////////////

  @DisplayName("JctJsr199Interop#performCompilerPass tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class PerformCompilerPassTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MockedStatic<JctJsr199Interop> staticMock;

    @Mock(name = "compiler that is mocked with Mockito")
    JctCompiler<?, ?> compiler;

    @Mock(strictness = Mock.Strictness.LENIENT)
    JavaCompiler jctJsr199Compiler;

    @Mock
    TeeWriter writer;

    List<String> flags;

    @Mock
    JctFileManager fileManager;

    @Mock
    TracingDiagnosticListener<JavaFileObject> tracingDiagnosticListener;

    List<JavaFileObject> compilationUnits;

    @Mock
    CompilationTask task;

    @BeforeEach
    void setUp() {
      staticMock
          .when(() -> performCompilerPass(any(), any(), any(), any(), any(), any(), any(), any()))
          .thenCallRealMethod();
      when(jctJsr199Compiler.getTask(any(), any(), any(), any(), any(), any()))
          .thenReturn(task);
      flags = someFlags();
      compilationUnits = someCompilationUnits();
    }

    boolean doPerformCompilerPass(@Nullable Collection<String> classes) {
      return performCompilerPass(
          compiler,
          jctJsr199Compiler,
          writer,
          flags,
          fileManager,
          tracingDiagnosticListener,
          compilationUnits,
          classes
      );
    }

    @DisplayName("the compilation task is initialised in the correct order before being called")
    @MethodSource(
        "io.github.ascopes.jct.tests.unit.compilers.impl.JctJsr199InteropTest#explicitClassesArgs"
    )
    @ParameterizedTest(name = "for classes = {0}")
    void theCompilationTaskIsInitialisedInTheCorrectOrderBeforeBeingCalled(
        @Nullable Collection<String> classes
    ) {
      // Given
      var locale = someLocale();
      when(compiler.getLocale()).thenReturn(locale);
      when(task.call()).thenReturn(true);
      var orderedMock = inOrder(jctJsr199Compiler, JctJsr199Interop.class, task);

      // When
      doPerformCompilerPass(classes);

      // Then
      orderedMock.verify(jctJsr199Compiler).getTask(
          writer,
          fileManager,
          tracingDiagnosticListener,
          flags,
          classes,
          compilationUnits
      );
      orderedMock.verify(task).setLocale(locale);
      orderedMock.verify(staticMock, () -> configureAnnotationProcessorDiscovery(compiler, task));
      orderedMock.verify(task).call();
      orderedMock.verifyNoMoreInteractions();
    }

    @DisplayName("Compilations return the result")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "when calling the compilation task returns {0}")
    void compilationsReturnTheResult(boolean expectedResult) {
      // Given
      when(task.call()).thenReturn(expectedResult);

      // When
      var actualResult = doPerformCompilerPass(null);

      // Then
      assertThat(actualResult).isEqualTo(expectedResult);
      verify(task).call();
    }

    @DisplayName("Buggy compilers returning null from task#call() raise an exception")
    @Test
    void buggyCompilersReturningNullFromTaskCallRaiseException() {
      // Given
      when(task.call()).thenReturn(null);

      // Then
      assertThatThrownBy(() -> doPerformCompilerPass(null))
          .isInstanceOf(JctCompilerException.class)
          .hasNoCause()
          .hasMessage(
              "Compiler \"%s\" failed to produce a valid result, this is a bug in the "
                  + "compiler implementation, please report it to the compiler vendor!",
              compiler
          )
          .hasNoSuppressedExceptions();
    }

    @DisplayName("Exceptions thrown by the compiler are wrapped and reraised")
    @Test
    void exceptionsThrownByTheCompilerAreWrappedAndReraised() {
      // Given
      var cause = someUncheckedException();
      when(task.call()).thenThrow(cause);

      // Then
      assertThatThrownBy(() -> doPerformCompilerPass(null))
          .isInstanceOf(JctCompilerException.class)
          .hasCause(cause)
          .hasMessage(
              "Compiler \"%s\" raised an unhandled exception",
              compiler
          )
          .hasNoSuppressedExceptions();
    }
  }

  //////////////////////////////////////////////
  /// .configureAnnotationProcessorDiscovery ///
  //////////////////////////////////////////////

  @DisplayName("JctJsr199Interop#configureAnnotationProcessorDiscovery tests")
  @ExtendWith(MockitoExtension.class)
  @Nested
  class ConfigureAnnotationProcessorDiscoveryTest {

    @Mock
    JctCompiler<?, ?> compiler;

    @Mock
    CompilationTask task;

    void doConfigureAnnotationProcessorDiscovery() {
      configureAnnotationProcessorDiscovery(compiler, task);
    }

    @DisplayName("Disable AP discovery if any Processors are provided")
    @CsvSource({
        "ENABLED,              COMPILATION_AND_ANNOTATION_PROCESSING,  1",
        "ENABLED,              ANNOTATION_PROCESSING_ONLY,             1",
        "ENABLED,              COMPILATION_AND_ANNOTATION_PROCESSING,  2",
        "ENABLED,              ANNOTATION_PROCESSING_ONLY,             2",
        "ENABLED,              COMPILATION_AND_ANNOTATION_PROCESSING,  3",
        "ENABLED,              ANNOTATION_PROCESSING_ONLY,             3",
        "ENABLED,              COMPILATION_AND_ANNOTATION_PROCESSING,  5",
        "ENABLED,              ANNOTATION_PROCESSING_ONLY,             5",
        "ENABLED,              COMPILATION_AND_ANNOTATION_PROCESSING, 10",
        "ENABLED,              ANNOTATION_PROCESSING_ONLY,            10",
        "INCLUDE_DEPENDENCIES, COMPILATION_AND_ANNOTATION_PROCESSING,  1",
        "INCLUDE_DEPENDENCIES, ANNOTATION_PROCESSING_ONLY,             1",
        "INCLUDE_DEPENDENCIES, COMPILATION_AND_ANNOTATION_PROCESSING,  2",
        "INCLUDE_DEPENDENCIES, ANNOTATION_PROCESSING_ONLY,             2",
        "INCLUDE_DEPENDENCIES, COMPILATION_AND_ANNOTATION_PROCESSING,  3",
        "INCLUDE_DEPENDENCIES, ANNOTATION_PROCESSING_ONLY,             3",
        "INCLUDE_DEPENDENCIES, COMPILATION_AND_ANNOTATION_PROCESSING,  5",
        "INCLUDE_DEPENDENCIES, ANNOTATION_PROCESSING_ONLY,             5",
        "INCLUDE_DEPENDENCIES, COMPILATION_AND_ANNOTATION_PROCESSING, 10",
        "INCLUDE_DEPENDENCIES, ANNOTATION_PROCESSING_ONLY,            10",
        "DISABLED,             COMPILATION_AND_ANNOTATION_PROCESSING,  1",
        "DISABLED,             ANNOTATION_PROCESSING_ONLY,             1",
        "DISABLED,             COMPILATION_AND_ANNOTATION_PROCESSING,  2",
        "DISABLED,             ANNOTATION_PROCESSING_ONLY,             2",
        "DISABLED,             COMPILATION_AND_ANNOTATION_PROCESSING,  3",
        "DISABLED,             ANNOTATION_PROCESSING_ONLY,             3",
        "DISABLED,             COMPILATION_AND_ANNOTATION_PROCESSING,  5",
        "DISABLED,             ANNOTATION_PROCESSING_ONLY,             5",
        "DISABLED,             COMPILATION_AND_ANNOTATION_PROCESSING, 10",
        "DISABLED,             ANNOTATION_PROCESSING_ONLY,            10",
    })
    @ParameterizedTest(
        name = "for {2} explicit processor(s) when compilation mode = {1} and discovery = {0}"
    )
    void disableApDiscoveryIfAnyProcessorsAreProvidedExplicitly(
        AnnotationProcessorDiscovery discovery,
        CompilationMode compilationMode,
        int processorCount
    ) {
      // Given
      var processors = Stream.generate(Fixtures::someAnnotationProcessor)
          .limit(processorCount)
          .collect(Collectors.toList());

      when(compiler.getCompilationMode())
          .thenReturn(compilationMode);

      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(discovery);
      when(compiler.getAnnotationProcessors())
          .thenReturn(processors);

      // When
      doConfigureAnnotationProcessorDiscovery();

      // Then
      verify(task).setProcessors(processors);
      verifyNoMoreInteractions(task);
      verify(compiler).getAnnotationProcessorDiscovery();
      verify(compiler).getAnnotationProcessors();
      verifyNoMoreInteractions(compiler);
    }

    @DisplayName("Do nothing when the compiler mode disables annotation processing")
    @EnumSource(value = AnnotationProcessorDiscovery.class)
    @ParameterizedTest(name = "for discovery mode {0}")
    void ignoreAnnotationProcessing(AnnotationProcessorDiscovery discovery) {
      // Given
      when(compiler.getCompilationMode())
          .thenReturn(CompilationMode.COMPILATION_ONLY);
      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(discovery);
      when(compiler.getAnnotationProcessors())
          .thenReturn(List.of());

      // When
      doConfigureAnnotationProcessorDiscovery();

      // Then
      verifyNoInteractions(task);
    }

    @DisplayName("Enable AP discovery when no processors are provided and discovery is enabled")
    @EnumSource(
        value = AnnotationProcessorDiscovery.class,
        mode = Mode.EXCLUDE,
        names = {"DISABLED"}
    )
    @ParameterizedTest(name = "for discovery mode {0}")
    void enableApDiscovery(AnnotationProcessorDiscovery discovery) {
      // Given
      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(discovery);
      when(compiler.getAnnotationProcessors())
          .thenReturn(List.of());

      // When
      doConfigureAnnotationProcessorDiscovery();

      // Then
      verifyNoInteractions(task);
    }

    @DisplayName("Disable AP discovery when no processors are provided and discovery is disabled")
    @Test
    void disableApDiscovery() {
      // Given
      when(compiler.getAnnotationProcessorDiscovery())
          .thenReturn(AnnotationProcessorDiscovery.DISABLED);
      when(compiler.getAnnotationProcessors())
          .thenReturn(List.of());

      // When
      doConfigureAnnotationProcessorDiscovery();

      // Then
      verify(task).setProcessors(List.of());
      verifyNoMoreInteractions(task);
    }
  }

  static Stream<Collection<String>> explicitClassesArgs() {
    return Stream.of(
        null,
        Set.of("org.example.Foo", "org.example.Bar")
    );
  }
}
