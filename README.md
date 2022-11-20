[![Build](https://github.com/ascopes/java-compiler-testing/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/ascopes/java-compiler-testing/actions/workflows/build.yml)
[![Code Coverage](https://codecov.io/gh/ascopes/java-compiler-testing/branch/main/graph/badge.svg?token=VT74BP2742)](https://codecov.io/gh/ascopes/java-compiler-testing)
[![Releases](https://img.shields.io/github/downloads/ascopes/java-compiler-testing/total)](https://github.com/ascopes/java-compiler-testing/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.ascopes.jct/java-compiler-testing)](https://search.maven.org/artifact/com.github.ascopes.jct/java-compiler-testing)
[![Issues](https://img.shields.io/github/issues-raw/ascopes/java-compiler-testing)](https://github.com/ascopes/java-compiler-testing/issues)
[![Closed Issues](https://img.shields.io/github/issues-closed-raw/ascopes/java-compiler-testing)](https://github.com/ascopes/java-compiler-testing/issues?q=is%3Aissue+is%3Aclosed)
[![License](https://img.shields.io/github/license/ascopes/java-compiler-testing)](https://github.com/ascopes/java-compiler-testing/blob/main/LICENSE.txt)
![Activity](https://img.shields.io/github/commit-activity/y/ascopes/java-compiler-testing)

# java-compiler-testing

A framework for performing exhaustive integration testing against Java compilers in modern Java
libraries, with a focus on full JPMS support.

The _Java Compiler Testing_ API has a number of facilities for assisting in
testing anything related to the Java compiler. This includes Javac plugins and JSR-199 annotation
processors.

All test cases are designed to be as stateless as possible, with facilities to produce
in-memory file systems (using Google's JIMFS API) or using OS-provided temporary directories.
All file system mechanisms are complimented with a fluent API that enables writing expressive
declarations without unnecessary boilerplate.

Integration test cases can be written to cross-compile against a range of Java compiler
versions, with the ability to provide as much or as little configuration detail as you wish.
Additionally, APIs can be easily extended to integrate with any other JSR-199-compliant compiler
as required.

Compilation results are complimented with a suite of assertion facilities that extend
the AssertJ API to assist in writing fluent and human-readable test cases for your code. Each of
these assertions comes with specially-developed human-readable error messages and formatting.

Full JUnit5 integration is provided to help streamline the development process.

**This module is still under development.** Any contributions or feedback
are always welcome!

## Examples

### In-memory code, using RAM disks for source directories

```java
@DisplayName("Example tests")
class ExampleTest {

    @DisplayName("I can compile a Hello World application")
    @JavacCompilerTest
    void canCompileHelloWorld(JctCompiler<?, ?> compiler) {
        // Given
        var sources = newRamDirectory("src")
                .createFile("org/example/Message.java").withContents("""
                        package org.example;

                        import lombok.Data;
                        import lombok.NonNull;

                        @Data
                        public class Message {
                          private String content;

                          public static void main(String[] args) {
                            Message message = new Message("Hello, World!");
                            System.out.println(message);
                          }
                        }
                        """
                );

        // When
        var compilation = compiler
                .addSourcePath(sources)
                .compile();

        // Then
        assertThatCompilation(compilation)
                .isSuccessfulWithoutWarnings();

        assertThatCompilation(compilation)
                .classOutput().packages()
                .fileExists("com/example/Message.class")
                .isNotEmptyFile();
    }
}
```

### Compiling and testing with a custom annotation processor

```java
import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;

import org.example.processor.JsonSchemaAnnotationProcessor;
import org.skyscreamer.jsonassert.JSONAssert;

class JsonSchemaAnnotationProcessorTest {

    @JavacCompilerTest(minVersion = 11, maxVersion = 19)
    void theJsonSchemaIsCreatedFromTheInputCode(JctCompiler<?, ?> compiler) {
        // Given
        var sources = newRamDirectory("sources")
                .createDirectory("org", "example", "tests")
                .copyContentsFrom("src", "test", "resources", "code", "schematest");

        // When
        var compilation = compiler
                .addSources(sources)
                .addAnnotationProcessors(new JsonSchemaAnnotationProcessor())
                .addAnnotationProcessorOptions("jsonschema.verbose=true")
                .failOnWarnings(true)
                .showDeprecationWarnings(true)
                .compile();

        // Then
        assertThatCompilation(compilation)
                .isSuccessfulWithoutWarnings();

        assertThatCompilation(compilation)
                .diagnostics().notes().singleElement()
                .message().isEqualTo(
                        "Creating JSON schema in Java %s for package org.example.tests",
                        compiler.getRelease()
                );

        assertThatCompilation(compilation)
                .classOutputs().packages()
                .fileExists("json-schemas/UserSchema.json").contents()
                .isNotEmpty()
                .satisfies(contents -> JSONAssert.assertEquals(...));
    }
}
```

### Compiling multi-module sources

The following shows an example of compiling a multi-module style application with JPMS
support, running the Lombok annotation processor over the input. This assumes that the Lombok
JAR is already on the classpath for the JUnit test runner.

Even Maven lacks native support for this, still!

```java

@DisplayName("Example tests")
class ExampleTest {

    @DisplayName("I can compile a module that is using Lombok")
    @JavacCompilerTest(modules = true)
    void canCompileModuleUsingLombok(JctCompiler<?, ?> compiler) {
        // Given
        var sources = newRamDirectory("hello.world")
                .createFile("org/example/Message.java").withContents("""
                        package org.example;

                        import lombok.Data;
                        import lombok.NonNull;

                        @Data
                        public class Message {
                          @NonNull
                          private final String content;
                        }
                        """
                )
                .and().createFile("org/example/Main.java").withContents("""
                        package org.example;

                        public class Main {
                          public static void main(String[] args) {
                            for (var arg : args) {
                              var message = new Message(arg);
                              System.out.println(arg);
                            }
                          }
                        }
                        """
                )
                .and().createFile("module-info.java").withContents("""
                        module hello.world {
                          requires java.base;
                          requires static lombok;
                        }
                        """
                );

        // When
        var compilation = compiler
                .addModuleSourcePath("hello.world", sources)
                .compile();

        // Then
        assertThatCompilation(compilation)
                .isSuccessfulWithoutWarnings();

        assertThatCompilation(compilation)
                .classOutput().packages()
                .fileExists("com/example/Message.class")
                .isNotEmptyFile();
    }
}
```

## Features

- Implements in-memory file management compatible with the NIO Path and
  FileSystem API, enabling tests to run without write access to the host
  system, and without awkward resource-cleanup logic.
- Ability to run compilation on combinations of real files, class path
  resources, and in-memory files.
- Supports Java 9 JPMS modules as intended.
- Ability to customise a large assortment of configuration parameters
  to enable you to test exactly what you need to test.
- Provides support for `javac` out of the box, with the
  ability to support other JSR-199 implementations if desired --
  just make use of one of the compiler classes, or make your own!
- Implements a fully functional JSR-199 Path JavaFileManager.
- Fluent syntax for creating configurations, executing them, and
  inspecting the results.
- Integration with AssertJ for fluent assertions on compilation
  results.
- Ability to have multiple source roots, just like when using
  `javac` normally.
- Diagnostic reporting includes stacktraces, so you can find out
  exactly what triggered a diagnostic and begin debugging any
  issues in your applications quickly.
- Helpful error messages to assist in annotation processor development

```
[main] ERROR io.github.ascopes.jct.diagnostics.TracingDiagnosticListener - cannot find symbol
  symbol:   class Generated
  location: package javax.annotation
[main] INFO io.github.ascopes.jct.compilers.CompilationFactory - Compilation with compiler Javac 9 failed after ~332.3ms

java.lang.AssertionError: Expected a successful compilation, but it failed.

Diagnostics:

 - [ERROR] compiler.err.cant.resolve.location /sources-f1728706-5de5-4b89-9a6a-b51233ce67c8/io/github/ascopes/jct/examples/immutables/dataclass/ImmutableAnimal.java (at line 25, col 18)

   23 | @SuppressWarnings({"all"})
   24 | @ParametersAreNonnullByDefault
   25 | @javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
      +  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
   26 | @Immutable
   27 | @CheckReturnValue

  cannot find symbol
  symbol:   class Generated
  location: package javax.annotation
  
  at io.github.ascopes.jct.acceptancetests.immutables.ImmutablesIntegrationTest.immutablesValueProducesTheExpectedClass(ImmutablesIntegrationTest.java:66)
  at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
  at java.base/java.lang.reflect.Method.invoke(Method.java:577)
  ...
```

## ECJ support

While this module initially supported ECJ, there were a number of problems relating to bugs
and incorrect behaviour within ECJ's implementation. This prevented any form of module
support or generated source support within this library, since ECJ would not use the
standard JavaFileManager API consistently (often falling back to using FileInputStream objects
directly). In addition, many edge cases exist where binary names and paths are not handled
consistently either (such as using forward-slashes to delimit binary names).

To keep this library simpler and more consistent, as of
[!92](https://github.com/ascopes/java-compiler-testing/issues/92),
this support has been officially removed due to the overwhelming work it was creating.

If Eclipse fix the issues detailed in the above PR in a future release, supporting ECJ out
of the box in this library will be considered for inclusion. This may also include support
for the AspectJ compiler since this relies on ECJ internally.
