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

package com.github.ascopes.jct.compilations;

import com.github.ascopes.jct.diagnostics.TraceDiagnostic;
import com.github.ascopes.jct.paths.PathLocationRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;


/**
 * Representation of the result of running a Javac compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class StandardCompilation implements Compilation {

  private final boolean warningsAsErrors;
  private final boolean success;
  private final List<String> outputLines;
  private final Set<? extends JavaFileObject> compilationUnits;
  private final List<TraceDiagnostic<? extends JavaFileObject>> diagnostics;
  private final PathLocationRepository repository;

  /**
   * Initialize the compilation result.
   *
   * @param warningsAsErrors {@code true} if warnings were errors, {@code false} otherwise.
   * @param success          {@code true} if successful, {@code false} otherwise.
   * @param outputLines      the lines of output from the compiler.
   * @param compilationUnits the compilation units that were used.
   * @param diagnostics      the diagnostics that were reported.
   * @param repository       the file repository that was used.
   */
  public StandardCompilation(
      boolean warningsAsErrors,
      boolean success,
      List<String> outputLines,
      Set<? extends JavaFileObject> compilationUnits,
      List<TraceDiagnostic<? extends JavaFileObject>> diagnostics,
      PathLocationRepository repository
  ) {
    this.warningsAsErrors = warningsAsErrors;
    this.success = success;
    this.outputLines = Collections.unmodifiableList(Objects.requireNonNull(outputLines));
    this.compilationUnits = Collections.unmodifiableSet(Objects.requireNonNull(compilationUnits));
    this.diagnostics = Collections.unmodifiableList(Objects.requireNonNull(diagnostics));
    this.repository = Objects.requireNonNull(repository);
  }

  @Override
  public boolean isWarningsAsErrors() {
    return warningsAsErrors;
  }

  @Override
  public boolean isSuccessful() {
    return success;
  }

  @Override
  public List<String> getOutputLines() {
    return outputLines;
  }

  @Override
  public Set<? extends JavaFileObject> getCompilationUnits() {
    return compilationUnits;
  }

  @Override
  public List<TraceDiagnostic<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }

  @Override
  public PathLocationRepository getFileRepository() {
    return repository;
  }
}
