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
  private final List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics;
  private final PathLocationRepository repository;

  private StandardCompilation(Builder builder) {
    warningsAsErrors = Objects.requireNonNull(builder.warningsAsErrors);
    success = Objects.requireNonNull(builder.success);
    outputLines = Collections.unmodifiableList(Objects.requireNonNull(builder.outputLines));
    compilationUnits = Collections.unmodifiableSet(
        Objects.requireNonNull(builder.compilationUnits));
    diagnostics = Collections.unmodifiableList(Objects.requireNonNull(builder.diagnostics));
    repository = Objects.requireNonNull(builder.fileRepository);
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
  public List<? extends TraceDiagnostic<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }

  @Override
  public PathLocationRepository getFileRepository() {
    return repository;
  }

  /**
   * Initialize a builder for a new StandardCompilation object.
   *
   * @return the builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder type for a {@link StandardCompilation} to simplify initialization.
   */
  public static final class Builder {

    private Boolean warningsAsErrors;
    private Boolean success;
    private List<String> outputLines;
    private Set<? extends JavaFileObject> compilationUnits;
    private List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics;
    private PathLocationRepository fileRepository;

    private Builder() {
      // Only initialized in this file.
    }

    /**
     * Set whether to treat warnings as errors.
     *
     * @param warningsAsErrors {@code true} or {@code false}.
     * @return this builder.
     */
    public Builder warningsAsErrors(boolean warningsAsErrors) {
      this.warningsAsErrors = warningsAsErrors;
      return this;
    }

    /**
     * Set whether the compilation succeeded.
     *
     * @param success {@code true} or {@code false}.
     * @return this builder.
     */
    public Builder success(boolean success) {
      this.success = success;
      return this;
    }

    /**
     * Set the output lines.
     *
     * @param outputLines the output lines.
     * @return this builder.
     */
    public Builder outputLines(List<String> outputLines) {
      this.outputLines = Objects.requireNonNull(outputLines);
      return this;
    }

    /**
     * Set the compilation units.
     *
     * @param compilationUnits the compilation units.
     * @return this builder.
     */
    public Builder compilationUnits(Set<? extends JavaFileObject> compilationUnits) {
      this.compilationUnits = Objects.requireNonNull(compilationUnits);
      return this;
    }

    /**
     * Set the diagnostics.
     *
     * @param diagnostics the diagnostics.
     * @return this builder.
     */
    public Builder diagnostics(
        List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics
    ) {
      this.diagnostics = Objects.requireNonNull(diagnostics);
      return this;
    }

    /**
     * Set the file repository.
     *
     * @param fileRepository the file repository.
     * @return this builder.
     */
    public Builder fileRepository(PathLocationRepository fileRepository) {
      this.fileRepository = Objects.requireNonNull(fileRepository);
      return this;
    }

    /**
     * Build this builder and output the created {@link StandardCompilation}.
     *
     * @return the built object.
     */
    public StandardCompilation build() {
      return new StandardCompilation(this);
    }

  }
}
