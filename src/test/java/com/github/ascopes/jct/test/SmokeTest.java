package com.github.ascopes.jct.test;

import static com.github.ascopes.jct.assertions.CompilationAssertions.assertThat;

import com.github.ascopes.jct.compilers.Compilers;
import com.github.ascopes.jct.compilers.StandardCompiler;
import com.github.ascopes.jct.compilers.StandardCompiler.LoggingMode;
import com.github.ascopes.jct.paths.InMemoryPath;
import java.lang.management.ManagementFactory;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SmokeTest {

  @MethodSource("compilers")
  @ParameterizedTest(name = "I can compile something for Java {1} using {0}")
  void i_can_compile_something(StandardCompiler compiler, int version) throws Exception {

    var sources = InMemoryPath
        .create("sources")
        .createFile(
            "com/github/ascopes/jct/test/examples/HelloWorld.java",
            "package com.github.ascopes.jct.test.examples;",
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
        .withDiagnosticLogging(LoggingMode.LOGGING)
        .compile();

    assertThat(compilation)
        .isSuccessfulWithoutWarnings()
        .diagnostics().isEmpty();
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
