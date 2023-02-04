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
package io.github.ascopes.jct.filemanagers.config;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurer for a file manager that applies the running JVM's module path to the file manager.
 *
 * <p>If module path inheritance is disabled in the compiler, then this will not run.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JctFileManagerJvmModulePathConfigurer implements JctFileManagerConfigurer {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JctFileManagerJvmModulePathConfigurer.class);

  private final JctCompiler<?, ?> compiler;

  /**
   * Initialise the configurer with the desired compiler.
   *
   * @param compiler the compiler to wrap.
   */
  public JctFileManagerJvmModulePathConfigurer(JctCompiler<?, ?> compiler) {
    this.compiler = compiler;
  }


  @Override
  public JctFileManager configure(JctFileManager fileManager) {
    LOGGER.debug("Configuring module path");

    SpecialLocationUtils
        .currentModulePathLocations()
        .stream()
        .peek(loc -> LOGGER.trace("Adding {} to file manager modulepath (inherited from JVM)", loc))
        .map(WrappingDirectoryImpl::new)
        .forEach(dir -> {
          // Since we do not know if the code being compiled will use modules or not just yet,
          // make sure any modules are on the class path as well so that they remain accessible
          // in unnamed modules.
          fileManager.addPath(StandardLocation.MODULE_PATH, dir);
          fileManager.addPath(StandardLocation.CLASS_PATH, dir);
        });

    return fileManager;
  }

  @Override
  public boolean isEnabled() {
    return compiler.isInheritModulePath();
  }
}
