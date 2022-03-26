module com.github.ascopes.jct {
  requires java.base;
  requires java.compiler;
  requires java.management;

  requires ecj;
  requires jimfs;
  requires org.assertj.core;
  requires org.reflections;
  requires org.slf4j;

  exports com.github.ascopes.jct.assertions;
  exports com.github.ascopes.jct.compilations;
  exports com.github.ascopes.jct.compilers;
  exports com.github.ascopes.jct.diagnostics;
  exports com.github.ascopes.jct.paths;

  exports com.github.ascopes.jct.intern to com.github.ascopes.jct.testing;
}