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

import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Factory for producing {@link JctCompilation} objects by performing a physical compilation with a
 * compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface JctCompilationFactory {

  /**
   * Create a compilation.
   *
   * @param flags          the flags to pass to the compiler.
   * @param fileManager    the file manager to use for file management.
   * @param jsr199Compiler the compiler backend to use.
   * @param classNames     the binary names of the classes to compile. If this is null, then classes
   *                       should be discovered automatically.
   * @return the compilation result that contains whether the compiler succeeded or failed, amongst
   *     other information.
   * @throws JctCompilerException if compiler raises an unhandled exception and cannot complete.
   */
  JctCompilation createCompilation(
      List<String> flags,
      JctFileManager fileManager,
      JavaCompiler jsr199Compiler,
      @Nullable Collection<String> classNames
  );
}
