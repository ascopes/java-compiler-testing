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
package io.github.ascopes.jct.testing.unit.compilers;

import static io.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.clearInvocations;

import io.github.ascopes.jct.compilers.SimpleCompilationFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@code buildFlags} in {@link SimpleCompilationFactory}.
 *
 * @author Ashley Scopes
 */
@DisplayName("SimpleCompilationFactory buildFlags tests")
class SimpleCompilationFactoryBuildFlagsTest extends AbstractSimpleCompilationFactoryTest {

  private static final Random RANDOM = new Random();

  @DisplayName("annotation processor options should be added")
  @Test
  void annotationProcessorOptionsShouldBeAdded() {
    var options = someListOfOptions();
    given(compiler.getAnnotationProcessorOptions()).willReturn(options);
    execute();
    then(flagBuilder).should().annotationProcessorOptions(options);
  }

  @DisplayName("deprecation warnings flags should be added")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for showDeprecationWarnings = {0}")
  void deprecationWarningsShouldBeAdded(boolean showDeprecationWarnings) {
    given(compiler.isShowDeprecationWarnings()).willReturn(showDeprecationWarnings);
    execute();
    then(flagBuilder).should().showDeprecationWarnings(showDeprecationWarnings);
  }

  @DisplayName("fail-on-warnings warnings flags should be added")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for failOnWarnings = {0}")
  void failOnWarningsShouldBeAdded(boolean failOnWarnings) {
    given(compiler.isFailOnWarnings()).willReturn(failOnWarnings);
    execute();
    then(flagBuilder).should().failOnWarnings(failOnWarnings);
  }

  @DisplayName("compiler options should be added")
  @Test
  void compilerOptionsShouldBeAdded() {
    var options = someListOfOptions();
    given(compiler.getCompilerOptions()).willReturn(options);
    execute();
    then(flagBuilder).should().compilerOptions(options);
  }

  @DisplayName("runtime options should be added")
  @Test
  void runtimeOptionsShouldBeAdded() {
    var options = someListOfOptions();
    given(compiler.getRuntimeOptions()).willReturn(options);
    execute();
    then(flagBuilder).should().runtimeOptions(options);
  }

  @DisplayName("preview features should be added")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for previewFeatures = {0}")
  void previewFeaturesShouldBeAdded(boolean previewFeatures) {
    given(compiler.isPreviewFeatures()).willReturn(previewFeatures);
    execute();
    then(flagBuilder).should().previewFeatures(previewFeatures);
  }

  @DisplayName("release should be added")
  @NullSource
  @ValueSource(strings = {"8", "11", "17"})
  @ParameterizedTest(name = "for release = {0}")
  void releaseShouldBeAdded(String release) {
    given(compiler.getRelease()).willReturn(Optional.ofNullable(release));
    execute();
    then(flagBuilder).should().release(release);
  }

  @DisplayName("source should be added")
  @NullSource
  @ValueSource(strings = {"8", "11", "17"})
  @ParameterizedTest(name = "for source = {0}")
  void sourceShouldBeAdded(String source) {
    given(compiler.getSource()).willReturn(Optional.ofNullable(source));
    execute();
    then(flagBuilder).should().source(source);
  }

  @DisplayName("target should be added")
  @NullSource
  @ValueSource(strings = {"8", "11", "17"})
  @ParameterizedTest(name = "for target = {0}")
  void targetShouldBeAdded(String target) {
    given(compiler.getTarget()).willReturn(Optional.ofNullable(target));
    execute();
    then(flagBuilder).should().target(target);
  }

  @DisplayName("verbose should be added")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for verbose = {0}")
  void verboseShouldBeAdded(boolean verbose) {
    given(compiler.isVerbose()).willReturn(verbose);
    execute();
    then(flagBuilder).should().verbose(verbose);
  }

  @DisplayName("show warnings should be added")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for showWarnings = {0}")
  void showWarningsShouldBeAdded(boolean showWarnings) {
    given(compiler.isShowWarnings()).willReturn(showWarnings);
    execute();
    then(flagBuilder).should().showWarnings(showWarnings);
  }

  @DisplayName("built flags are passed to the compilation task")
  @Test
  void builtFlagsArePassedToTheCompilationTask() throws IOException {
    var flags = someListOfOptions();
    given(flagBuilder.build()).willReturn(flags);
    given(fileManager.list(any(), any(), any(), anyBoolean()))
        .willReturn(List.of(stub(JavaFileObject.class)));
    execute();
    then(jsr199Compiler).should().getTask(any(), any(), any(), eq(flags), any(), any());
  }

  @DisplayName("no calls should occur on the flag builder after build()")
  @Test
  void noCallsShouldOccurOnTheFlagBuilderAfterBuild() {
    var wasCalled = new AtomicBoolean(false);

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

    then(flagBuilder).shouldHaveNoInteractions();
  }

  static List<String> someListOfOptions() {
    return Stream
        .generate(() -> Integer.toHexString(RANDOM.nextInt(Integer.MAX_VALUE)))
        .limit(RANDOM.nextInt(5) + 2)
        .collect(Collectors.toList());
  }
}
