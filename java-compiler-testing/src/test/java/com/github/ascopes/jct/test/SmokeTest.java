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

package com.github.ascopes.jct.test;

import static com.github.ascopes.jct.assertions.CompilationAssert.assertThat;

import com.github.ascopes.jct.compilers.Compilers;
import com.github.ascopes.jct.compilers.StandardCompiler;
import com.github.ascopes.jct.compilers.StandardCompiler.LoggingMode;
import com.github.ascopes.jct.paths.InMemoryPath;
import java.lang.management.ManagementFactory;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Smoke test")
class SmokeTest {

  @MethodSource("compilers")
  @ParameterizedTest(name = "I can compile something for Java {1} using {0}")
  void compilationSucceeds(StandardCompiler compiler, int version) throws Exception {

    var sources = InMemoryPath
        .createPath()
        .createFile(
            "org/me/test/examples/HelloWorld.java",
            "package org.me.test.examples.test;",
            "",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World!\");",
            "  }",
            "}"
        );

    var compilation = compiler
        .addSourcePath(sources)
        .releaseVersion(version)
        .withDiagnosticLogging(LoggingMode.ENABLED)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();
    assertThat(compilation).diagnostics().isEmpty();

    assertThat(compilation).classOutput()
        .file("org/me/test/examples/test/HelloWorld.class")
        .exists()
        .isNotEmptyFile();
  }

  static Stream<Arguments> compilers() {
    var thisVersion = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getSpecVersion());

    return Stream
        .<Supplier<StandardCompiler>>of(Compilers::javac, Compilers::ecj)
        .flatMap(compilerSupplier -> IntStream
            .rangeClosed(11, thisVersion)
            .mapToObj(version -> Arguments.of(compilerSupplier.get(), version)));
  }
}
