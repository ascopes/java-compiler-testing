/*
 * Copyright (C) 2022 Ashley Scopes
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

package com.github.ascopes.jct.compilers;

import com.github.ascopes.jct.compilers.impl.CompilerImpl;
import com.github.ascopes.jct.compilers.impl.EcjFlagBuilder;
import com.github.ascopes.jct.compilers.impl.JavacFlagBuilder;
import javax.tools.ToolProvider;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * Utility class that allows initialization of several common types of compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class Compilers {

  private Compilers() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Create an instance of the JDK-provided compiler.
   *
   * @return the JDK-provided compiler instance.
   */
  public static Compiler<?, ?> javac() {
    return new CompilerImpl(
        "javac",
        ToolProvider.getSystemJavaCompiler(),
        JavacFlagBuilder::new
    );
  }

  /**
   * Create an instance of the Eclipse Compiler for Java.
   *
   * <p>This is bundled with this toolkit.
   *
   * <p><strong>Note:</strong> the ECJ implementation does not currently work correctly with
   * JPMS modules.
   *
   * @return the ECJ instance.
   */
  public static Compiler<?, ?> ecj() {
    return new CompilerImpl(
        "ecj",
        new EclipseCompiler(),
        EcjFlagBuilder::new
    );
  }
}
