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

/**
 * A framework for performing exhaustive integration testing against Java compilers in modern Java
 * libraries, with a focus on full JPMS support.
 *
 * <p>The <em>Java Compiler Testing</em> API has a number of facilities for assisting in testing
 * anything related to the Java compiler. This includes Javac plugins and JSR-199 annotation
 * processors.
 *
 * <p>All test cases are designed to be as stateless as possible, with facilities to produce
 * {@link io.github.ascopes.jct.pathwrappers.RamDirectory in-memory file systems} (using Google's
 * JIMFS API), or using
 * {@link io.github.ascopes.jct.pathwrappers.TempDirectory OS-provided temporary directories}. All
 * file system mechanisms are complimented with a fluent API that enables writing expressive
 * declarations without unnecessary boilerplate.
 *
 * <p>Integration test cases can be written to cross-compile against a range of Java compiler
 * versions, with the ability to provide as much or as little configuration detail as you wish.
 * Additionally, APIs can be easily
 * {@link io.github.ascopes.jct.compilers.AbstractJctCompiler extended} to integrate with any other
 * JSR-199-compliant compiler as required.
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
 * <pre><code>
 *    import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
 *    import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory;
 *
 *    import io.github.ascopes.jct.compilers.JctCompiler;
 *    import io.github.ascopes.jct.junit.JavacCompilerTest;
 *
 *    import org.example.processor.JsonSchemaAnnotationProcessor;
 *    import org.skyscreamer.jsonassert.JSONAssert;
 *
 *    class JsonSchemaAnnotationProcessorTest {
 *
 *      {@literal @JavacCompilerTest(minVersion=11, maxVersion=19)}
 *      void theJsonSchemaIsCreatedFromTheInputCode(JctCompiler&lt;?, ?&gt; compiler) {
 *
 *        // Given
 *        var sources = newRamDirectory("sources")
 *            .createDirectory("org", "example", "tests")
 *            .copyContentsFrom("src", "test", "resources", "code", "schematest");
 *
 *        // When
 *        var compilation = compiler
 *            .addSources(sources)
 *            .addAnnotationProcessors(new JsonSchemaAnnotationProcessor())
 *            .addAnnotationProcessorOptions("jsonschema.verbose=true")
 *            .failOnWarnings(true)
 *            .showDeprecationWarnings(true)
 *            .compile();
 *
 *        // Then
 *        assertThatCompilation(compilation)
 *            .isSuccessfulWithoutWarnings();
 *
 *        assertThatCompilation(compilation)
 *            .diagnostics().notes().singleElement()
 *            .message().isEqualTo(
 *                "Creating JSON schema in Java %s for package org.example.tests",
 *                compiler.getRelease()
 *            );
 *
 *        assertThatCompilation(compilation)
 *            .classOutputs().packages()
 *            .fileExists("json-schemas/UserSchema.json").contents()
 *            .isNotEmpty()
 *            .satisfies(contents -> JSONAssert.assertEquals(...));
 *      }
 *    }
 * </code></pre>
 */
module io.github.ascopes.jct {
  requires java.compiler;
  requires java.management;
  requires jimfs;
  requires static jsr305;
  requires me.xdrop.fuzzywuzzy;
  requires static transitive org.apiguardian.api;
  requires org.assertj.core;
  requires static transitive org.junit.jupiter.params;
  requires static transitive org.opentest4j;
  requires org.slf4j;

  exports io.github.ascopes.jct.assertions;
  exports io.github.ascopes.jct.containers;
  exports io.github.ascopes.jct.compilers;
  exports io.github.ascopes.jct.compilers.javac;
  exports io.github.ascopes.jct.diagnostics;
  exports io.github.ascopes.jct.ex;
  exports io.github.ascopes.jct.filemanagers;
  exports io.github.ascopes.jct.junit;
  exports io.github.ascopes.jct.pathwrappers;
  exports io.github.ascopes.jct.repr;

  opens io.github.ascopes.jct.junit;

  //////////////////////////////////////////////////////
  /// EXPOSURE OF INTERNALS TO THE TESTING NAMESPACE ///
  //////////////////////////////////////////////////////

  exports io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;

  opens io.github.ascopes.jct.assertions to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers.javac to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.containers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.diagnostics to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.ex to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.filemanagers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.pathwrappers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.repr to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;
}
