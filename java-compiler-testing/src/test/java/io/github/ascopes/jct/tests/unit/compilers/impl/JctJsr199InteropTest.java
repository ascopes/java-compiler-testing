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
package io.github.ascopes.jct.tests.unit.compilers.impl;

import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildDiagnosticListener;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildFileManager;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildFlags;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.buildWriter;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.compile;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureAnnotationProcessorPaths;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureClassPath;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureJvmSystemModules;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureModulePath;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configurePlatformClassPath;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureRequiredLocations;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.configureWorkspacePaths;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.determineRelease;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.findCompilationUnits;
import static io.github.ascopes.jct.compilers.impl.JctJsr199Interop.performCompilerPass;
import static io.github.ascopes.jct.tests.helpers.GenericMock.mockRaw;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctFlagBuilder;
import io.github.ascopes.jct.compilers.impl.JctCompilationImpl;
import io.github.ascopes.jct.compilers.impl.JctJsr199Interop;
import io.github.ascopes.jct.diagnostics.TeeWriter;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingFileManagerProxy;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

/**
 * {@link JctJsr199Interop} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctJsr199Interop tests")
class JctJsr199InteropTest implements UtilityClassTestTemplate {

  static final Random RANDOM = new Random();

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
      staticMock.when(() -> compile(any(), any(), any(), any()))
          .thenCallRealMethod();
    }

    JctCompilationImpl doCompile() {
      return compile(workspace, compiler, jsr199Compiler, flagBuilder);
    }

    @DisplayName("the writer is built using the compiler")
    @SuppressWarnings("resource")
    @Test
    void writerIsBuiltUsingCompiler() {
      // When
      doCompile();

      // Then
      staticMock.verify(() -> buildWriter(compiler));
    }

    @DisplayName("errors while building writers get re-raised")
    @SuppressWarnings("resource")
    @Test
    void errorsBuildingWritersAreReraised() {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildWriter(any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("the writer is closed after usage")
    @SuppressWarnings("resource")
    @Test
    void writerIsClosedAfterUsage() throws IOException {
      // Given
      var writer = mock(TeeWriter.class);
      staticMock.when(() -> buildWriter(any())).thenReturn(writer);

      // When
      doCompile();

      // Then
      verify(writer).close();
    }

    @DisplayName("the file manager is built using the compiler and workspace")
    @Test
    void fileManagerIsBuiltUsingCompilerAndWorkspace() {
      // When
      doCompile();

      // Then
      staticMock.verify(() -> buildFileManager(compiler, workspace));
    }

    @DisplayName("errors while building file managers get re-raised")
    @Test
    void errorsBuildingFileManagersAreReraised() {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("the file manager is closed after usage")
    @Test
    void fileManagerIsClosedAfterUsage() {
      // Given
      var fileManager = mock(JctFileManagerImpl.class);
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenReturn(fileManager);

      // When
      doCompile();

      // Then
      verify(fileManager).close();
    }

    @DisplayName("the flags are built using the compiler and flag builder")
    @Test
    void flagsAreBuiltUsingCompilerAndFlagBuilder() {
      // When
      doCompile();

      // Then
      staticMock.verify(() -> buildFlags(compiler, flagBuilder));
    }

    @DisplayName("errors while building flags get re-raised")
    @Test
    void errorsBuildingFlagsAreReraised() {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildFlags(any(), any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("the diagnostic listener is built using the compiler")
    @Test
    void diagnosticListenerIsBuiltUsingCompiler() {
      // When
      doCompile();

      // Then
      staticMock.verify(() -> buildDiagnosticListener(compiler));
    }

    @DisplayName("errors while building diagnostic listeners get re-raised")
    @Test
    void errorsBuildingDiagnosticListenersAreReraised() {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> buildDiagnosticListener(any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("compilation units are discovered using the file manager")
    @Test
    void compilationUnitsAreDiscoveredUsingTheFileManager() {
      // Given
      var fileManager = mock(JctFileManagerImpl.class);
      staticMock.when(() -> buildFileManager(any(), any()))
          .thenReturn(fileManager);

      // When
      doCompile();

      // Then
      staticMock.verify(() -> findCompilationUnits(fileManager));
    }

    @DisplayName("errors finding compilation units are reraised")
    @Test
    void errorsFindingCompilationUnitsAreReraised() {
      // Given
      var ex = someIoException();
      staticMock.when(() -> findCompilationUnits(any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("performCompilerPass is called with the expected arguments")
    @SuppressWarnings({"resource", "NullableProblems"})
    @Test
    void performCompilerPassCalledWithExpectedArguments() {
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
      doCompile();

      // Then
      staticMock.verify(() -> performCompilerPass(
          compiler,
          jsr199Compiler,
          writer,
          flags,
          fileManager,
          diagnosticListener,
          compilationUnits
      ));
    }

    @DisplayName("errors performing the compilation pass units are reraised")
    @Test
    void errorsPerformingTheCompilationPassAreReraised() {
      // Given
      var ex = someUncheckedException();
      staticMock.when(() -> performCompilerPass(any(), any(), any(), any(), any(), any(), any()))
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
          .isInstanceOf(JctCompilerException.class)
          .hasMessage("Failed to compile due to an error: %s", ex)
          .hasCause(ex);
    }

    @DisplayName("compilation results are returned")
    @SuppressWarnings({"resource", "NullableProblems"})
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

      var diagnostics = someDiagnostics();
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

      staticMock.when(() -> performCompilerPass(any(), any(), any(), any(), any(), any(), any()))
          .thenReturn(result);

      // When
      var compilation = doCompile();

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
    @Test
    void errorsExtractingWriterLinesAreReraised() {
      // Given
      var writer = mock(TeeWriter.class);
      staticMock.when(() -> buildWriter(any()))
          .thenReturn(writer);

      var ex = someUncheckedException();
      when(writer.toString())
          .thenThrow(ex);

      // Then
      assertThatThrownBy(this::doCompile)
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
      var actualWriter = buildWriter(compiler);

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

    @DisplayName(".buildFlags(...) builds the flags and returns them")
    @Test
    void buildFlagsBuildsTheFlags() {
      // When
      final var actualFlags = buildFlags(jctCompiler, flagBuilder);

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
          .runtimeOptions(jctCompiler.getRuntimeOptions());
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
    MockedStatic<LoggingFileManagerProxy> fileManagerProxyStaticMock;

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
      // Mockito doesn't seem to support in-order mocking of static methods, so we have to
      // improvise.
      var actualCallOrder = new ArrayList<String>();
      staticMock.when(() -> configureWorkspacePaths(any(), any()))
          .then(addMethodNameTo(actualCallOrder));
      staticMock.when(() -> configureClassPath(any(), any()))
          .then(addMethodNameTo(actualCallOrder));
      staticMock.when(() -> configureModulePath(any(), any()))
          .then(addMethodNameTo(actualCallOrder));
      staticMock.when(() -> configurePlatformClassPath(any(), any()))
          .then(addMethodNameTo(actualCallOrder));
      staticMock.when(() -> configureJvmSystemModules(any(), any()))
          .then(addMethodNameTo(actualCallOrder));
      staticMock.when(() -> configureAnnotationProcessorPaths(any(), any()))
          .then(addMethodNameTo(actualCallOrder));
      staticMock.when(() -> configureRequiredLocations(any(), any()))
          .then(addMethodNameTo(actualCallOrder));

      // When
      doBuild();

      // Then
      staticMock.verify(() -> configureWorkspacePaths(workspace, fileManager));
      staticMock.verify(() -> configureClassPath(compiler, fileManager));
      staticMock.verify(() -> configureModulePath(compiler, fileManager));
      staticMock.verify(() -> configurePlatformClassPath(compiler, fileManager));
      staticMock.verify(() -> configureJvmSystemModules(compiler, fileManager));
      staticMock.verify(() -> configureAnnotationProcessorPaths(compiler, fileManager));
      staticMock.verify(() -> configureRequiredLocations(workspace, fileManager));

      assertThat(actualCallOrder)
          .as("call order (%s)", actualCallOrder)
          .containsExactly(
              "configureWorkspacePaths",
              "configureClassPath",
              "configureModulePath",
              "configurePlatformClassPath",
              "configureJvmSystemModules",
              "configureAnnotationProcessorPaths",
              "configureRequiredLocations"
          );
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
      fileManagerProxyStaticMock.verifyNoInteractions();
    }

    @DisplayName("a proxy is used when file manager logging is enabled")
    @Test
    void proxyIsUsedWhenFileManagerLoggingIsEnabled() {
      // Given
      when(compiler.getFileManagerLoggingMode()).thenReturn(LoggingMode.ENABLED);
      var proxyFileManger = mock(JctFileManager.class);
      fileManagerProxyStaticMock.when(() -> LoggingFileManagerProxy.wrap(any(), anyBoolean()))
          .thenReturn(proxyFileManger);

      // When
      var actualFileManager = doBuild();

      // Then
      fileManagerProxyStaticMock.verify(() -> LoggingFileManagerProxy.wrap(fileManager, false));
      fileManagerProxyStaticMock.verifyNoMoreInteractions();

      assertThat(actualFileManager)
          .isSameAs(proxyFileManger)
          .isNotSameAs(fileManager);
    }

    @DisplayName("a proxy is used when file manager logging is enabled with stacktraces")
    @Test
    void proxyIsUsedWhenFileManagerLoggingIsEnabledWithStackTraces() {
      // Given
      when(compiler.getFileManagerLoggingMode()).thenReturn(LoggingMode.STACKTRACES);
      var proxyFileManger = mock(JctFileManager.class);
      fileManagerProxyStaticMock.when(() -> LoggingFileManagerProxy.wrap(any(), anyBoolean()))
          .thenReturn(proxyFileManger);

      // When
      var actualFileManager = doBuild();

      // Then
      fileManagerProxyStaticMock.verify(() -> LoggingFileManagerProxy.wrap(fileManager, true));
      fileManagerProxyStaticMock.verifyNoMoreInteractions();

      assertThat(actualFileManager)
          .isSameAs(proxyFileManger)
          .isNotSameAs(fileManager);
    }
  }

  ///////////////////////
  /// Common fixtures ///
  ///////////////////////

  List<String> someFlags() {
    return Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .map("--"::concat)
        .limit(10)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("NullableProblems")
  List<TraceDiagnostic<? extends JavaFileObject>> someDiagnostics() {
    return Stream
        .generate(() -> mockRaw(TraceDiagnostic.class)
            .<TraceDiagnostic<? extends JavaFileObject>>upcastedTo()
            .build(withSettings().strictness(Strictness.LENIENT)))
        .limit(5)
        .collect(Collectors.toList());
  }

  List<JavaFileObject> someCompilationUnits() {
    return Stream
        .generate(() -> mock(JavaFileObject.class, withSettings().strictness(Strictness.LENIENT)))
        .peek(mock -> when(mock.getName()).thenReturn(UUID.randomUUID().toString()))
        .limit(5)
        .collect(Collectors.toList());
  }

  String someLinesOfText() {
    return Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .limit(15)
        .collect(Collectors.joining("\n"));
  }

  boolean someBoolean() {
    return RANDOM.nextBoolean();
  }

  Throwable someUncheckedException() {
    var message = Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .limit(3)
        .collect(Collectors.joining(" blah blah "));
    return new RuntimeException(message)
        .fillInStackTrace();
  }

  Throwable someIoException() {
    var message = Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .limit(3)
        .collect(Collectors.joining(" blah blah "));
    return new IOException(message)
        .fillInStackTrace();
  }

  Charset someCharset() {
    var options = List.of(
        StandardCharsets.UTF_8,
        StandardCharsets.UTF_16BE,
        StandardCharsets.UTF_16LE,
        StandardCharsets.UTF_16,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.US_ASCII
    );

    return options.get(RANDOM.nextInt(options.size()));
  }

  String someRelease() {
    return Integer.toString(RANDOM.nextInt(11) + 11);
  }

  Answer<Void> addMethodNameTo(List<String> list) {
    return ctx -> {
      list.add(ctx.getMethod().getName());
      return null;
    };
  }
}
