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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Annotation that can be applied to a JUnit parameterized test to invoke that test case across
 * multiple compilers, each configured to a specific version in a range of Java language versions.
 *
 * <p>This will also add the {@code "java-compiler-testing-test"} tag and {@code "javac-test"}
 * tags to your test method, meaning you can instruct your IDE or build system to optionally only
 * run tests annotated with this method for development purposes. As an example, Maven Surefire
 * could be instructed to only run these tests by passing {@code -Dgroup="javac-test"} to Maven.
 *
 * <p>If your build is running in a GraalVM Native Image, then this test will not execute, as
 * the <em>Java Compiler Testing</em> API is not yet tested within Native Images.
 *
 * <p>For example, to run a simple test on Java 11 through 17 (inclusive):
 *
 * <pre><code>
 *   class SomeTest {
 *     {@literal @JavacCompilerTest(minVersion = 11, maxVersion = 17)}
 *     void canCompileHelloWorld(JctCompiler> compiler) {
 *       // Given
 *       try (var workspace = Workspaces.newWorkspace()) {
 *         workspace
 *            .createFile("org", "example", "HelloWorld.java")
 *            .withContents("""
 *              package org.example;
 *
 *              public class HelloWorld {
 *                public static void main(String[] args) {
 *                  System.out.println("Hello, World!");
 *                }
 *              }
 *            """);
 *
 *         var compilation = compiler.compile(workspace);
 *
 *         assertThat(compilation)
 *             .isSuccessfulWithoutWarnings();
 *       }
 *     }
 *   }
 * </code></pre>
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@ArgumentsSource(JavacCompilersProvider.class)
@DisabledInNativeImage
@Documented
@Inherited
@ParameterizedTest(name = "for compiler \"{0}\"")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Tags({
    @Tag("java-compiler-testing-test"),
    @Tag("javac-test")
})
public @interface JavacCompilerTest {

  /**
   * Minimum version to use (inclusive).
   *
   * <p>By default, it will use the lowest possible version supported by the compiler. This
   * varies between versions of the JDK that are in use.
   *
   * <p>If the version is lower than the minimum supported version, then the minimum supported
   * version of the compiler will be used instead. This enables writing tests that will work on a
   * range of JDKs during builds without needing to duplicate the test to satisfy different JDK
   * supported version ranges.
   *
   * @return the minimum version.
   */
  int minVersion() default Integer.MIN_VALUE;

  /**
   * Maximum version to use (inclusive).
   *
   * <p>By default, it will use the highest possible version supported by the compiler. This
   * varies between versions of the JDK that are in use.
   *
   * <p>If the version is higher than the maximum supported version, then the maximum supported
   * version of the compiler will be used instead. This enables writing tests that will work on a
   * range of JDKs during builds without needing to duplicate the test to satisfy different JDK
   * supported version ranges.
   *
   * @return the maximum version.
   */
  int maxVersion() default Integer.MAX_VALUE;

  /**
   * Get an array of compiler configurer classes to apply in-order before starting the test.
   *
   * <p>Each configurer must have a public no-args constructor, and their package must be
   * open to this module if JPMS modules are in-use, for example:
   *
   * <pre><code>
   * module mytests {
   *   requires io.github.ascopes.jct;
   *   requires org.junit.jupiter.api;
   *
   *   opens org.example.mytests to io.github.ascopes.jct;
   * }
   * </code></pre>
   * <p>
   * An example of usage:
   *
   * <pre><code>
   *   public class WerrorConfigurer implements JctCompilerConfigurer&lt;RuntimeException&gt; {
   *     {@literal @Override}
   *     public void configure(JctCompiler compiler) {
   *       compiler.failOnWarnings(true);
   *     }
   *   }
   *
   *   // ...
   *
   *   class SomeTest {
   *     {@literal @JavacCompilerTest(configurers = WerrorConfigurer.class)}
   *     void someTest(JctCompiler compiler) {
   *       // ...
   *     }
   *   }
   * </code></pre>
   *
   * @return an array of classes to run to configure the compiler. These run in the given order.
   */
  Class<? extends JctCompilerConfigurer<?>>[] configurers() default {};

  /**
   * The version strategy to use.
   *
   * <p>This determines whether the version number being iterated across specifies the
   * release, source, target, or source and target versions.
   *
   * <p>The default is to specify the release.
   *
   * @return the version strategy to use.
   */
  VersionStrategy versionStrategy() default VersionStrategy.RELEASE;
}
