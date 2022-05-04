# Known issues with ECJ

The following lists some known issues using ECJ that may be worth
providing as feedback to Eclipse at some point. Many of these points
differ from the behaviour of the reference implementation of javac.

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
