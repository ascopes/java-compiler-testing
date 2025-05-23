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

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.workspaces.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurer for a file manager that applies the given workspace.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JctFileManagerWorkspaceConfigurer
    implements JctFileManagerConfigurer {

  private static final Logger log
      = LoggerFactory.getLogger(JctFileManagerWorkspaceConfigurer.class);

  private final Workspace workspace;

  /**
   * Initialise the configurer with the desired workspace.
   *
   * @param workspace the workspace to wrap.
   */
  public JctFileManagerWorkspaceConfigurer(Workspace workspace) {
    this.workspace = workspace;
  }

  @Override
  public JctFileManager configure(JctFileManager fileManager) {
    log.debug("Configuring file manager with user-provided paths");

    workspace.getAllPaths().forEach((location, paths) -> {
      log
          .atTrace()
          .setMessage("Adding paths from workspace location {} into file manager ({})")
          .addArgument(() -> StringUtils.quoted(location.getName()))
          .addArgument(() -> StringUtils.quotedIterable(paths))
          .log();
      fileManager.addPaths(location, paths);
    });

    return fileManager;
  }
}
