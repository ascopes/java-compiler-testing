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
package io.github.ascopes.jct.compilers.ecj;

import io.github.ascopes.jct.compilers.SimpleCompiler;
import io.github.ascopes.jct.compilers.SimpleFileManagerTemplate;
import javax.tools.JavaCompiler;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * Implementation of an ECJ compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class EcjCompiler extends SimpleCompiler<EcjCompiler> {

  private static final String NAME = "Eclipse Compiler for Java";

  /**
   * Initialize a new ECJ compiler.
   */
  public EcjCompiler() {
    this(new EclipseCompiler());
  }

  /**
   * Initialize a new ECJ compiler.
   *
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public EcjCompiler(JavaCompiler jsr199Compiler) {
    this(NAME, jsr199Compiler);
  }

  /**
   * Initialize a new ECJ compiler.
   *
   * @param name the name to give the compiler.
   */
  public EcjCompiler(String name) {
    super(name, new SimpleFileManagerTemplate(), new EclipseCompiler(), new EcjFlagBuilder());
  }

  /**
   * Initialize a new ECJ compiler.
   *
   * @param name           the name to give the compiler.
   * @param jsr199Compiler the JSR-199 compiler backend to use.
   */
  public EcjCompiler(String name, JavaCompiler jsr199Compiler) {
    super(name, new SimpleFileManagerTemplate(), jsr199Compiler, new EcjFlagBuilder());
  }

  @Override
  public String getDefaultRelease() {
    return Integer.toString(getMaxVersion());
  }

  /**
   * Get the maximum version of ECJ that is supported.
   */
  public static int getMaxVersion() {
    var version = (ClassFileConstants.getLatestJDKLevel() >> (Short.BYTES * 8))
        - ClassFileConstants.MAJOR_VERSION_0;

    return (int) version;
  }
}
