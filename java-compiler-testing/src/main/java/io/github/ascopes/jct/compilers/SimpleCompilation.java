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

package io.github.ascopes.jct.compilers;

import static io.github.ascopes.jct.utils.IterableUtils.nonNullUnmodifiableList;
import static io.github.ascopes.jct.utils.IterableUtils.nonNullUnmodifiableSet;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.jsr199.FileManager;
import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.util.List;
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
public final class SimpleCompilation implements Compilation {

  private final boolean success;
  private final boolean failOnWarnings;
  private final List<String> outputLines;
  private final Set<? extends JavaFileObject> compilationUnits;
  private final List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics;
  private final FileManager fileManager;

  private SimpleCompilation(Builder builder) {
    success = requireNonNull(builder.success, "success");
    failOnWarnings = requireNonNull(builder.failOnWarnings, "failOnWarnings");
    outputLines = nonNullUnmodifiableList(builder.outputLines, "outputLines");
    compilationUnits = nonNullUnmodifiableSet(builder.compilationUnits, "compilationUnits");
    diagnostics = nonNullUnmodifiableList(builder.diagnostics, "diagnostics");
    fileManager = requireNonNull(builder.fileManager, "fileManager");
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("success", success)
        .attribute("failOnWarnings", failOnWarnings)
        .attribute("fileManager", fileManager)
        .toString();
  }

  @Override
  public boolean isSuccessful() {
    return success;
  }

  @Override
  public boolean isFailOnWarnings() {
    return failOnWarnings;
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
  public FileManager getFileManager() {
    return fileManager;
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
   * Builder type for a {@link SimpleCompilation} to simplify initialization.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  public static final class Builder {

    private Boolean failOnWarnings;
    private Boolean success;
    private List<String> outputLines;
    private Set<? extends JavaFileObject> compilationUnits;
    private List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics;
    private FileManager fileManager;

    private Builder() {
      // Only initialized in this file.
    }

    /**
     * Set whether to treat warnings as errors.
     *
     * @param failOnWarnings {@code true} or {@code false}.
     * @return this builder.
     */
    public Builder failOnWarnings(boolean failOnWarnings) {
      this.failOnWarnings = failOnWarnings;
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
      this.outputLines = requireNonNull(outputLines, "outputLines");
      return this;
    }

    /**
     * Set the compilation units.
     *
     * @param compilationUnits the compilation units.
     * @return this builder.
     */
    public Builder compilationUnits(Set<? extends JavaFileObject> compilationUnits) {
      this.compilationUnits = requireNonNull(compilationUnits, "compilationUnits");
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
      this.diagnostics = requireNonNull(diagnostics, "diagnostics");
      return this;
    }

    /**
     * Set the file manager.
     *
     * @param fileManager the file manager.
     * @return this builder.
     */
    public Builder fileManager(FileManager fileManager) {
      this.fileManager = requireNonNull(fileManager, "fileManager");
      return this;
    }

    /**
     * Build this builder and output the created {@link SimpleCompilation}.
     *
     * @return the built object.
     */
    public SimpleCompilation build() {
      return new SimpleCompilation(this);
    }
  }
}
