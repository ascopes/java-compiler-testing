# java-compiler-testing
Facility for running compilation tests and annotation processing tests
within unit tests, for javac and ecj.

I developed this after several months of pulling out my hair trying to
find easy ways to integration test annotation processors for Java. While
one or two tools exist for Java 8, I have yet to find one that works
seamlessly with Java 11 and newer.

This module comes with full JPMS support. I decided to drop support for
Java 8 due to complexity around implementing this without the ability to
reference modules, and ideally this tool should be forward compatible to
prevent future issues for any projects deciding to use it.

This module is still under development. Any contributions or feedback
are always welcome!
