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

package com.github.ascopes.jct.compilers.javac;

import com.github.ascopes.jct.compilers.FlagBuilder;
import com.github.ascopes.jct.compilers.SimpleAbstractCompiler;
import java.util.function.Supplier;
import javax.tools.JavaCompiler;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Implementation of a {@code javac} compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JavacCompiler extends SimpleAbstractCompiler<JavacCompiler> {

  private JavacCompiler(Supplier<JavaCompiler> compilerSupplier) {
    super(compilerSupplier);
  }

  @Override
  protected String getName() {
    return "javac";
  }

  @Override
  protected FlagBuilder createFlagBuilder() {
    return new JavacFlagBuilder();
  }

  /**
   * Initialize this compiler.
   *
   * @param compilerSupplier the supplier of new underlying JSR-199 compiler instances to use.
   */
  public static JavacCompiler using(Supplier<JavaCompiler> compilerSupplier) {
    return new JavacCompiler(compilerSupplier);
  }
}
