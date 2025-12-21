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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerFactoryImpl;

/**
 * Helpers to create instances of default implementations for file managers.
 *
 * @author Ashley Scopes
 * @since 1.1.0
 */
public final class JctFileManagers {

  private JctFileManagers() {
    // Static-only class.
  }

  /**
   * Create a new default implementation of a file manager factory.
   *
   * @param compiler the JctCompiler to bind any file managers to.
   * @return the file manager factory instance.
   */
  public static JctFileManagerFactory newJctFileManagerFactory(JctCompiler compiler) {
    return new JctFileManagerFactoryImpl(compiler);
  }
}
