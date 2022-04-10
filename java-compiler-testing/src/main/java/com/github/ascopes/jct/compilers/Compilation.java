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

package com.github.ascopes.jct.compilers;

import com.github.ascopes.jct.paths.PathLocationRepository;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Interface representing the result of a compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface Compilation {

  /**
   * Determine if warnings were treated as errors.
   *
   * @return {@code true} if warnings were treated as errors, or {@code false} otherwise.
   */
  boolean isWarningsAsErrors();

  /**
   * Determine if the compilation was successful or not.
   *
   * @return {@code true} if successful, or {@code false} if not successful.
   */
  boolean isSuccessful();

  /**
   * Determine if the compilation was a failure or not.
   *
   * @return {@code true} if not successful, or {@code false} if successful.
   */
  default boolean isFailure() {
    return !isSuccessful();
  }

  /**
   * Get the lines of output produced by the compiler, if any were captured.
   *
   * <p>This is separate to diagnostics.
   *
   * @return the lines of output.
   */
  List<String> getOutputLines();

  /**
   * Get the compilation units used in the compilation.
   *
   * @return the compilation units.
   */
  Set<? extends JavaFileObject> getCompilationUnits();

  /**
   * Get the diagnostics that were reported by the compilation.
   *
   * @return the diagnostics
   */
  List<? extends TraceDiagnostic<? extends JavaFileObject>> getDiagnostics();

  /**
   * Get the location repository that was used to store files.
   *
   * @return the location repository.
   */
  PathLocationRepository getFileRepository();
}
