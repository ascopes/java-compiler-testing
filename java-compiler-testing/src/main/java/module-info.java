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
import io.github.ascopes.jct.junit.JctExtension;
import io.github.ascopes.jct.workspaces.RamFileSystemProvider;
import org.junit.jupiter.api.extension.Extension;

/**
 * A framework for performing exhaustive integration testing against Java compilers in modern Java
 * libraries, with a focus on full JPMS support.
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
 * <a href="https://github.com/ascopes/java-compiler-testing">on GitHub</a>.
 *
 * <p>Releases can be found on
 * <a href="https://repo1.maven.org/maven2/io/github/ascopes/jct/java-compiler-testing/">
 * Maven Central</a>.
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
 *      {@literal @JavacCompilerTest(minVersion=11, maxVersion=19)}
 *      void theJsonSchemaIsCreatedFromTheInputCode(JctCompiler&lt;?, ?&gt; compiler) {
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
 *              .classOutputs().packages()
 *              .fileExists("json-schemas", "UserSchema.json").contents()
 *              .isNotEmpty()
 *              .satisfies(contents -> JSONAssert.assertEquals(...));
 *      }
 *    }
 * </code></pre>
 */
@SuppressWarnings("removal")
module io.github.ascopes.jct {

  ////////////////////
  /// DEPENDENCIES ///
  ////////////////////

  requires java.compiler;
  requires java.management;
  requires jimfs;
  requires me.xdrop.fuzzywuzzy;
  requires static transitive org.apiguardian.api;
  requires org.assertj.core;
  requires static org.jspecify;
  requires static transitive org.junit.jupiter.api;
  requires static transitive org.junit.jupiter.params;
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

  provides Extension with JctExtension;
  uses RamFileSystemProvider;

  //////////////////////////////////////////////////////
  /// EXPOSURE OF INTERNALS TO THE TESTING NAMESPACE ///
  //////////////////////////////////////////////////////

  exports io.github.ascopes.jct.compilers.impl to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.compilers.javac to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.containers.impl to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.filemanagers.impl to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.workspaces.impl to io.github.ascopes.jct.testing;

  //////////////////////////////////////////////////////////////////////////
  /// EXPOSURE OF ALL COMPONENTS TO THE TESTING NAMESPACE FOR REFLECTION ///
  //////////////////////////////////////////////////////////////////////////

  opens io.github.ascopes.jct.assertions to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers.impl to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers.javac to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.containers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.containers.impl to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.diagnostics to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.ex to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.filemanagers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.filemanagers.config to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.filemanagers.impl to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.junit to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.repr to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.workspaces to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.workspaces.impl to io.github.ascopes.jct.testing;
}
