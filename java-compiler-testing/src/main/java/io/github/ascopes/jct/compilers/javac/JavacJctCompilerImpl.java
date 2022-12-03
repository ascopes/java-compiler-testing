/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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

import io.github.ascopes.jct.compilers.impl.AbstractJctCompiler;
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
    addCompilerOptions("-implicit:class");
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getLatestSupportedVersionInt(false));
  }

  /**
   * Get the minimum version of Javac that is supported.
   *
   * @param modules whether modules need to be supported or not.
   * @return the minimum supported version.
   */
  public static int getEarliestSupportedVersionInt(boolean modules) {
    // Currently we set a hard limit on Java 8 for non-modules and Java 9 for modules.
    // In future implementations of the JDK, however, this will change to support Java 9 or later
    // as a minimum version. When this eventually does happen, this return value may need further
    // logic behind it to calculate the right behaviour.
    return modules
        ? SourceVersion.RELEASE_9.ordinal()
        : SourceVersion.RELEASE_8.ordinal();
  }

  /**
   * Get the maximum version of Javac that is supported.
   *
   * @param modules whether to require module support or not. This is currently ignored but exists
   *                for future compatibility purposes.
   * @return the maximum supported version.
   */
  public static int getLatestSupportedVersionInt(@SuppressWarnings("unused") boolean modules) {
    return SourceVersion.latestSupported().ordinal();
  }
}
