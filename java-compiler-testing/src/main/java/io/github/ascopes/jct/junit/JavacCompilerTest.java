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
import io.github.ascopes.jct.compilers.javac.JavacJctCompilerImpl;
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
 * Annotation that can be applied to a {@link ParameterizedTest} to enable passing in a range of
 * {@link JavacJctCompilerImpl} instances with specific configured versions as the  first
 * parameter.
 *
 * <p>This will also add the {@code "java-compiler-testing-test"} tag and {@code "javac-test"}
 * tags to your test method, meaning you can instruct your IDE or build system to optionally only
 * run tests annotated with this method for development purposes. As an example, Maven Surefire
 * could be instructed to only run these tests by passing {@code -Dgroup="javac-test"} to Maven.
 *
 * <p>If your build is running in a GraalVM Native Image, then this test will not execute, as
 * the <em>Java Compiler Testing</em> API is not yet tested within Native Images.
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
@TestTemplate
@Tags({
    @Tag("java-compiler-testing-test"),
    @Tag("javac-test")
})
public @interface JavacCompilerTest {

  /**
   * Minimum version to use (inclusive).
   *
   * @return the minimum version.
   */
  int minVersion() default Integer.MIN_VALUE;

  /**
   * Maximum version to use (inclusive).
   *
   * @return the maximum version.
   */
  int maxVersion() default Integer.MAX_VALUE;

  /**
   * Get an array of compiler configurers to apply in-order before starting the test.
   *
   * <p>Each configurer must have a public no-args constructor, and their package must be
   * open to this module if JPMS modules are in-use, for example:
   * <p>
   * <pre><code>
   * module mytests {
   *   requires io.github.ascopes.jct;
   *   requires org.junit.jupiter.api;
   *
   *   opens org.example.mytests to io.github.ascopes.jct;
   * }
   * </code></pre>
   *
   * @return an array of classes to run to configure the compiler. These run in the given order.
   */
  Class<? extends JctCompilerConfigurer<?>>[] configurers() default {};

  /**
   * Whether we need to support modules or not.
   *
   * <p>Setting this to true will skip any versions of the compiler that do not support JPMS
   * modules.
   *
   * @return {@code true} if we need to support modules, or {@code false} if we do not.
   */
  boolean modules() default false;

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
