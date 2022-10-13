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

import io.github.ascopes.jct.compilers.Compiler;
import io.github.ascopes.jct.compilers.FileManagerBuilder;
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
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class JavacCompiler extends Compiler<JavacCompiler> {

  private static final String NAME = "JDK Compiler";


  /**
   * Initialize a new Java compiler.
   */
  public JavacCompiler() {
    this(ToolProvider.getSystemJavaCompiler());
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public JavacCompiler(JavaCompiler jsr199Compiler) {
    this(NAME, jsr199Compiler);
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param name the name to give the compiler.
   */
  public JavacCompiler(String name) {
    this(name, ToolProvider.getSystemJavaCompiler());
  }

  /**
   * Initialize a new Java compiler.
   *
   * @param name           the name to give the compiler.
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public JavacCompiler(String name, JavaCompiler jsr199Compiler) {
    super(name, new FileManagerBuilder(), jsr199Compiler, new JavacFlagBuilder());
    addCompilerOptions("-implicit:class");
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getMaxVersion());
  }

  /**
   * Get the maximum version of Javac that is supported.
   */
  public static int getMaxVersion() {
    return SourceVersion.latestSupported().ordinal();
  }
}
