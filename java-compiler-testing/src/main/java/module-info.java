/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
import io.github.ascopes.jct.workspaces.impl.MemoryFileSystemProvider.MemoryFileSystemUrlHandlerProvider;
import java.net.spi.URLStreamHandlerProvider;
import org.jspecify.annotations.NullMarked;

/**
 * A framework for performing exhaustive integration testing against Java compilers in modern Java
 * libraries, with a focus on full JPMS support.
 *
 * <p><strong>For Java 11 support, please use version 4 of this library, which is still kept up to date.
 * Version 5 of this library only supports Java 17 onwards.</strong>
 *
 * <p>The <em>Java Compiler Testing</em> API has a number of facilities for assisting in testing
 * anything related to the Java compiler. This includes Javac plugins and JSR-199 annotation
 * processors.
 *
 * <p>All test cases are designed to be as stateless as possible, with facilities to produce
 * managed test directories on RAM disks or in temporary locations within the host file system.
 *
 * <p>Integration test cases can be written to cross-compile against a range of Java compiler
 * versions, with the ability to provide as much or as little configuration detail as you wish.
 *
 * <p>Compilation results are complimented with a suite of
 * {@link io.github.ascopes.jct.assertions.JctAssertions assertion} facilities that extend the
 * AssertJ API to assist in writing fluent and human-readable test cases for your code. Each of
 * these assertions comes with specially-developed human-readable error messages and formatting.
 *
 * <p>Full {@link io.github.ascopes.jct.junit.JavacCompilerTest JUnit5 integration}
 * is provided to streamline the development process, letting you focus on <strong>your</strong>
 * code rather than flaky test environments and dependency management.</p>
 *
 * <p>Any questions, feedback, or issues can be submitted
 * <a target="_blank" href="https://github.com/ascopes/java-compiler-testing">on GitHub</a>.
 *
 * <p>Releases can be found on
 * <a target="_blank"
 * href="https://repo1.maven.org/maven2/io/github/ascopes/jct/java-compiler-testing/"> Maven
 * Central</a>.
 *
 * <h2>Common elements</h2>
 *
 * <ul>
 *   <li><strong>JctCompiler</strong> - an encapsulation of the compiler API that can be
 *     interacted with to configure how the compiler runs.
 *   <li><strong>JctCompilation</strong> - the result of calling {@code compile()} on a
 *     JctCompiler object. It holds information about whether the compilation succeeded, what
 *     was logged, and can be asserted upon to verify various conditions.
 *   <li><strong>Workspace</strong> - a wrapper around a temporary file system that holds
 *     all the compiler inputs and outputs. This can be asserted upon to verify various 
 *     conditions, and is designed to be created per test case to help keep your tests
 *     reproducible.
 * </ul>
 *
 * <h2>A simple example</h2>
 *
 * <pre><code>
 *    import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
 *
 *    import io.github.ascopes.jct.compilers.JctCompiler;
 *    import io.github.ascopes.jct.junit.JavacCompilerTest;
 *    import io.github.ascopes.jct.workspaces.Workspaces;
 *    import org.example.processor.JsonSchemaAnnotationProcessor;
 *    import org.skyscreamer.jsonassert.JSONAssert;
 *
 *    class JsonSchemaAnnotationProcessorTest {
 *
 *      {@literal @EcjCompilerTest(minVersion=17)}
 *      {@literal @JavacCompilerTest(minVersion=17)}
 *      void theJsonSchemaIsCreatedFromTheInputCode(JctCompiler compiler) {
 *
 *        try (var workspace = Workspaces.newWorkspace()) {
 *          // Given
 *          workspace
 *              .createSourcePathPackage()
 *              .createDirectory("org", "example", "tests")
 *              .copyContentsFrom("src", "test", "resources", "code", "schematest");
 *
 *          // When
 *          var compilation = compiler
 *              .addAnnotationProcessors(new JsonSchemaAnnotationProcessor())
 *              .addAnnotationProcessorOptions("jsonschema.verbose=true")
 *              .failOnWarnings(true)
 *              .showDeprecationWarnings(true)
 *              .compile(workspace);
 *
 *          // Then
 *          assertThatCompilation(compilation)
 *              .isSuccessfulWithoutWarnings();
 *
 *          assertThatCompilation(compilation)
 *              .diagnostics().notes().singleElement()
 *              .message().isEqualTo(
 *                  "Creating JSON schema in Java %s for package org.example.tests",
 *                  compiler.getRelease()
 *              );
 *
 *          assertThatCompilation(compilation)
 *              .classOutputPackages()
 *              .fileExists("json-schemas", "UserSchema.json").contents()
 *              .isNotEmpty()
 *              .satisfies(contents -> JSONAssert.assertEquals(...));
 *      }
 *    }
 * </code></pre>
 */
@NullMarked
module io.github.ascopes.jct {

  ////////////////////
  /// DEPENDENCIES ///
  ////////////////////

  requires com.github.marschall.memoryfilesystem;
  requires java.compiler;
  requires java.management;
  requires me.xdrop.fuzzywuzzy;
  requires org.assertj.core;
  requires org.eclipse.jdt.core.compiler.batch;
  requires static org.jspecify;
  requires static org.junit.jupiter.api;
  requires static org.junit.jupiter.params;
  requires org.slf4j;

  //////////////////
  /// PUBLIC API ///
  //////////////////

  exports io.github.ascopes.jct.assertions;
  exports io.github.ascopes.jct.containers;
  exports io.github.ascopes.jct.compilers;
  exports io.github.ascopes.jct.diagnostics;
  exports io.github.ascopes.jct.ex;
  exports io.github.ascopes.jct.filemanagers;
  exports io.github.ascopes.jct.filemanagers.config;
  exports io.github.ascopes.jct.junit;
  exports io.github.ascopes.jct.repr;
  exports io.github.ascopes.jct.workspaces;

  ///////////////////////////////////
  /// SERVICE PROVIDER INTERFACES ///
  ///////////////////////////////////

  provides URLStreamHandlerProvider with MemoryFileSystemUrlHandlerProvider;
}
