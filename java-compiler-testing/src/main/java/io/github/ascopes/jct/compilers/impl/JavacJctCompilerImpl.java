/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Implementation of a {@code javac} compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JavacJctCompilerImpl extends AbstractJctCompiler<JavacJctCompilerImpl> {

  private static final String NAME = "JDK Compiler";

  /**
   * Initialize a new Java compiler.
   */
  public JavacJctCompilerImpl() {
    super(NAME);
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getLatestSupportedVersionInt(false));
  }

  @Override
  public JctFlagBuilderFactory getFlagBuilderFactory() {
    return JavacJctFlagBuilderImpl::new;
  }

  @Override
  public Jsr199CompilerFactory getCompilerFactory() {
    // RequireNonNull to ensure the return result is non-null, since the ToolProvider
    // method is not annotated.
    return () -> requireNonNull(ToolProvider.getSystemJavaCompiler());
  }

  @Override
  public JctFileManagerFactory getFileManagerFactory() {
    return new JctFileManagerFactoryImpl(this);
  }

  /**
   * Get the minimum version of Javac that is supported.
   *
   * <p>Note, once Java 8 reaches the end of the EOL support window,
   * the {@code modules} parameter will be ignored and deprecated in a future release, instead
   * always defaulting to {@code true}.
   *
   * @param modules whether modules need to be supported or not.
   * @return the minimum supported version.
   */
  public static int getEarliestSupportedVersionInt(boolean modules) {
    // Purposely do not hardcode members of the SourceVersion enum here other
    // than utility methods, as this prevents compilation problems on various
    // versions of the JDK when certain members are unavailable.

    var latestSupported = SourceVersion.latestSupported().ordinal();

    //noinspection NonStrictComparisonCanBeEquality
    if (latestSupported >= 20) {
      // JDK 20 marks source-version 8 as obsolete, and emits compilation
      // warnings that may break tests using "fail on warnings". To avoid this,
      // disallow compiling Java 8 sources under Javac in JDK 20 or newer
      return 9;
    }

    // Anything below Java 20 allows Java 9 as the minimum for JPMS support,
    // or Java 8 for non-JPMS compilations.
    if (modules) {
      return 9;
    }

    return 8;
  }

  /**
   * Get the maximum version of Javac that is supported.
   *
   * <p>Note, once Java 8 reaches the end of the EOL support window,
   * the {@code modules} parameter will be ignored and deprecated in a future release, instead
   * always defaulting to {@code true}.
   *
   * @param modules whether to require module support or not. This is currently ignored but exists
   *                for future compatibility purposes.
   * @return the maximum supported version.
   */
  public static int getLatestSupportedVersionInt(@SuppressWarnings("unused") boolean modules) {
    return SourceVersion.latestSupported().ordinal();
  }
}
