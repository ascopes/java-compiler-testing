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
package io.github.ascopes.jct.acceptancetests.immutables;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.junit.JavacCompilers;
import io.github.ascopes.jct.paths.RamPath;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Example integration test that makes use of the Immutables annotation processor.
 *
 * <p>This proves the ability to interoperate between a common and widely used annotation processor
 * library and the JCT library.
 *
 * @author Ashley Scopes
 */
@DisplayName("Immutables Integration test")
class ImmutablesIntegrationTest {

  @DisplayName("Immutables @Value produces the expected class")
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void immutablesValueProducesTheExpectedClass(Compilable<?, ?> compiler)
      throws ReflectiveOperationException {
    // Given
    var sources = RamPath
        .createPath("sources")
        .createFile(
            "io/github/ascopes/jct/acceptancetests/immutables/dataclass/Animal.java",
            "package io.github.ascopes.jct.acceptancetests.immutables.dataclass;",
            "",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            // We have to set this to force immutables to not try and use javax.annotation.Generated
            // which is only available in JDK-8 and older. Not sure why it is doing this, but it
            // looks like a bug in Immutables rather than JCT itself, since it appears to be
            // resolving the class somewhere. Guessing there is an issue with the usage of the
            // --release javac flag somewhere?
            "@Value.Style(allowedClasspathAnnotations={java.lang.annotation.Inherited.class})",
            "public interface Animal {",
            "  String name();",
            "  int legCount();",
            "  int age();",
            "}"
        );

    var compilation = compiler
        .addSourcePath(sources)
        .compile();

    assertThatCompilation(compilation)
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
        .loadClass("io.github.ascopes.jct.acceptancetests.immutables.dataclass.ImmutableAnimal");

    var builder = animalClass.getDeclaredMethod("builder").invoke(null);
    var builderClass = builder.getClass();
    builder = builderClass.getDeclaredMethod("name", String.class).invoke(builder, "Cat");
    builder = builderClass.getDeclaredMethod("legCount", int.class).invoke(builder, 4);
    builder = builderClass.getDeclaredMethod("age", int.class).invoke(builder, 5);
    var animal = builderClass.getDeclaredMethod("build").invoke(builder);

    assertThat(animal)
        .hasFieldOrPropertyWithValue("name", "Cat")
        .hasFieldOrPropertyWithValue("legCount", 4)
        .hasFieldOrPropertyWithValue("age", 5);
  }
}
