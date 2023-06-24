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
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import org.apiguardian.api.API;

/**
 * A file manager factory that is used for ECJ compilers. This wraps a regular
 * {@link JctFileManagerFactory}, modifying the result slightly.
 *
 * @author Ashley Scopes
 * @since TBC
 */
@API(since = "TBC", status = API.Status.INTERNAL)
public final class EcjJctFileManagerFactoryImpl implements JctFileManagerFactory {

  private final JctFileManagerFactory factory;

  /**
   * Initialise this factory.
   *
   * @param compiler the compiler to use.
   */
  public EcjJctFileManagerFactoryImpl(JctCompiler compiler) {
    factory = new JctFileManagerFactoryImpl(compiler);
  }

  @Override
  public JctFileManager createFileManager(Workspace workspace) {
    // ECJ uses the java.io.File API in arbitrary places, which means RAM_DIRECTORIES will fail
    // to perform lookups in certain places. Therefore, we have to enforce that we only use
    // workspaces that reside in the root file system.
    if (workspace.getPathStrategy() != PathStrategy.TEMP_DIRECTORIES) {
      throw new IllegalArgumentException(
          "The ECJ compiler only supports the TEMP_DIRECTORIES path strategy. " +
              "Specify this explicitly when you create the workspace to fix this."
      );
    }

    var fileManager = factory.createFileManager(workspace);

    // Wrap the result in the EcjJctFileManagerImpl to mitigate incorrect API usage from ECJ.
    return new EcjJctFileManagerImpl(fileManager);
  }
}
