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
package io.github.ascopes.jct.acceptancetests.serviceloaderjpms.testing;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.github.ascopes.jct.acceptancetests.serviceloaderjpms.ServiceProcessor;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

@DisplayName("ServiceProcessor tests (JPMS)")
class ServiceProcessorJpmsTest {

  @DisplayName("Expected files get created when the processor is run")
  @JavacCompilerTest(minVersion = 9)
  void expectedFilesGetCreated(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "org", "example");

      var compilation = compiler
          .addAnnotationProcessors(new ServiceProcessor())
          .compile(workspace);

      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("META-INF", "services", "org.example.InsultProvider")
          .hasContent("org.example.MeanInsultProviderImpl");
    }
  }

  @DisplayName("Expected files get created when the processor is run in multi-module roots")
  @JavacCompilerTest(minVersion = 9)
  void expectedFilesGetCreatedInMultiModuleRoots(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathModule("org.example")
          .copyContentsFrom("src", "test", "resources", "code");

      var compilation = compiler
          .addAnnotationProcessors(new ServiceProcessor())
          .compile(workspace);

      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputModules().moduleExists("org.example")
          .fileExists("META-INF", "services", "org.example.InsultProvider")
          .hasContent("org.example.MeanInsultProviderImpl");
    }
  }
}
