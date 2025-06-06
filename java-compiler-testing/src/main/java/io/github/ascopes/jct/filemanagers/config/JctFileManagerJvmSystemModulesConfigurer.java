/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import javax.tools.StandardLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurer for a file manager that applies the running JVM's system modules to the file manager.
 *
 * <p>If system module inheritance is disabled in the compiler, then this will not run.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JctFileManagerJvmSystemModulesConfigurer
    implements JctFileManagerConfigurer {

  private static final Logger log = LoggerFactory
      .getLogger(JctFileManagerJvmSystemModulesConfigurer.class);

  private final JctCompiler compiler;

  /**
   * Initialise the configurer with the desired compiler.
   *
   * @param compiler the compiler to wrap.
   */
  public JctFileManagerJvmSystemModulesConfigurer(JctCompiler compiler) {
    this.compiler = compiler;
  }

  @Override
  public JctFileManager configure(JctFileManager fileManager) {
    log.debug("Configuring JVM system modules path");

    SpecialLocationUtils
        .javaRuntimeLocations()
        .stream()
        .peek(loc -> log
            .atTrace()
            .setMessage("Adding {} ({}) to file manager system modules path (inherited from JVM))")
            .addArgument(() -> StringUtils.quoted(loc.toAbsolutePath()))
            .addArgument(() -> StringUtils.quoted(loc.toUri()))
            .log())
        .map(WrappingDirectoryImpl::new)
        .forEach(dir -> fileManager.addPath(StandardLocation.SYSTEM_MODULES, dir));

    return fileManager;
  }

  @Override
  public boolean isEnabled() {
    return compiler.isInheritSystemModulePath();
  }
}
