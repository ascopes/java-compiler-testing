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
package io.github.ascopes.jct.compilers.impl;

import static io.github.ascopes.jct.fixtures.ExtraArgumentMatchers.containsExactlyElements;
import static io.github.ascopes.jct.fixtures.Fixtures.oneOf;
import static io.github.ascopes.jct.fixtures.Fixtures.someBinaryName;
import static io.github.ascopes.jct.fixtures.Fixtures.someFlags;
import static io.github.ascopes.jct.fixtures.Fixtures.someLinesOfText;
import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static io.github.ascopes.jct.fixtures.Fixtures.someTraceDiagnostic;
import static io.github.ascopes.jct.utils.IterableUtils.flatten;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.diagnostics.TeeWriter;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction.MockInitializer;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

/**
 * {@link JctCompilationFactoryImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctCompilationFactoryImpl tests")
@ExtendWith(MockitoExtension.class)
class JctCompilationFactoryImplTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  JctFileManager fileManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  JavaCompiler javaCompiler;

  List<String> flags;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  JctCompiler jctCompiler;

  @InjectMocks
  JctCompilationFactoryImpl factory;

  @BeforeEach
  void setUp() {
    flags = someFlags();
  }

  JctCompilation doCompile(@Nullable Collection<String> classNames) {
    return factory.createCompilation(flags, fileManager, javaCompiler, classNames);
  }

  @DisplayName("Multi-module sources are used when they exist")
  @Test
  void multiModuleSourcesAreUsedWhenTheyExist() throws IOException {
    // Given
    var apiLocation = new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, "org.example.api");
    var apiObjects = Set.of(
        somePathFileObject(someBinaryName()),
        somePathFileObject(someBinaryName())
    );

    var implLocation = new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, "org.example.impl");
    var implObjects = Set.of(
        somePathFileObject(someBinaryName()),
        somePathFileObject(someBinaryName())
    );

    final var allObjects = flatten(Set.of(apiObjects, implObjects));

    var multiModuleLocations = (Iterable<Set<Location>>) Set.of(
        Set.<Location>of(apiLocation, implLocation)
    );

    when(fileManager.listLocationsForModules(any()))
        .thenReturn(multiModuleLocations);

    when(fileManager.list(eq(apiLocation), any(), any(), anyBoolean()))
        .thenReturn(apiObjects);

    when(fileManager.list(eq(implLocation), any(), any(), anyBoolean()))
        .thenReturn(implObjects);

    // When
    doCompile(null);

    // Then
    verify(fileManager).listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH);
    verify(fileManager).list(apiLocation, "", Set.of(Kind.SOURCE), true);
    verify(fileManager).list(implLocation, "", Set.of(Kind.SOURCE), true);
    verifyNoMoreInteractions(fileManager);

    verify(javaCompiler).getTask(
        any(),
        any(),
        any(),
        any(),
        any(),
        containsExactlyElements(allObjects)
    );
  }

  @DisplayName("Legacy sources are used when multi-module sources do not exist")
  @Test
  void legacySourcesAreUsedWhenMultiModuleSourcesDoNotExist() throws IOException {
    // Given
    when(fileManager.listLocationsForModules(any()))
        .thenReturn(Set.of());

    var sourceObjects = Set.of(
        somePathFileObject(someBinaryName()),
        somePathFileObject(someBinaryName()),
        somePathFileObject(someBinaryName()),
        somePathFileObject(someBinaryName())
    );

    when(fileManager.list(eq(StandardLocation.SOURCE_PATH), any(), any(), anyBoolean()))
        .thenReturn(sourceObjects);

    // When
    doCompile(null);

    // Then
    verify(fileManager).listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH);
    verify(fileManager).list(StandardLocation.SOURCE_PATH, "", Set.of(Kind.SOURCE), true);
    verifyNoMoreInteractions(fileManager);

    verify(javaCompiler).getTask(
        any(),
        any(),
        any(),
        any(),
        any(),
        containsExactlyElements(sourceObjects)
    );
  }

  @DisplayName("All compilation units are used when null classNames collection is provided")
  @Test
  void allCompilationUnitsAreUsedWhenNullClassNamesCollectionIsProvided() throws IOException {
    // Given
    var compilationUnits = Set.of(
        somePathFileObject("foo.bar.Baz"),
        somePathFileObject("do.ray.Me"),
        somePathFileObject("a.b.C")
    );
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(compilationUnits);

    // When
    doCompile(null);

    // Then
    verify(javaCompiler).getTask(
        any(),
        same(fileManager),
        any(),
        any(),
        isNull(),
        eq(compilationUnits)
    );
  }

  @DisplayName("All compilation units are included in the compilation result when null "
      + "classNames collection is provided")
  @Test
  void allCompilationUnitsAreIncludedInTheCompilationResultWhenNullClassNamesCollectionIsProvided()
      throws IOException {
    // Given
    var compilationUnits = Set.of(
        somePathFileObject("foo.bar.Baz"),
        somePathFileObject("do.ray.Me"),
        somePathFileObject("a.b.C")
    );
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(compilationUnits);

    // When
    var result = doCompile(null);

    // Then
    assertThat(result.getCompilationUnits())
        .isEqualTo(compilationUnits);
  }

  @DisplayName("Filtering compilation units with an empty class names collection raises an error")
  @Test
  void filteringCompilationUnitsWithEmptyClassNamesCollectionRaisesAnError() {
    // Then
    assertThatThrownBy(() -> doCompile(List.of()))
        .isInstanceOf(JctCompilerException.class)
        .hasMessage("The list of explicit class names to compile is empty");

    verifyNoInteractions(javaCompiler);
  }

  @DisplayName("Filtered compilation units are used when class names are provided")
  @Test
  void filteredCompilationUnitsAreUsedWhenClassNamesAreProvided() throws IOException {
    // Given
    var fooBarBaz = somePathFileObject("foo.bar.Baz");
    var doRayMe = somePathFileObject("do.ray.Me");

    var compilationUnits = Set.of(fooBarBaz, doRayMe, somePathFileObject("a.b.C"));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(compilationUnits);

    // When
    doCompile(Set.of("foo.bar.Baz", "do.ray.Me"));

    // Then
    verify(javaCompiler).getTask(
        any(),
        same(fileManager),
        any(),
        any(),
        isNull(),
        containsExactlyElements(doRayMe, fooBarBaz)
    );
  }

  @DisplayName("Filtered compilation units are included in the compilation result when "
      + "class names are provided")
  @Test
  void filteredCompilationUnitsAreIncludedInTheCompilationResultWhenClassNamesAreProvided()
      throws IOException {
    // Given
    var fooBarBaz = somePathFileObject("foo.bar.Baz");
    var doRayMe = somePathFileObject("do.ray.Me");

    var compilationUnits = Set.of(fooBarBaz, doRayMe, somePathFileObject("a.b.C"));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(compilationUnits);

    // When
    var result = doCompile(Set.of("foo.bar.Baz", "do.ray.Me"));

    // Then
    assertThat(result.getCompilationUnits())
        .containsExactlyInAnyOrder(fooBarBaz, doRayMe);
  }

  @DisplayName("An error is raised when an explicit class name is not in the compilation units")
  @Test
  void anErrorIsRaisedWhenAnExplicitClassNameIsNotInTheCompilationUnits() throws IOException {
    // Given
    var fooBarBaz = somePathFileObject("foo.bar.Baz");
    var doRayMe = somePathFileObject("do.ray.Me");

    var compilationUnits = Set.of(fooBarBaz, doRayMe, somePathFileObject("a.b.C"));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(compilationUnits);

    // Then
    assertThatThrownBy(() -> doCompile(Set.of("foo.bar.Baz", "this.clazz.does.not.Exist")))
        .isInstanceOf(JctCompilerException.class)
        .hasMessage(
            "No compilation unit matching this.clazz.does.not.Exist found in the provided sources"
        );
    verifyNoInteractions(javaCompiler);
  }

  @DisplayName("An error is raised if no compilation units are found")
  @Test
  void anErrorIsRaisedIfNoCompilationUnitsAreFound() throws IOException {
    // Given
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(Set.of());

    // Then
    assertThatThrownBy(() -> doCompile(null))
        .isInstanceOf(JctCompilerException.class)
        .hasMessage("No compilation units were found in the given workspace");
    verifyNoInteractions(javaCompiler);
  }

  @DisplayName("The compiler should write logs to System.out")
  @Test
  void theCompilerShouldWriteLogsToSystemOut() throws IOException {
    // Given
    try (var teeWriterStatic = mockStatic(TeeWriter.class)) {
      var teeWriter = mock(TeeWriter.class);
      teeWriterStatic.when(() -> TeeWriter.wrapOutputStream(any(), any()))
          .thenReturn(teeWriter);
      when(teeWriter.getContent())
          .thenReturn(someLinesOfText());

      // Do not inline this, it will break in Mockito's stubber backend.
      var fileObjects = Set.of(somePathFileObject(someBinaryName()));
      when(fileManager.list(any(), any(), any(), anyBoolean()))
          .thenReturn(fileObjects);

      // When
      doCompile(null);

      // Then
      teeWriterStatic.verify(() -> TeeWriter.wrapOutputStream(
          System.out,
          jctCompiler.getLogCharset()
      ));
      teeWriterStatic.verifyNoMoreInteractions();

      // Ensure the tee writer is used for the compiler task.
      verify(javaCompiler).getTask(same(teeWriter), any(), any(), any(), any(), any());
    }
  }

  @DisplayName("The tee writer logs should be placed in the compilation result")
  @Test
  void teeWriterLogsShouldBePlacedInTheCompilationResult() throws IOException {
    // Given
    try (var teeWriterStatic = mockStatic(TeeWriter.class)) {
      var teeWriter = mock(TeeWriter.class);
      var order = inOrder(teeWriter);

      teeWriterStatic.when(() -> TeeWriter.wrapOutputStream(any(), any()))
          .thenReturn(teeWriter);
      var lines = someLinesOfText();
      when(teeWriter.getContent()).thenReturn(lines);

      // Do not inline this, it will break in Mockito's stubber backend.
      var fileObjects = Set.of(somePathFileObject(someBinaryName()));
      when(fileManager.list(any(), any(), any(), anyBoolean()))
          .thenReturn(fileObjects);

      // When
      var result = doCompile(null);

      // Then
      assertThat(result.getOutputLines())
          .containsExactlyElementsOf(lines.lines().collect(toList()));

      // Ensure we flushed before we called toString, otherwise data might be missing.
      order.verify(teeWriter).flush();
      order.verify(teeWriter).getContent();
    }
  }

  @DisplayName("A correctly configured diagnostic listener is used for compilation")
  @CsvSource({
      "DISABLED,   false, false",
      "ENABLED,     true, false",
      "STACKTRACES, true,  true"
  })
  @ParameterizedTest(name = "- LoggingMode.{0} should use new TracingDiagnosticListener({1}, {2})")
  @SuppressWarnings({"unchecked", "rawtypes", "AssertBetweenInconvertibleTypes"})
  void correctlyConfiguredDiagnosticListenerIsUsedForCompilation(
      LoggingMode loggingMode,
      boolean expectedEnabled,
      boolean expectedStackTraces
  ) throws IOException {
    // Given
    when(jctCompiler.getDiagnosticLoggingMode())
        .thenReturn(loggingMode);

    MockInitializer<TracingDiagnosticListener> verifier = (mock, ctx) -> {
      assertThat(ctx.arguments())
          .hasSize(2)
          .satisfies(
              args -> assertThat(args).element(0).isEqualTo(expectedEnabled),
              args -> assertThat(args).element(1).isEqualTo(expectedStackTraces)
          );
    };

    try (var listenerCls = mockConstruction(TracingDiagnosticListener.class, verifier)) {
      // Do not inline this, it will break in Mockito's stubber backend.
      var fileObjects = Set.of(somePathFileObject(someBinaryName()));
      when(fileManager.list(any(), any(), any(), anyBoolean()))
          .thenReturn(fileObjects);

      // When
      doCompile(null);

      // Then
      verify(javaCompiler).getTask(
          any(),
          any(),
          same(listenerCls.constructed().iterator().next()),
          any(),
          any(),
          any()
      );
    }
  }

  @DisplayName("Arguments get placed in the compilation result")
  @Test
  void argumentsGetPlacedInTheCompilationResult() throws IOException {
    // Given
    var fileObjects = Set.of(
        somePathFileObject("foo.bar.Baz"),
        somePathFileObject("do.ray.Me"),
        somePathFileObject("a.b.C")
    );
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    var result = doCompile(null);

    // Then
    assertThat(result.getArguments()).isEqualTo(flags);
  }

  @DisplayName("Diagnostics get placed in the compilation result")
  @Test
  @SuppressWarnings("rawtypes")
  void diagnosticsGetPlacedInTheCompilationResult() throws IOException {
    // Given
    var diagnostics = List.of(
        someTraceDiagnostic(),
        someTraceDiagnostic(),
        someTraceDiagnostic(),
        someTraceDiagnostic()
    );

    MockInitializer<TracingDiagnosticListener> configurer =
        (mock, ctx) -> when(mock.getDiagnostics()).thenReturn(diagnostics);

    try (var ignored = mockConstruction(TracingDiagnosticListener.class, configurer)) {
      // Do not inline this, it will break in Mockito's stubber backend.
      var fileObjects = Set.of(somePathFileObject(someBinaryName()));
      when(fileManager.list(any(), any(), any(), anyBoolean()))
          .thenReturn(fileObjects);

      // When
      var result = doCompile(null);

      // Then
      assertThat(result.getDiagnostics())
          .isEqualTo(diagnostics);
    }
  }

  @DisplayName("The file manager is passed to the compiler task")
  @Test
  void theFileManagerIsPassedToTheCompilationTask() throws IOException {
    // Given
    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    doCompile(null);

    // Then
    verify(javaCompiler).getTask(any(), same(fileManager), any(), any(), any(), any());
  }

  @DisplayName("The file manager is included in the compilation result")
  @Test
  void theFileManagerIsIncludedInTheCompilationResult() throws IOException {
    // Given
    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    var result = doCompile(null);

    // Then
    assertThat(result.getFileManager())
        .isSameAs(fileManager);
  }

  @DisplayName("Flags are passed to the compilation task")
  @Test
  void flagsArePassedToTheCompilationTask() throws IOException {
    // Given
    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    doCompile(null);

    // Then
    verify(javaCompiler).getTask(any(), any(), any(), same(flags), any(), any());
  }

  @DisplayName("Annotation processors are not registered if processors list is empty")
  @Test
  void annotationProcessorsAreNotRegisteredIfProcessorsListIsEmpty() throws IOException {
    // Given
    when(jctCompiler.getAnnotationProcessors()).thenReturn(List.of());

    var task = mock(CompilationTask.class);
    when(javaCompiler.getTask(any(), any(), any(), any(), any(), any()))
        .thenReturn(task);

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    doCompile(null);

    // Then
    verify(task, never()).setProcessors(any());
  }

  @DisplayName("Annotation processors are registered if processors list is not empty")
  @Test
  void annotationProcessorsAreRegisteredIfProcessorsListIsNotEmpty() throws IOException {
    // Given
    var processors = List.of(
        mock(Processor.class),
        mock(Processor.class),
        mock(Processor.class),
        mock(Processor.class)
    );
    when(jctCompiler.getAnnotationProcessors()).thenReturn(processors);

    var task = mock(CompilationTask.class);
    when(javaCompiler.getTask(any(), any(), any(), any(), any(), any()))
        .thenReturn(task);

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    doCompile(null);

    // Then
    verify(task).setProcessors(processors);
  }

  @DisplayName("The locale is set on the compiler task")
  @Test
  void theLocaleIsSetOnTheCompilerTask() throws IOException {
    // Given
    var locale = someLocale();
    when(jctCompiler.getLocale()).thenReturn(locale);

    var task = mock(CompilationTask.class);
    when(javaCompiler.getTask(any(), any(), any(), any(), any(), any()))
        .thenReturn(task);

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    doCompile(null);

    // Then
    verify(task).setLocale(locale);
  }

  @DisplayName("The compilation is invoked and the outcome is placed in the compilation result")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when CompilationTask.call() returns {0}")
  void theCompilationIsInvokedAndTheOutcomeIsPlacedInTheCompilationResult(boolean success)
      throws IOException {
    // Given
    var task = mock(CompilationTask.class);
    var order = inOrder(task);
    when(javaCompiler.getTask(any(), any(), any(), any(), any(), any()))
        .thenReturn(task);
    when(task.call())
        .thenReturn(success);

    when(jctCompiler.getAnnotationProcessors())
        .thenReturn(mock());

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    var result = doCompile(null);

    // Then
    assertThat(result.isSuccessful())
        .isEqualTo(success);

    // Verify that we call the .call() method as the last thing we call on the object.
    order.verify(task).setProcessors(any());
    order.verify(task).setLocale(any());
    order.verify(task).call();
    order.verifyNoMoreInteractions();
  }

  @DisplayName("Exceptions during compilation get wrapped and rethrown")
  @Test
  void exceptionsDuringCompilationGetWrappedAndRethrown() throws IOException {
    // Given
    var task = mock(CompilationTask.class);
    var cause = new IOException("Something is messed up");

    when(javaCompiler.getTask(any(), any(), any(), any(), any(), any()))
        .thenReturn(task);
    when(task.call())
        .thenThrow(cause);

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // Then
    assertThatThrownBy(() -> doCompile(null))
        .isInstanceOf(JctCompilerException.class)
        .hasMessage("Failed to perform compilation, an unexpected exception was raised")
        .hasCause(cause);
  }

  @DisplayName("Compilers returning null outcomes will be raised as an exception")
  @Test
  void compilersReturningNullOutcomesWillBeRaisedAsAnException() throws IOException {
    // Given
    var task = mock(CompilationTask.class);
    var name = someText();

    when(jctCompiler.getName())
        .thenReturn(name);

    when(javaCompiler.getTask(any(), any(), any(), any(), any(), any()))
        .thenReturn(task);
    when(task.call())
        .thenReturn(null);

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // Then
    assertThatThrownBy(() -> doCompile(null))
        .isInstanceOf(JctCompilerException.class)
        .hasMessage("Failed to perform compilation, an unexpected exception was raised")
        .cause()
        .hasMessage("Compiler %s task .call() method returned null unexpectedly!", name)
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("The compilation result holds the failOnWarnings flag from the compiler")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for compiler.isFailOnWarnings() = {0}")
  void theCompilationResultHoldsTheFailOnWarningsFlagFromTheCompiler(boolean failOnWarnings)
      throws IOException {
    // Given
    when(jctCompiler.isFailOnWarnings())
        .thenReturn(failOnWarnings);

    // Do not inline this, it will break in Mockito's stubber backend.
    var fileObjects = Set.of(somePathFileObject(someBinaryName()));
    when(fileManager.list(any(), any(), any(), anyBoolean()))
        .thenReturn(fileObjects);

    // When
    var result = doCompile(null);

    // Then
    assertThat(result.isFailOnWarnings())
        .isEqualTo(failOnWarnings);
  }

  static JavaFileObject somePathFileObject(String binaryName) {
    var pathFileObject = mock(PathFileObject.class, withSettings().strictness(Strictness.LENIENT));
    when(pathFileObject.getBinaryName()).thenReturn(binaryName);
    return pathFileObject;
  }

  static Locale someLocale() {
    return oneOf(
        Locale.ENGLISH,
        Locale.CHINESE,
        Locale.JAPAN,
        Locale.CANADA,
        Locale.GERMANY
    );
  }
}
