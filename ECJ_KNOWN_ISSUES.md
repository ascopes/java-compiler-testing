# Known issues with ECJ

The following lists some known issues using ECJ that may be worth
providing as feedback to Eclipse at some point. Many of these points
differ from the behaviour of the reference implementation of javac.

## ECJ passes class names using slash-delimited paths rather than binary names

The OpenJDK javac implementation consistently uses binary names (e.g. `foo.bar.Baz`) for classes
when reading and writing with `JavaFileManager` objects.

The ECJ implementation instead sometimes passes around files with slashes instead
(e.g. `foo/bar/Baz`), which has to then have custom handling to enforce consistent behaviour.


## ECJ attempts to read generated sources from the root filesystem

The `org.eclipse.jdt.internal.compiler.parser.Parser` class has a method with signature
`parse(ICompilationUnit, CompilationResult, int, int)` which attempts to invoke the following code
on any generated sources:

```java
char[] contents;
try {
  contents = this.readManager != null ? this.readManager.getContents(sourceUnit) : sourceUnit.getContents();
} catch (AbortCompilationUnit var11) {
  this.problemReporter().cannotReadSource(this.compilationUnit, var11, this.options.verbose);
  contents = CharOperation.NO_CHAR;
}
```

It would appear that for reading generated sources, the `readManager` field is `null`, so the
fallback is called (`sourceUnit.getContents()`).

Inspecting the implementation of `sourceUnit.getContents()`, we find the following:

```java
public char[] getContents() {
  if (this.contents != null) {
    return this.contents;
  } else {
    try {
      return Util.getFileCharContent(new File(new String(this.fileName)), this.encoding);
    } catch (IOException var2) {
      this.contents = CharOperation.NO_CHAR;
      throw new AbortCompilationUnit((CompilationResult)null, var2, this.encoding);
    }
  }
}
```

...which subsequently calls `Utils.getFileCharContent`. This static method attempts to read the
file from a `FileInputStream` which will only consider the root default file system on the machine
the JVM is running on. This makes it impossible to read generated sources for recompilation with
ECJ.

Interestingly this limitation doesn't appear to exist for other resource types, and ECJ will
correctly write the generated sources using the `JavaFileManager` API first.

## Multi-module projects do not compile correctly

Observed this issue when invoking both ECJ from this framework, and
when trying to compile a multi-module example project from IntelliJ
with the ECJ compiler enabled.

For some reason, the packages do not resolve correctly when in a
nested module directory, such as:

```java
module com.example {
  exports com.example;
}
```

The following error gets reported:

```
ERROR in module-info.java (at line 3)
exports com.example;
        ^^^^^^^^^^^
The package com.example does not exist or is empty
```

## ECJ requires a `StandardLocation.CLASS_PATH` even if it is not used

If we do not provide the `StandardLocation.CLASS_PATH` location as being present
by default, ECJ will fail to run because this is missing. Javac does not have this
same behaviour.

## ECJ is not fully JPMS-compatible

ECJ does not define a `module-info.class` or provide an `Automatic-Module-Name` entry in the
`MANIFEST.mf`, meaning the module name for ECJ may be subject to change if the JAR name changes
as well.
