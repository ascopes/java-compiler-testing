package com.github.ascopes.jct.compilers;

import javax.tools.ToolProvider;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * Utility class that allows initialization of several common types of compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class Compilers {

  private Compilers() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Create an instance of the JDK-provided compiler.
   *
   * @return the JDK-provided compiler instance.
   */
  public static StandardCompiler javac() {
    return new StandardCompiler("javac", ToolProvider.getSystemJavaCompiler());
  }

  /**
   * Create an instance of the Eclipse Compiler for Java.
   *
   * <p>This is bundled with this toolkit.
   *
   * @return the ECJ instance.
   */
  public static StandardCompiler ecj() {
    return new StandardCompiler("ecj", new EclipseCompiler());
  }
}
