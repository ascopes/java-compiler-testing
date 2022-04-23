[![Build](https://github.com/ascopes/java-compiler-testing/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/ascopes/java-compiler-testing/actions/workflows/build.yml)
[![Code Coverage](https://codecov.io/gh/ascopes/java-compiler-testing/branch/main/graph/badge.svg?token=VT74BP2742)](https://codecov.io/gh/ascopes/java-compiler-testing)

# java-compiler-testing

Facility for running compilation tests and annotation processing tests
for `javac` and `ecj`.

I developed this after several months of pulling out my hair trying to
find easy ways to integration test annotation processors for Java. While
one or two tools exist for Java 8, I have yet to find one that works
seamlessly with Java 11 and newer.

This module comes with full JPMS support. I decided to drop support for
Java 8 due to complexity around implementing this without the ability to
reference modules, and ideally this tool should be forward compatible to
prevent future issues for any projects deciding to use it.

**This module is still under development.** Any contributions or feedback
are always welcome!

## Example

The following is an example of using this library with JUnit Jupiter to run both javac and ECJ:

```java
class HelloWorldTest {

    @Test
    void i_can_compile_hello_world_with_javac() {
        // Given
        var sources = RamPath
                .createPath("sources")
                .createFile(
                        "org/me/test/examples/HelloWorld.java",
                        "package org.me.test.examples;",
                        "",
                        "public class HelloWorld {",
                        "  public static void main(String[] args) {",
                        "    System.out.println(\"Hello, World!\");",
                        "  }",
                        "}"
                )
                .createFile(
                        "module-info.java",
                        "module org.me.test.examples {",
                        "  exports org.me.test.examples;",
                        "}"
                );

        // When
        var compilation = Compilers
                .javac()
                .addSourceRamPaths(sources)
                .release(11)
                .compile();

        // Then
        assertThatCompilation(compilation).isSuccessfulWithoutWarnings();
        assertThatCompilation(compilation).diagnostics().isEmpty();
        assertThatCompilation(compilation).classOutput()
                .file("org/me/test/examples/HelloWorld.class")
                .exists()
                .isNotEmptyFile();
        assertThatCompilation(compilation).classOutput()
                .file("module-info.class")
                .exists()
                .isNotEmptyFile();
    }

    @Test
    void i_can_compile_hello_world_with_ecj() {
        // Given
        var sources = RamPath
                .createPath("sources")
                .createFile(
                        "org/me/test/examples/HelloWorld.java",
                        "package org.me.test.examples;",
                        "",
                        "public class HelloWorld {",
                        "  public static void main(String[] args) {",
                        "    System.out.println(\"Hello, World!\");",
                        "  }",
                        "}"
                )
                .createFile(
                        "module-info.java",
                        "module org.me.test.examples {",
                        "  exports org.me.test.examples;",
                        "}"
                );

        // When
        var compilation = Compilers
                .ecj()
                .addSourceRamPaths(sources)
                .release(11)
                .compile();

        // Then
        assertThatCompilation(compilation).isSuccessfulWithoutWarnings();
        assertThatCompilation(compilation).diagnostics().isEmpty();
        assertThatCompilation(compilation).classOutput()
                .file("org/me/test/examples/HelloWorld.class")
                .exists()
                .isNotEmptyFile();
        assertThatCompilation(compilation).classOutput()
                .file("module-info.class")
                .exists()
                .isNotEmptyFile();
    }
}
```

## Features

- Ability to run compilation on real files, class path resources,
  and in-memory files.
- Supports Java 9 JPMS modules as intended.
- Ability to customise a large assortment of configuration parameters
  to enable you to test exactly what you need to test.
- Provides support for `javac` and `ecj` out of the box, with the
  ability to support other JSR-199 implementations if desired --
  just make use of one of the compiler classes, or make your own!
- Implements a fully functional JSR-199 Path JavaFileManager.
- Fluent syntax for creating configurations, executing them, and
  inspecting the results.
- Integration with AssertJ for fluent assertions on compilation
  results.
- Fuzzy matching of similar path names when asserting files exist
  to provide helpful assertion errors.
- Ability to have multiple source roots, just like when using
  `javac` normally.
- Diagnostic reporting includes stacktraces, so you can find out
  exactly what triggered a diagnostic and begin debugging any
  issues in your applications quickly.
