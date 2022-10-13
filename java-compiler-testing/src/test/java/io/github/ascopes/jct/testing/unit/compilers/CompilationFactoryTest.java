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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.compilers.CompilationFactory;
import io.github.ascopes.jct.compilers.CompilationImpl;
import io.github.ascopes.jct.compilers.FileManagerBuilder;
import io.github.ascopes.jct.compilers.FlagBuilder;
import io.github.ascopes.jct.jsr199.FileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Abstract test base for {@link CompilationFactory} tests.
 *
 * @author Ashley Scopes
 */
@ExtendWith(MockitoExtension.class)
abstract class CompilationFactoryTest {

  CompilationFactory<StubbedCompiler> compilationFactory;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
  StubbedCompiler compiler;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_SMART_NULLS)
  JavaCompiler jsr199Compiler;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_SELF)
  FlagBuilder flagBuilder;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_SMART_NULLS)
  CompilationTask compilationTask;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
  FileManagerBuilder fileManagerTemplate;

  @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
  FileManager fileManager;

  Boolean expectedCompilationResult;

  @BeforeEach
  void setUp() {
    compilationFactory = new CompilationFactory<>();
    expectedCompilationResult = true;

    given(jsr199Compiler.getTask(any(), any(), any(), any(), any(), any()))
        .willAnswer(ctx -> compilationTask);
    given(flagBuilder.build())
        .willAnswer(Answers.RETURNS_MOCKS);
    given(compilationTask.call())
        .willAnswer(ctx -> expectedCompilationResult);
    given(fileManagerTemplate.createFileManager(any()))
        .willReturn(fileManager);
  }

  final CompilationImpl execute() {
    return compilationFactory.compile(compiler, fileManagerTemplate, jsr199Compiler, flagBuilder);
  }

  abstract static class StubbedCompiler
      implements Compilable<StubbedCompiler, CompilationImpl> {
  }
}
