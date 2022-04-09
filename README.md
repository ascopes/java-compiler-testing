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

The following is an example of using this library with JUnit Jupiter:

```java
class HelloWorldTest {

  @Test
  void i_can_compile_hello_world() {
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

    var compilation = Compilers
        .javac()
        .addSourcePath(sources)
        .releaseVersion("11")
        .withDiagnosticLogging(LoggingMode.ENABLED)
        .compile();

    assertThat(compilation).isSuccessfulWithoutWarnings();
    assertThat(compilation).diagnostics().isEmpty();
    assertThat(compilation).classOutput()
        .file("org/me/test/examples/HelloWorld.class")
        .exists()
        .isNotEmptyFile();
  }
}
```

## Features

- Ability to run compilation on real files, classpath resources,
  and in-memory files.
- Provides support for `javac` and `ecj` out of the box, with the
  ability to support other JSR-199 implementations if desired.
- Implements a fully functional JSR-199 Path JavaFileManager.
- Fluent syntax for creating configurations, executing them, and
  inspecting the results.
- Integration with AssertJ for fluent assertions on compilation
  results.
- Fuzzy matching of similar path names when asserting files exist
  to provide helpful assertion errors.
- Ability to have multiple source roots, just like when using
  `javac` normally.
