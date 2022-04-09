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

package com.github.ascopes.jct.integrationtests.basic;

import static com.github.ascopes.jct.assertions.CompilationAssert.assertThat;

import com.github.ascopes.jct.compilers.Compiler.LoggingMode;
import com.github.ascopes.jct.compilers.Compilers;
import com.github.ascopes.jct.paths.InMemoryPath;
import java.util.stream.IntStream;
import javax.lang.model.SourceVersion;
import org.junit.jupiter.api.Disabled;
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
  @MethodSource("versions")
  @ParameterizedTest(name = "I can compile a 'Hello, World!' program with javac (Java {0})")
  void helloWorldJavac(int version) {
    var sources = InMemoryPath
        .createPath()
        .createFile(
            "hello.world/com/example/HelloWorld.java",
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        )
        .createFile(
            "hello.world/module-info.java",
            "module hello.world {",
            "  exports com.example;",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addModuleSourcePath(sources)
        .deprecationWarnings(true)
        .releaseVersion(version)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/com/example/HelloWorld.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/module-info.class")
        .exists()
        .isNotEmptyFile();
  }

  // FIXME: work out how to get around this.
  // Looks like passing --module-source-path and --add-modules on the commandline along with the
  // compilation units may fix this, but I can't get this to work in-code, only via the command
  // line runner, which is unfortunate.
  @Disabled(
      "ECJ currently does not appear to interoperate correctly with JPMS when called from JSR-199"
  )
  @DisplayName("I can compile a 'Hello, World!' program with ecj")
  @MethodSource("versions")
  @ParameterizedTest(name = "I can compile a 'Hello, World!' program with ecj (Java {0})")
  void helloWorldEcj(int version) {
    var sources = InMemoryPath
        .createPath()
        .createFile(
            "hello.world/com/example/HelloWorld.java",
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        )
        .createFile(
            "hello.world/module-info.java",
            "module hello.world {",
            "  exports com.example;",
            "}"
        );

    var compilation = Compilers
        .ecj()
        .addModuleSourcePath(sources)
        .deprecationWarnings(true)
        .releaseVersion(version)
        .verbose(true)
        .withDiagnosticLogging(LoggingMode.STACKTRACES)
        .withFileManagerLogging(LoggingMode.ENABLED)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/com/example/HelloWorld.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/module-info.class")
        .exists()
        .isNotEmptyFile();
  }

  @DisplayName("I can compile multiple modules with javac")
  @MethodSource("versions")
  @ParameterizedTest(name = "I can compile multiple modules with javac (Java {0})")
  void helloWorldMultiModuleJavac(int version) {
    var sources = InMemoryPath
        .createPath()
        .createFile(
            "hello.world/com/example/HelloWorld.java",
            "package com.example;",
            "import com.example.greeter.Greeter;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(Greeter.greet(\"World\"));",
            "  }",
            "}"
        )
        .createFile(
            "hello.world/module-info.java",
            "module hello.world {",
            "  requires greeter;",
            "  exports com.example;",
            "}"
        )
        .createFile(
            "greeter/com/example/greeter/Greeter.java",
            "package com.example.greeter;",
            "public class Greeter {",
            "  public static String greet(String name) {",
            "    return \"Hello, \" + name + \"!\";",
            "  }",
            "}"
        )
        .createFile(
            "greeter/module-info.java",
            "module greeter {",
            "  exports com.example.greeter;",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addModuleSourcePath(sources)
        .deprecationWarnings(true)
        .releaseVersion(version)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/com/example/HelloWorld.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/module-info.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("greeter/com/example/greeter/Greeter.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("greeter/module-info.class")
        .exists()
        .isNotEmptyFile();
  }

  // FIXME: work out how to get around this.
  // Looks like passing --module-source-path and --add-modules on the commandline along with the
  // compilation units may fix this, but I can't get this to work in-code, only via the command
  // line runner, which is unfortunate.
  @Disabled(
      "ECJ currently does not appear to interoperate correctly with JPMS when called from JSR-199"
  )
  @DisplayName("I can compile multiple modules with ecj")
  @MethodSource("versions")
  @ParameterizedTest(name = "I can compile multiple modules with ecj (Java {0})")
  void helloWorldMultiModuleEcj(int version) {
    var sources = InMemoryPath
        .createPath()
        .createFile(
            "hello.world/com/example/HelloWorld.java",
            "package com.example;",
            "import com.example.greeter.Greeter;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(Greeter.greet(\"World\"));",
            "  }",
            "}"
        )
        .createFile(
            "hello.world/module-info.java",
            "module hello.world {",
            "  requires greeter;",
            "  exports com.example;",
            "}"
        )
        .createFile(
            "greeter/com/example/greeter/Greeter.java",
            "package com.example.greeter;",
            "public class Greeter {",
            "  public static String greet(String name) {",
            "    return \"Hello, \" + name + \"!\";",
            "  }",
            "}"
        )
        .createFile(
            "greeter/module-info.java",
            "module greeter {",
            "  exports com.example.greeter;",
            "}"
        );

    var compilation = Compilers
        .ecj()
        .addModuleSourcePath(sources)
        .deprecationWarnings(true)
        .releaseVersion(version)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/com/example/HelloWorld.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("hello.world/module-info.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("greeter/com/example/greeter/Greeter.class")
        .exists()
        .isNotEmptyFile();

    assertThat(compilation)
        .classOutput()
        .file("greeter/module-info.class")
        .exists()
        .isNotEmptyFile();
  }

  static IntStream versions() {
    return IntStream.rangeClosed(9, SourceVersion.latest().ordinal());
  }
}
