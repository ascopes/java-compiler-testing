package io.github.ascopes.jct.compilers.impl;

import io.github.ascopes.jct.compilers.AbstractJctCompiler;
import io.github.ascopes.jct.compilers.JctFlagBuilderFactory;
import io.github.ascopes.jct.compilers.Jsr199CompilerFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;


/**
 * Implementation of a JCT compiler that integrates with the Eclipse Java Compiler.
 *
 * @author Ashley Scopes
 * @since 5.0.0
 */
public final class EcjJctCompilerImpl extends AbstractJctCompiler {

  public EcjJctCompilerImpl() {
    super("ECJ");
  }

  @Override
  public JctFlagBuilderFactory getFlagBuilderFactory() {
    return EcjJctFlagBuilderImpl::new;
  }

  @Override
  public Jsr199CompilerFactory getCompilerFactory() {
    return EclipseCompiler::new;
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getLatestSupportedVersionInt());
  }

  /**
   * Get the minimum version of ECJ that is supported.
   *
   * @return the minimum supported version.
   */
  public static int getEarliestSupportedVersionInt() {
    return decodeMajorVersion(ClassFileConstants.JDK1_8);
  }

  /**
   * Get the maximum version of ECJ that is supported.
   *
   * @return the maximum supported version.
   */
  public static int getLatestSupportedVersionInt() {
    return decodeMajorVersion(ClassFileConstants.getLatestJDKLevel());
  }

  private static int decodeMajorVersion(long classFileConstant) {
    return (int) ((classFileConstant >> 16L) - ClassFileConstants.MAJOR_VERSION_0);
  }
}
