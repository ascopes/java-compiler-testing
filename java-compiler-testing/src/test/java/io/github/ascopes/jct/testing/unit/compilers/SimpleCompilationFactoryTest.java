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

import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.compilers.FlagBuilder;
import io.github.ascopes.jct.compilers.SimpleCompilation;
import io.github.ascopes.jct.compilers.SimpleCompilationFactory;
import io.github.ascopes.jct.compilers.SimpleFileManagerTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link SimpleCompilationFactory} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("SimpleCompilationFactory tests")
@ExtendWith(MockitoExtension.class)
class SimpleCompilationFactoryTest {

  private static final Random RANDOM = new Random();

  SimpleCompilationFactory<StubbedCompiler> compilationFactory;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
  StubbedCompiler compiler;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_SMART_NULLS)
  JavaCompiler jsr199Compiler;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_SELF)
  FlagBuilder flagBuilder;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_SMART_NULLS)
  CompilationTask compilationTask;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
  SimpleFileManagerTemplate fileManagerTemplate;

  Boolean expectedCompilationResult;

  @BeforeEach
  void setUp() {
    compilationFactory = new SimpleCompilationFactory<>();
    expectedCompilationResult = true;

    given(jsr199Compiler.getTask(any(), any(), any(), any(), any(), any()))
        .willAnswer(ctx -> compilationTask);
    given(flagBuilder.build())
        .willAnswer(Answers.RETURNS_MOCKS);
    given(compilationTask.call())
        .willAnswer(ctx -> expectedCompilationResult);
  }

  @DisplayName("compile tests")
  @Nested
  class CompileTests {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  @DisplayName("build flags tests")
  @Nested
  class BuildFlagsTest {

    @DisplayName("annotation processor options should be added")
    @Test
    void annotationProcessorOptionsShouldBeAdded() {
      var options = someListOfOptions();
      given(compiler.getAnnotationProcessorOptions()).willReturn(options);
      execute();
      verify(flagBuilder).annotationProcessorOptions(options);
    }

    @DisplayName("deprecation warnings flags should be added")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for showDeprecationWarnings = {0}")
    void deprecationWarningsShouldBeAdded(boolean showDeprecationWarnings) {
      given(compiler.isShowDeprecationWarnings()).willReturn(showDeprecationWarnings);
      execute();
      verify(flagBuilder).showDeprecationWarnings(showDeprecationWarnings);
    }

    @DisplayName("fail-on-warnings warnings flags should be added")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for failOnWarnings = {0}")
    void failOnWarningsShouldBeAdded(boolean failOnWarnings) {
      given(compiler.isFailOnWarnings()).willReturn(failOnWarnings);
      execute();
      verify(flagBuilder).failOnWarnings(failOnWarnings);
    }

    @DisplayName("compiler options should be added")
    @Test
    void compilerOptionsShouldBeAdded() {
      var options = someListOfOptions();
      given(compiler.getCompilerOptions()).willReturn(options);
      execute();
      verify(flagBuilder).compilerOptions(options);
    }

    @DisplayName("runtime options should be added")
    @Test
    void runtimeOptionsShouldBeAdded() {
      var options = someListOfOptions();
      given(compiler.getRuntimeOptions()).willReturn(options);
      execute();
      verify(flagBuilder).runtimeOptions(options);
    }

    @DisplayName("preview features should be added")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for previewFeatures = {0}")
    void previewFeaturesShouldBeAdded(boolean previewFeatures) {
      given(compiler.isPreviewFeatures()).willReturn(previewFeatures);
      execute();
      verify(flagBuilder).previewFeatures(previewFeatures);
    }

    @DisplayName("release should be added")
    @NullSource
    @ValueSource(strings = {"8", "11", "17"})
    @ParameterizedTest(name = "for release = {0}")
    void releaseShouldBeAdded(String release) {
      given(compiler.getRelease()).willReturn(Optional.ofNullable(release));
      execute();
      verify(flagBuilder).release(release);
    }

    @DisplayName("source should be added")
    @NullSource
    @ValueSource(strings = {"8", "11", "17"})
    @ParameterizedTest(name = "for source = {0}")
    void sourceShouldBeAdded(String source) {
      given(compiler.getSource()).willReturn(Optional.ofNullable(source));
      execute();
      verify(flagBuilder).source(source);
    }

    @DisplayName("target should be added")
    @NullSource
    @ValueSource(strings = {"8", "11", "17"})
    @ParameterizedTest(name = "for target = {0}")
    void targetShouldBeAdded(String target) {
      given(compiler.getTarget()).willReturn(Optional.ofNullable(target));
      execute();
      verify(flagBuilder).target(target);
    }

    @DisplayName("verbose should be added")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for verbose = {0}")
    void verboseShouldBeAdded(boolean verbose) {
      given(compiler.isVerbose()).willReturn(verbose);
      execute();
      verify(flagBuilder).verbose(verbose);
    }

    @DisplayName("show warnings should be added")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for showWarnings = {0}")
    void showWarningsShouldBeAdded(boolean showWarnings) {
      given(compiler.isShowWarnings()).willReturn(showWarnings);
      execute();
      verify(flagBuilder).showWarnings(showWarnings);
    }

    @DisplayName("built flags are passed to the compilation task")
    @Test
    void builtFlagsArePassedToTheCompilationTask() {
      var flags = someListOfOptions();
      given(flagBuilder.build()).willReturn(flags);
      execute();
      verify(jsr199Compiler).getTask(any(), any(), any(), eq(flags), any(), any());
    }

    @DisplayName("no calls should occur on the flag builder after build()")
    @Test
    void noCallsShouldOccurOnTheFlagBuilderAfterBuild() {
      AtomicBoolean wasCalled = new AtomicBoolean(false);

      given(flagBuilder.build())
          .will(ctx -> {
            wasCalled.set(true);
            clearInvocations(flagBuilder);
            return new ArrayList<String>();
          });
      execute();

      assumeThat(wasCalled)
          .withFailMessage("build was not called, so this test makes no sense")
          .isTrue();

      verifyNoInteractions(flagBuilder);
    }
  }

  @DisplayName("build JavaFileManager tests")
  @Nested
  class BuildJavaFileManagerTest {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  @DisplayName("find compilation units tests")
  @Nested
  class FindCompilationUnitsTest {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  @DisplayName("apply logging to file manager tests")
  @Nested
  class ApplyLoggingToFileManagerTestMode {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  @DisplayName("build diagnostic listener tests")
  @Nested
  class BuildDiagnosticListenerTest {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  @DisplayName("build compilation task tests")
  @Nested
  class BuildCompilationTaskTest {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  @DisplayName("run compilation task tests")
  @Nested
  class RunCompilationTaskTest {

    @Disabled("TODO: implement")
    @Test
    void toDo() {
    }
  }

  private SimpleCompilation execute() {
    return compilationFactory.compile(compiler, fileManagerTemplate, jsr199Compiler, flagBuilder);
  }

  private static List<String> someListOfOptions() {
    return Stream
        .generate(() -> Integer.toHexString(RANDOM.nextInt(Integer.MAX_VALUE)))
        .limit(RANDOM.nextInt(5) + 2)
        .collect(Collectors.toList());
  }

  private abstract static class StubbedCompiler
      implements Compilable<StubbedCompiler, SimpleCompilation> {
  }
}
