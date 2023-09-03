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
package io.github.ascopes.jct.filemanagers.impl;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.JctFileManagerFactory;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerAnnotationProcessorClassPathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerConfigurerChain;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmClassPathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmClassPathModuleConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmModulePathConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerJvmSystemModulesConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerLoggingProxyConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerRequiredLocationsConfigurer;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerWorkspaceConfigurer;
import io.github.ascopes.jct.utils.VisibleForTestingOnly;
import io.github.ascopes.jct.workspaces.Workspace;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Basic implementation for a file manager factory that returns a {@link JctFileManagerImpl}
 * instance.
 *
 * <p>This implementation binds to a given {@link JctCompiler} object on construction to enable
 * potential reuse.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JctFileManagerFactoryImpl implements JctFileManagerFactory {

  private final JctCompiler compiler;

  /**
   * Initialise this factory.
   *
   * @param compiler the compiler to pull configuration details from.
   */
  public JctFileManagerFactoryImpl(JctCompiler compiler) {
    this.compiler = compiler;
  }

  /**
   * Get the compiler that was set on this file manager factory.
   *
   * @return the compiler
   * @since 1.1.0
   */
  @VisibleForTestingOnly
  public JctCompiler getCompiler() {
    return compiler;
  }

  @Override
  public JctFileManager createFileManager(Workspace workspace) {
    var release = compiler.getEffectiveRelease();
    var fileManager = new JctFileManagerImpl(release);
    return createConfigurerChain(workspace)
        .configure(fileManager);
  }

  /**
   * Create the default configurer chain to use for the given workspace.
   *
   * <p>This is visible for testing only.
   *
   * @param workspace the workspace to configure with.
   * @return the chain to use.
   */
  @VisibleForTestingOnly
  public JctFileManagerConfigurerChain createConfigurerChain(Workspace workspace) {
    // The order here is important. Do not adjust it without testing extensively first!
    return new JctFileManagerConfigurerChain()
        .addLast(new JctFileManagerWorkspaceConfigurer(workspace))
        .addLast(new JctFileManagerJvmClassPathConfigurer(compiler))
        .addLast(new JctFileManagerJvmClassPathModuleConfigurer(compiler))
        .addLast(new JctFileManagerJvmModulePathConfigurer(compiler))
        .addLast(new JctFileManagerJvmSystemModulesConfigurer(compiler))
        .addLast(new JctFileManagerAnnotationProcessorClassPathConfigurer(compiler))
        .addLast(new JctFileManagerRequiredLocationsConfigurer(workspace))
        .addLast(new JctFileManagerLoggingProxyConfigurer(compiler));
  }
}
