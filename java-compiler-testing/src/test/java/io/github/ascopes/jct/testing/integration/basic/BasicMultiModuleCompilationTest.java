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

package io.github.ascopes.jct.testing.integration.basic;

import io.github.ascopes.jct.assertions.CompilationAssert;
import io.github.ascopes.jct.compilers.Compiler.Logging;
import io.github.ascopes.jct.compilers.Compilers;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.testing.helpers.Skipping;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import javax.lang.model.SourceVersion;
import javax.tools.StandardLocation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Basic multi-module compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic multi-module compilation integration tests")
class BasicMultiModuleCompilationTest {

  @DisplayName("I can compile a multi-module 'Hello, World!' program with javac")
  @MethodSource("javacVersions")
  @ParameterizedTest(name = "targeting Java {0}")
  void helloWorldJavac(int version) {
    var source = RamPath
        .createPath("hello.world")
        .createFile(
            "com/example/HelloWorld.java",
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module hello.world {",
            "  exports com.example;",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "hello.world", source)
        .showDeprecationWarnings(true)
        //.diagnosticLogging(Logging.STACKTRACES)
        //.fileManagerLogging(Logging.ENABLED)
        .release(version)
        .compile();

    CompilationAssert.assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

    // TODO(ascopes): fix this to work with the file manager rewrite.
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/com/example/HelloWorld.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/module-info.class")
    //    .exists()
    //    .isNotEmptyFile();
  }

  @DisplayName("I can compile a 'Hello, World!' program with ecj")
  @MethodSource("ecjVersions")
  @ParameterizedTest(name = "targeting Java {0}")
  void helloWorldEcj(int version) {
    Skipping.skipBecauseEcjFailsToSupportModulesCorrectly();

    var source = RamPath
        .createPath("hello.world")
        .createFile(
            "com/example/HelloWorld.java",
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module hello.world {",
            "  exports com.example;",
            "}"
        );

    var compilation = Compilers
        .ecj()
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "hello.world", source)
        .showDeprecationWarnings(true)
        .release(version)
        .verbose(true)
        //.diagnosticLogging(Logging.STACKTRACES)
        //.fileManagerLogging(Logging.ENABLED)
        .compile();

    CompilationAssert.assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

    // TODO(ascopes): fix this to work with the file manager rewrite.
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/com/example/HelloWorld.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/module-info.class")
    //    .exists()
    //    .isNotEmptyFile();
  }

  @DisplayName("I can compile multiple modules with javac")
  @MethodSource("javacVersions")
  @ParameterizedTest(name = "targeting Java {0}")
  void helloWorldMultiModuleJavac(int version) {
    var helloWorld = RamPath
        .createPath("hello.world")
        .createFile(
            "com/example/HelloWorld.java",
            "package com.example;",
            "import com.example.greeter.Greeter;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(Greeter.greet(\"World\"));",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module hello.world {",
            "  requires greeter;",
            "  exports com.example;",
            "}"
        );
    var greeter = RamPath
        .createPath("greeter")
        .createFile(
            "com/example/greeter/Greeter.java",
            "package com.example.greeter;",
            "public class Greeter {",
            "  public static String greet(String name) {",
            "    return \"Hello, \" + name + \"!\";",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module greeter {",
            "  exports com.example.greeter;",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "hello.world", helloWorld)
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "greeter", greeter)
        //.showDeprecationWarnings(true)
        //.diagnosticLogging(Logging.STACKTRACES)
        .release(version)
        .compile();

    CompilationAssert.assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

    // TODO(ascopes): fix this to work with the file manager rewrite.
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/com/example/HelloWorld.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/module-info.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("greeter/com/example/greeter/Greeter.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("greeter/module-info.class")
    //    .exists()
    //    .isNotEmptyFile();
  }

  @DisplayName("I can compile multiple modules with ecj")
  @MethodSource("ecjVersions")
  @ParameterizedTest(name = "targeting Java {0}")
  void helloWorldMultiModuleEcj(int version) {
    Skipping.skipBecauseEcjFailsToSupportModulesCorrectly();

    var helloWorld = RamPath
        .createPath("hello.world")
        .createFile(
            "com/example/HelloWorld.java",
            "package com.example;",
            "import com.example.greeter.Greeter;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(Greeter.greet(\"World\"));",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module hello.world {",
            "  requires greeter;",
            "  exports com.example;",
            "}"
        );
    var greeter = RamPath
        .createPath("greeter")
        .createFile(
            "com/example/greeter/Greeter.java",
            "package com.example.greeter;",
            "public class Greeter {",
            "  public static String greet(String name) {",
            "    return \"Hello, \" + name + \"!\";",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module greeter {",
            "  exports com.example.greeter;",
            "}"
        );

    var compilation = Compilers
        .ecj()
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "hello.world", helloWorld)
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "greeter", greeter)
        .showDeprecationWarnings(true)
        //.diagnosticLogging(Logging.STACKTRACES)
        //.fileManagerLogging(Logging.ENABLED)
        .release(version)
        .compile();

    CompilationAssert.assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

    // TODO(ascopes): fix this to work with the file manager rewrite.
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/com/example/HelloWorld.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("hello.world/module-info.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("greeter/com/example/greeter/Greeter.class")
    //    .exists()
    //    .isNotEmptyFile();
    //
    //CompilationAssert.assertThatCompilation(compilation)
    //    .classOutput()
    //    .file("greeter/module-info.class")
    //    .exists()
    //    .isNotEmptyFile();
  }

  static IntStream javacVersions() {
    return IntStream.rangeClosed(9, SourceVersion.latestSupported().ordinal());
  }

  static IntStream ecjVersions() {
    var maxEcjVersion = (ClassFileConstants.getLatestJDKLevel() >> (Short.BYTES * 8))
        - ClassFileConstants.MAJOR_VERSION_0;

    return LongStream.rangeClosed(9, maxEcjVersion)
        .mapToInt(i -> (int) i);
  }
}
