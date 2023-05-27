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
package io.github.ascopes.jct.workspaces;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.utils.UtilityClass;
import io.github.ascopes.jct.workspaces.impl.WorkspaceImpl;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helpers to create new workspaces.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class Workspaces extends UtilityClass {

  private Workspaces() {
    // Do nothing.
  }

  /**
   * Create a new default workspace instance using the default path strategy.
   *
   * <p>This workspace must be closed after use. Ideally, you should use a try-with-resources
   * to achieve this. Failing to do this will lead to resources being leaked and tests not being
   * cleared up correctly.
   *
   * <p>The workspace can be passed to {@link JctCompiler#compile(Workspace)} to run the compiler
   * across this workspace of code.
   *
   * <p>For example:
   *
   * <pre><code>
   *   try (var workspace = Workspaces.newWorkspace()) {
   *     workspace
   *        .createSourcePathPackage()
   *        .copyContentsFrom("src", "test", "resources", "test-data");
   *
   *     var compilation = someCompiler.compile(workspace);
   *
   *     assertThat(compilation).isSuccessful();
   *   }
   * </code></pre>
   *
   * @return the workspace.
   */
  public static Workspace newWorkspace() {
    return newWorkspace(PathStrategy.defaultStrategy());
  }

  /**
   * Create a new default workspace instance using the given path strategy.
   *
   * <p>This workspace must be closed after use. Ideally, you should use a try-with-resources
   * to achieve this. Failing to do this will lead to resources being leaked and tests not being
   * cleared up correctly.
   *
   * <p>The workspace can be passed to {@link JctCompiler#compile(Workspace)} to run the compiler
   * across this workspace of code.
   *
   * <p>For example:
   *
   * <pre><code>
   *   try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
   *     workspace
   *        .createSourcePathPackage()
   *        .copyContentsFrom("src", "test", "resources", "test-data");
   *
   *     var compilation = someCompiler.compile(workspace);
   *
   *     assertThat(compilation).isSuccessful();
   *   }
   * </code></pre>
   *
   * @param pathStrategy the path strategy to use.
   * @return the workspace.
   */
  public static Workspace newWorkspace(PathStrategy pathStrategy) {
    return new WorkspaceImpl(pathStrategy);
  }
}
