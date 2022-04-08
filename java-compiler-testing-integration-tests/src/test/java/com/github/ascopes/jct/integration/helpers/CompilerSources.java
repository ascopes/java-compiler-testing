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

package com.github.ascopes.jct.integration.helpers;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.github.ascopes.jct.compilers.Compiler;
import com.github.ascopes.jct.compilers.Compilers;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import org.junit.jupiter.api.DynamicTest;

/**
 * Helpers to create {@link DynamicTest} streams for {@link Compiler} configurations that target
 * specific JDK versions.
 *
 * @author Ashley Scopes
 */
public final class CompilerSources {

  private CompilerSources() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Create a stream of test cases for all supported compilers from Java 8 onwards..
   *
   * @param runner the test runner.
   * @return the stream of tests.
   */
  public static Stream<DynamicTest> allCompilers(CompilationRunner runner) {
    return allCompilers(8, SourceVersion.latest().ordinal(), runner);
  }

  /**
   * Create a stream of test cases for all compilers in the closed version range.
   *
   * @param minVersion the minimum Java version (inclusive).
   * @param maxVersion the maximum Java version (inclusive).
   * @param runner     the test runner.
   * @return the stream of tests.
   */
  public static Stream<DynamicTest> allCompilers(
      int minVersion,
      int maxVersion,
      CompilationRunner runner
  ) {
    return IntStream
        .rangeClosed(minVersion, maxVersion)
        .boxed()
        .flatMap(version -> Stream
            .of(Compilers.javac(), Compilers.ecj())
            .map(compiler -> dynamicTest(
                compiler + " for Java version " + version,
                () -> runner.run(compiler.releaseVersion(version))
            )))
        .parallel();
  }

  /**
   * Functional interface for a function that consumes a compiler.
   */
  @FunctionalInterface
  public interface CompilationRunner {

    void run(Compiler<?, ?> compiler) throws Throwable;
  }
}
