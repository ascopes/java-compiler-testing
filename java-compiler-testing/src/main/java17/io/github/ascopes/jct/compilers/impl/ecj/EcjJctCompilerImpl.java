/*
 * Copyright (C) 2022 - 2024, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.compilers.impl.ecj;

import io.github.ascopes.jct.compilers.AbstractJctCompiler;
import io.github.ascopes.jct.compilers.JctFlagBuilderFactory;
import io.github.ascopes.jct.compilers.Jsr199CompilerFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;


/**
 * Implementation of a JCT compiler that integrates with the Eclipse Java Compiler.
 *
 * @author Ashley Scopes
 * @since TBC
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
