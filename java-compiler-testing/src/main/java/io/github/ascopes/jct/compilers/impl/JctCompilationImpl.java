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
package io.github.ascopes.jct.compilers.impl;

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.filemanagers.JctFileManager;
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
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JctCompilationImpl implements JctCompilation {

  private final boolean success;
  private final boolean failOnWarnings;
  private final List<String> outputLines;
  private final Set<JavaFileObject> compilationUnits;
  private final List<TraceDiagnostic<JavaFileObject>> diagnostics;
  private final JctFileManager fileManager;

  private JctCompilationImpl(Builder builder) {
    success = requireNonNull(
        builder.success, "success"
    );
    failOnWarnings = requireNonNull(
        builder.failOnWarnings, "failOnWarnings"
    );
    outputLines = unmodifiableList(
        requireNonNullValues(builder.outputLines, "outputLines")
    );
    compilationUnits = unmodifiableSet(
        requireNonNullValues(builder.compilationUnits, "compilationUnits")
    );
    diagnostics = unmodifiableList(
        requireNonNullValues(builder.diagnostics, "diagnostics")
    );
    fileManager = requireNonNull(
        builder.fileManager, "fileManager"
    );
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
  public Set<JavaFileObject> getCompilationUnits() {
    return compilationUnits;
  }

  @Override
  public List<TraceDiagnostic<JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }

  @Override
  public JctFileManager getFileManager() {
    return fileManager;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("success", success)
        .attribute("failOnWarnings", failOnWarnings)
        .attribute("fileManager", fileManager)
        .toString();
  }

  /**
   * Initialize a builder for a new {@link JctCompilationImpl} object.
   *
   * @return the builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder type for a {@link JctCompilationImpl} to simplify initialization.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
  public static final class Builder {

    private Boolean failOnWarnings;
    private Boolean success;
    private List<String> outputLines;
    private Set<JavaFileObject> compilationUnits;
    private List<TraceDiagnostic<JavaFileObject>> diagnostics;
    private JctFileManager fileManager;

    private Builder() {
      // Only initialized in this file.
      failOnWarnings = null;
      success = null;
      outputLines = null;
      compilationUnits = null;
      diagnostics = null;
      fileManager = null;
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
    public Builder compilationUnits(Set<JavaFileObject> compilationUnits) {
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
        List<TraceDiagnostic<JavaFileObject>> diagnostics
    ) {
      this.diagnostics = requireNonNull(diagnostics, "diagnostics");
      return this;
    }

    /**
     * Set the file manager.
     *
     * <p>The file manager will not be closed once finshed with.
     *
     * @param fileManager the file manager.
     * @return this builder.
     */
    public Builder fileManager(JctFileManager fileManager) {
      this.fileManager = requireNonNull(fileManager, "fileManager");
      return this;
    }

    /**
     * Build this builder and output the created {@link JctCompilationImpl}.
     *
     * @return the built object.
     */
    public JctCompilationImpl build() {
      return new JctCompilationImpl(this);
    }
  }
}
