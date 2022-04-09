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

import com.github.ascopes.jct.compilers.Compilers;
import com.github.ascopes.jct.paths.InMemoryPath;
import java.util.stream.IntStream;
import javax.lang.model.SourceVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Basic legacy compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic module compilation integration tests")
class BasicModuleCompilationTest {

  @DisplayName("I can compile a 'Hello, World!' module program with javac")
  @MethodSource("versions")
  @ParameterizedTest(name = "I can compile a 'Hello, World!' module program with javac (Java {0})")
  void helloWorldJavac(int version) {
    var sources = InMemoryPath
        .createPath()
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
            "  requires java.base;",
            "  exports com.example;",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addSourcePath(sources)
        .deprecationWarnings(true)
        .releaseVersion(version)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();
  }

  // FIXME: work out how to get around this.
  // Looks like passing --module-source-path and --add-modules on the commandline along with the
  // compilation units may fix this, but I can't get this to work in-code, only via the command
  // line runner, which is unfortunate.
  @Disabled(
      "ECJ currently does not appear to interoperate correctly with JPMS when called from JSR-199"
  )
  @DisplayName("I can compile a 'Hello, World!' module program with ecj")
  @MethodSource("versions")
  @ParameterizedTest(name = "I can compile a 'Hello, World!' module program with ecj (Java {0})")
  void helloWorldEcj(int version) {
    var sources = InMemoryPath
        .createPath()
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
            "  requires java.base;",
            "  exports com.example;",
            "}"
        );

    var compilation = Compilers
        .ecj()
        .addSourcePath(sources)
        .deprecationWarnings(true)
        .releaseVersion(version)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();
  }

  static IntStream versions() {
    return IntStream.rangeClosed(9, SourceVersion.latest().ordinal());
  }
}
