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

package io.github.ascopes.jct.examples.lombok;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.assertions.CompilationAssert;
import io.github.ascopes.jct.compilers.Compiler.Logging;
import io.github.ascopes.jct.compilers.Compilers;
import io.github.ascopes.jct.paths.RamPath;
import java.util.stream.IntStream;
import javax.lang.model.SourceVersion;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Example integration test that makes use of the Lombok annotation processor.
 *
 * <p>This proves the ability to interoperate between Lombok which modifies internal
 * details of the compiler implementation and the JCT facility.
 *
 * @author Ashley Scopes
 */
@DisplayName("Lombok Integration test")
class LombokIntegrationTest {

  @DisplayName("Lombok @Data compiles the expected data class")
  @MethodSource("supportedJavacVersions")
  @ParameterizedTest(name = "for Java version {0}")
  void lombokDataCompilesTheExpectedDataClass(int version) throws Exception {
    var sources = RamPath
        .createPath("sources")
        .createFile(
            "io/github/ascopes/jct/examples/lombok/dataclass/Animal.java",
            "package io.github.ascopes.jct.examples.lombok.dataclass;",
            "",
            "import lombok.Data;",
            "",
            "@Data",
            "public class Animal {",
            "  private final String name;",
            "  private final int legCount;",
            "  private final int age;",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addPath(StandardLocation.SOURCE_PATH, sources)
        .release(version)
        .fileManagerLogging(Logging.ENABLED)
        .diagnosticLogging(Logging.STACKTRACES)
        .compile();

    CompilationAssert.assertThatCompilation(compilation)
        .isSuccessful();

    // Github Issue #9 sanity check - Improve annotation processor discovery mechanism
    // TODO(ascopes): fix this to work with the file manager rewrite.
    //CompilationAssert.assertThatCompilation(compilation)
    //    .location(StandardLocation.ANNOTATION_PROCESSOR_PATH)
    //    .containsAll(compilation
    //        .getFileManager()
    //        .getExpectedManager(StandardLocation.CLASS_PATH)
    //        .getRoots());

    var animalClass = compilation
        .getFileManager()
        .getClassLoader(StandardLocation.CLASS_OUTPUT)
        .loadClass("io.github.ascopes.jct.examples.lombok.dataclass.Animal");

    var animal = animalClass
        .getDeclaredConstructor(String.class, int.class, int.class)
        .newInstance("Cat", 4, 5);

    assertThat(animal)
        .hasFieldOrPropertyWithValue("name", "Cat")
        .hasFieldOrPropertyWithValue("legCount", 4)
        .hasFieldOrPropertyWithValue("age", 5);
  }

  static IntStream supportedJavacVersions() {
    return IntStream.range(11, SourceVersion.values().length);
  }
}
