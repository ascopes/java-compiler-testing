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
package io.github.ascopes.jct.compilers.javac;

import io.github.ascopes.jct.compilers.AbstractJctCompiler;
import javax.annotation.concurrent.NotThreadSafe;
import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
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
@NotThreadSafe
public final class JavacJctCompilerImpl extends AbstractJctCompiler<JavacJctCompilerImpl> {

  private static final String NAME = "JDK Compiler";

  /**
   * Initialize a new Java compiler.
   */
  public JavacJctCompilerImpl() {
    this(ToolProvider.getSystemJavaCompiler());
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public JavacJctCompilerImpl(JavaCompiler jsr199Compiler) {
    this(NAME, jsr199Compiler);
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param name the name to give the compiler.
   */
  public JavacJctCompilerImpl(String name) {
    this(name, ToolProvider.getSystemJavaCompiler());
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param name           the name to give the compiler.
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public JavacJctCompilerImpl(String name, JavaCompiler jsr199Compiler) {
    super(name, jsr199Compiler, new JavacJctFlagBuilderImpl());
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getLatestSupportedVersionInt(false));
  }

  /**
   * Get the minimum version of Javac that is supported.
   *
   * <p>Note, once Java 8 reaches the end of the EOL support window,
   * the {@code modules} parameter will be ignored and deprecated in
   * a future release, instead always defaulting to {@code true}.
   *
   * @param modules whether modules need to be supported or not.
   * @return the minimum supported version.
   */
  @SuppressWarnings("ConstantConditions")
  public static int getEarliestSupportedVersionInt(boolean modules) {
    // Purposely do not hardcode members of the SourceVersion enum here other
    // than utility methods, as this prevents compilation problems on various
    // versions of the JDK when certain members are unavailable.

    var latestSupported = SourceVersion.latestSupported().ordinal();

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
   * the {@code modules} parameter will be ignored and deprecated in
   * a future release, instead always defaulting to {@code true}.
   *
   * @param modules whether to require module support or not. This is currently ignored but exists
   *                for future compatibility purposes.
   * @return the maximum supported version.
   */
  public static int getLatestSupportedVersionInt(@SuppressWarnings("unused") boolean modules) {
    return SourceVersion.latestSupported().ordinal();
  }
}
