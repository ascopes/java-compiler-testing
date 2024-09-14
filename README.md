![Java 11+](https://img.shields.io/badge/Java-11--23-brown?logo=openjdk&logoColor=white)
[![Build Status](https://github.com/ascopes/java-compiler-testing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/ascopes/java-compiler-testing/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ascopes.jct/java-compiler-testing)](https://repo1.maven.org/maven2/io/github/ascopes/jct/java-compiler-testing)
[![Code Coverage](https://codecov.io/gh/ascopes/java-compiler-testing/branch/main/graph/badge.svg?token=VT74BP2742)](https://codecov.io/gh/ascopes/java-compiler-testing)
[![javadoc (latest release)](https://javadoc.io/badge2/io.github.ascopes.jct/java-compiler-testing/javadoc.svg)](https://javadoc.io/doc/io.github.ascopes.jct/java-compiler-testing)
[![javadoc (main branch)](https://img.shields.io/badge/javadoc-latest--snapshot-darkgreen)](https://ascopes.github.io/java-compiler-testing)
[![Issues](https://img.shields.io/github/issues-raw/ascopes/java-compiler-testing?color=red)](https://github.com/ascopes/java-compiler-testing/issues)
[![License](https://img.shields.io/github/license/ascopes/java-compiler-testing?color=blueviolet)](https://github.com/ascopes/java-compiler-testing/blob/main/LICENSE.txt)
![Activity](https://img.shields.io/github/commit-activity/y/ascopes/java-compiler-testing)

# java-compiler-testing

A framework for performing exhaustive integration testing against Java compilers in modern Java
libraries, with a focus on full JPMS support.

The _Java Compiler Testing_ API has a number of facilities for assisting in
testing anything related to the Java compiler. This includes Javac plugins and JSR-199 annotation
processors.

All test cases are designed to be as stateless as possible, with facilities to produce
in-memory file systems or using OS-provided temporary directories.
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

## Features

- Implements in-memory file management compatible with the NIO Path and
  FileSystem API, enabling tests to run without write access to the host
  system, and without awkward resource-cleanup logic.
- Enables running compilations on combinations of real files, class path
  resources, in-memory files, JARs, WARs, EARs, ZIP files, etc.
- Null-safe API (using [JSpecify](https://jspecify.dev/)).
- [Tested on multiple existing frameworks](java-compiler-testing/src/it) including 
  Avaje, Spring, Lombok, MapStruct, ErrorProne, and CheckerFramework.
- Supports Java 9 JPMS modules.
- Ability to customise a large assortment of configuration parameters
  to enable you to test exactly what you need to test.
- Provides support for `javac` out of the box, with the
  ability to support other JSR-199 implementations if desired --
  just make use of one of the compiler classes, or make your own!
- Implements a fully functional JSR-199 Path JavaFileManager and class loading mechanism.
- Fluent syntax for creating configurations, executing them, and
  inspecting the results.
- Integration with [AssertJ](https://assertj.github.io/doc/)
  for fluent assertions on compilation results.
- Ability to have multiple source roots, just like when using
  `javac` normally.
- Diagnostic reporting includes stack traces, so you can find out
  exactly what triggered a diagnostic and begin debugging any
  issues in your applications quickly.

## Installation

The project can be found on Maven Central.

```xml
<dependency>
  <groupId>org.github.ascopes.jct</groupId>
  <artifactId>java-compiler-testing</artifactId>
  <version>${java-compiler-testing.version}</version>
</dependency>
```

If you are using Gradle, make sure you enable the Maven Central repositories
first, otherwise the dependency will not resolve.

```kotlin
repositories {
  mavenCentral()
}

dependencies {
  testImplementation("io.github.ascopes.jct:java-compiler-testing:$jctVersion")
}
```

## JPMS

If your tests make use of JPMS (i.e. they have a `module-info.java` somewhere), then you will want
to add a requirement for this module like so:

```java
module my.tests {
  ...
  requires org.assertj.core;
  requires io.github.ascopes.jct;
  ...
}
```

## JUnit5 integration

While this library provides JUnit5 support and extensions, it does not provide JUnit5 as a direct dependency.
You should ensure you have a working JUnit5 configuration in your project prior to including
this library. This includes `junit-jupiter` and `junit-jupiter-params`.

For Maven, it is also suggested to ensure you are using `maven-surefire-plugin` (or `maven-failsafe-plugin`)
on version 3.0.0-M1 or newer. This ensures that JPMS is handled correctly.

If making use of JPMS, you should include the following in your `module-info.java`:

```java
open module my.tests {
  ...
  requires org.assertj.core;
  requires io.github.ascopes.jct;
  requires transitive org.junit.jupiter;
  requires org.junit.jupiter.params;
  ...
}
```

## Examples

### In-memory code, using RAM disks for source directories

```java

@DisplayName("Example tests")
@ExtendWith(JctExtension.class)
class ExampleTest {
  
  @Managed
  Workspace workspace;

  @DisplayName("I can compile a Hello World application")
  @JavacCompilerTest
  void canCompileHelloWorld(JctCompiler compiler) {
    // Given
    workspace
        .createSourcePathPackage()
        .createFile("org/example/HelloWorld.java").withContents("""
            package org.example;
                
            @Data
            public class HelloWorld {
              public static void main(String[] args) {
                System.out.println("Hello, World!");
              }
            }
            """
        );

    // When
    var compilation = compiler.compile(workspace);

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();

    assertThatCompilation(compilation)
        .classOutputPackages()
        .fileExists("org/example/HelloWorld.class")
        .isNotEmptyFile();
  }
}
```

### Compiling and testing with a custom annotation processor

```java
import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.junit.JctExtension;
import io.github.ascopes.jct.junit.Managed;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.example.processor.JsonSchemaAnnotationProcessor;
import org.skyscreamer.jsonassert.JSONAssert;

@ExtendWith(JctExtension.class)
class JsonSchemaAnnotationProcessorTest {
  
  @Managed
  Workspace workspace;

  @JavacCompilerTest(minVersion = 11, maxVersion = 21)
  void theJsonSchemaIsCreatedFromTheInputCode(JctCompiler compiler) {
    // Given
    workspace
        .createSourcePathPackage()
        .createDirectory("org", "example", "tests")
        .copyContentsFrom("src", "test", "resources", "code", "schematest");

    // When
    var compilation = compiler
        .addAnnotationProcessors(new JsonSchemaAnnotationProcessor())
        .addAnnotationProcessorOptions("jsonschema.verbose=true")
        .failOnWarnings(true)
        .showDeprecationWarnings(true)
        .compile(workspace);

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
        .classOutputPackages()
        .fileExists("json-schemas", "UserSchema.json").contents()
        .isNotEmpty()
        .satisfies(contents -> JSONAssert.assertEquals(...));
  }
}
```

### Compiling multi-module sources

The following shows an example of compiling a multi-module style application with JPMS
support, running the Lombok annotation processor over the input. This assumes that the Lombok
JAR is already on the classpath for the JUnit test runner (e.g. is a test dependency in your
project).

You will want to make sure you do not attempt to target anything older than Java 9 in this case, since
module support was only introduced in Java 9.

```java

@DisplayName("Example tests")
class ExampleTest {

  @DisplayName("I can compile a module that is using Lombok")
  @JavacCompilerTest(minVersion = 9)
  void canCompileModuleUsingLombok(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathModule("hello.world")
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
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutputPackages()
          .fileExists("org/example/Message.class")
          .isNotEmptyFile();
    }
  }
}
```

## Tips to improve build speeds

Running a Java Compiler and maintaining a virtual file system is not the simplest
thing to achieve internally. As a result, some tests may take a few hundred milliseconds
to execute in some cases, especially when the JVM is warming up. There are a few things
you can do to ensure that tests are as snappy as possible, however.

### Parallel test runners

JUnit5 has the ability to configure tests to run in parallel.

You can configure this by creating a file in your test resources named
`junit-platform.properties` and add the following content to it.

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.classes.default=SAME_THREAD
junit.jupiter.execution.parallel.mode.default=CONCURRENT
```

Within build systems like Maven, you can also enable your builds to run
in parallel where desired. In the case of Maven, you can pass `-T1C` on the
command line to make it run one parallel operation on each CPU core.

### JVM flags

You can provide JVM flags to the runtime that executes your test packs. For Maven Surefire,
you do this by defining an `<argLine/>` attribute in your `<properties/>` with a string value
holding the flags you wish to use.

1. Enforce level-1 tiered compilation - this will prevent the JVM wasting time performing
   more complicated JIT compilation passes over your code when it usually does not provide
   much benefit on short-lived code. You can pass 
   `-XX:+TieredCompilation -XX:TieredStopAtLevel=1` to set this up. Enabling this in the JCT
   builds reduced the overall build time by around 20 seconds.
2. Use the ZGC - the ZGC will reduce lag when performing garbage collection on code that
   has a high churn of objects. On Java 11, the ZGC is an experimental feature, which needs
   to be enabled with `-XX:+UnlockExperimentalOptions -XX:+UseZGC`. On Java 17, you just
   need to pass `-XX:+UseZGC` alone.

## Third-party compiler support

The base classes to provide third party compiler integrations are made public in this
API so you can extend them. 

To integrate with your chosen JSR-199 compatible compiler, extend the [AbstractJctCompiler](https://ascopes.github.io/java-compiler-testing/io.github.ascopes.jct/io/github/ascopes/jct/compilers/AbstractJctCompiler.html) 
class and override anything you need to tweak. 

The call to `compile` will return a [JctCompilation](https://ascopes.github.io/java-compiler-testing/io.github.ascopes.jct/io/github/ascopes/jct/compilers/JctCompilation.html)
object. This is already defined for you. All you need to provide in your compiler class is:

- A [Jsr199CompilerFactory](https://ascopes.github.io/java-compiler-testing/io.github.ascopes.jct/io/github/ascopes/jct/compilers/AbstractJctCompiler.html#getCompilerFactory()) to provide JavaCompiler objects.
- A [default release](https://ascopes.github.io/java-compiler-testing/io.github.ascopes.jct/io/github/ascopes/jct/compilers/AbstractJctCompiler.html#getDefaultRelease()) that defines the default language version to use unless overridden.
- A [JctFlagBuilderFactory](https://ascopes.github.io/java-compiler-testing/io.github.ascopes.jct/io/github/ascopes/jct/compilers/AbstractJctCompiler.html#getFlagBuilderFactory()) to translate the compiler configuration to command-line arguments.

### ECJ (Eclipse Java Compiler)

While ECJ supports the same interfaces as Javac that are used to call the compiler from this library, [eclipse-jdt/eclipse.jdt.core#1153](https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1153) means we will not get functional Java 11 support going forwards. 

A number of issues were found while developing  [ascopes/java-compiler-testing#163](https://github.com/ascopes/java-compiler-testing/issues/163) with ECJ which prevents many features such as JPMS support from working correctly 
([eclipse-jdt/eclipse.jdt.core#958](https://github.com/eclipse-jdt/eclipse.jdt.core/issues/958)).

Since it is unlikely these issues will be addressed in the near future, support for ECJ has been shelved for the foreseeable future.
