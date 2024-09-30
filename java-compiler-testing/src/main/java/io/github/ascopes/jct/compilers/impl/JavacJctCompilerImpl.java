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
package io.github.ascopes.jct.compilers.impl;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.compilers.AbstractJctCompiler;
import io.github.ascopes.jct.compilers.JctFlagBuilderFactory;
import io.github.ascopes.jct.compilers.Jsr199CompilerFactory;
import io.github.ascopes.jct.filemanagers.JctFileManagerFactory;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerFactoryImpl;
import javax.lang.model.SourceVersion;
import javax.tools.ToolProvider;

/**
 * Implementation of a JCT compiler that integrates with the default {@code javac}
 * compiler that is bundled with the current JDK.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JavacJctCompilerImpl extends AbstractJctCompiler {

  private static final int JAVA_8 = 8;
  private static final int JAVA_9 = 9;
  private static final int JAVA_20 = 20;
  private static final String NAME = "JDK Compiler";

  /**
   * Initialize a new Java compiler.
   */
  public JavacJctCompilerImpl() {
    super(NAME);
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getLatestSupportedVersionInt());
  }

  @Override
  public JctFlagBuilderFactory getFlagBuilderFactory() {
    return JavacJctFlagBuilderImpl::new;
  }

  @Override
  public Jsr199CompilerFactory getCompilerFactory() {
    // RequireNonNull to ensure the return result is non-null, since the ToolProvider
    // method is not annotated.
    return () -> requireNonNull(ToolProvider.getSystemJavaCompiler(), 
        "ToolProvider.getSystemJavaCompiler()");
  }

  @Override
  public JctFileManagerFactory getFileManagerFactory() {
    return new JctFileManagerFactoryImpl(this);
  }

  /**
   * Get the minimum version of Javac that is supported.
   *
   * @return the minimum supported version.
   * @since 1.0.0
   */
  public static int getEarliestSupportedVersionInt() {
    // Purposely do not hardcode members of the SourceVersion enum here other
    // than utility methods, as this prevents compilation problems on various
    // versions of the JDK when certain members are unavailable.

    var latestSupported = SourceVersion.latestSupported().ordinal();

    if (latestSupported >= JAVA_20) {
      // JDK 20 marks source-version 8 as obsolete, and emits compilation
      // warnings that may break tests using "fail on warnings". To avoid this,
      // disallow compiling Java 8 sources under Javac in JDK 20 or newer
      return JAVA_9;
    }

    return JAVA_8;
  }

  /**
   * Get the maximum version of Javac that is supported.
   *
   * @return the maximum supported version.
   * @since 1.0.0
   */
  public static int getLatestSupportedVersionInt() {
    return SourceVersion.latestSupported().ordinal();
  }
}
