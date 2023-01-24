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

import static java.util.function.Predicate.not;

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.workspaces.Workspace;
import java.util.Set;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurer for a file manager that creates missing required locations to the file manager.
 *
 * <p>These locations will be created as empty paths in the workspace.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@Immutable
@ThreadSafe
public final class JctFileManagerRequiredLocationsConfigurer implements JctFileManagerConfigurer {

  private static final Logger LOGGER
      = LoggerFactory.getLogger(JctFileManagerRequiredLocationsConfigurer.class);

  // Locations that we have to ensure exist before the compiler is run.
  private static final Set<StandardLocation> REQUIRED_LOCATIONS = Set.of(
      // We have to manually create this one as javac will not attempt to access it lazily. Instead,
      // it will just abort if it is not present. This means we cannot take advantage of the
      // container group creating the roots as we try to access them for this specific case.
      StandardLocation.SOURCE_OUTPUT,
      // Annotation processors that create files will need this directory to exist if it is to
      // work properly.
      StandardLocation.CLASS_OUTPUT,
      // We need to provide a header output path in case header generation is enabled at any stage.
      // I might make this disabled by default in the future if there is too much overhead from
      // doing this by default.
      StandardLocation.NATIVE_HEADER_OUTPUT
  );

  private final Workspace workspace;

  /**
   * Initialise this configurer.
   *
   * @param workspace the workspace to bind to.
   */
  public JctFileManagerRequiredLocationsConfigurer(@WillNotClose Workspace workspace) {
    this.workspace = workspace;
  }

  @Override
  public JctFileManager configure(@WillNotClose JctFileManager fileManager) {
    LOGGER.debug("Configuring required locations that do not yet exist");

    REQUIRED_LOCATIONS
        .stream()
        .filter(not(fileManager::hasLocation))
        .forEach(location -> {
          LOGGER.trace(
              "Required location {} does not exist, so will be created in the workspace",
              location
          );
          fileManager.addPath(location, workspace.createPackage(location));
        });
    return fileManager;
  }
}
