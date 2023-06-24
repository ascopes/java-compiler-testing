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
package io.github.ascopes.jct.compilers;

import io.github.ascopes.jct.compilers.impl.EcjJctCompilerImpl;
import io.github.ascopes.jct.compilers.impl.JavacJctCompilerImpl;
import io.github.ascopes.jct.utils.UtilityClass;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helpers to create new compiler instances.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JctCompilers extends UtilityClass {

  private JctCompilers() {
    // Static-only class.
  }

  /**
   * Create a new instance of the default platform compiler that is part of the JDK ({@code javac}
   * on OpenJDK-derived implementations).
   *
   * @return the compiler instance.
   * @since 0.2.0
   */
  @API(status = Status.STABLE, since = "0.2.0")
  public static JctCompiler newPlatformCompiler() {
    return new JavacJctCompilerImpl();
  }

  /**
   * Create a new instance of an ECJ compiler.
   *
   * <p>Note that this is highly experimental, and may break in strange and unexpected ways, or may
   * even be removed without notice. Use at your own risk.
   *
   * @return the compiler instance.
   * @since TBC
   */
  @API(status = Status.EXPERIMENTAL, since = "TBC")
  public static JctCompiler newEcjCompiler() {
    return new EcjJctCompilerImpl();
  }
}
