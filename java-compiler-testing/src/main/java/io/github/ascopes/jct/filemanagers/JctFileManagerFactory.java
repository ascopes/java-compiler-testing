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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.workspaces.Workspace;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Factory interface for building a file manager object.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.STABLE)
@FunctionalInterface
public interface JctFileManagerFactory {

  /**
   * Create and configure a file manager for a workspace.
   *
   * @param workspace the workspace to access files in.
   * @return the file manager.
   */
  JctFileManager createFileManager(Workspace workspace);
}
