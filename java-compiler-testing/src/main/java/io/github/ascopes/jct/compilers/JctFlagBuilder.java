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
package io.github.ascopes.jct.compilers;

import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Interface for defining a common flag builder for compilers.
 *
 * @author Ashley Scopes
 */
public interface JctFlagBuilder {

  /**
   * Add output-verbosity preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  JctFlagBuilder verbose(boolean enabled);

  /**
   * Add preview feature preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  JctFlagBuilder previewFeatures(boolean enabled);

  /**
   * Add warnings preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  JctFlagBuilder showWarnings(boolean enabled);

  /**
   * Set whether to treat warnings as errors or not.
   *
   * @param enabled whether to treat warnings as errors.
   * @return this builder.
   */
  JctFlagBuilder failOnWarnings(boolean enabled);

  /**
   * Set the compilation mode to run under.
   *
   * @param compilationMode the compilation mode to run under.
   * @return this builder.
   */
  JctFlagBuilder compilationMode(CompilationMode compilationMode);

  /**
   * Add deprecation warning preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  JctFlagBuilder showDeprecationWarnings(boolean enabled);

  /**
   * Add the release version.
   *
   * @param version the release version, or {@code null} if not specified.
   * @return this builder.
   */
  JctFlagBuilder release(@Nullable String version);

  /**
   * Add the source version.
   *
   * @param version the source version, or {@code null} if not specified.
   * @return this builder.
   */
  JctFlagBuilder source(@Nullable String version);

  /**
   * Add the target version.
   *
   * @param version the target version, or {@code null} if not specified.
   * @return this builder.
   */
  JctFlagBuilder target(@Nullable String version);

  /**
   * Add the debugging info flags.
   *
   * @param set the set of debugging info flags to enable.
   * @return this builder.
   * @since 3.0.0
   */
  JctFlagBuilder debuggingInfo(Set<DebuggingInfo> set);

  /**
   * Specify whether to include parameter reflection info in the compiled classes.
   *
   * @param enabled whether the parameter info is enabled or not.
   * @return this builder.
   * @since 3.0.0
   */
  JctFlagBuilder parameterInfoEnabled(boolean enabled);

  /**
   * Add annotation processor options.
   *
   * @param options the annotation processor options to use.
   * @return this builder.
   */
  JctFlagBuilder annotationProcessorOptions(List<String> options);

  /**
   * Add additional command line options.
   *
   * @param options the additional commandline options to add.
   * @return this builder.
   */
  JctFlagBuilder compilerOptions(List<String> options);

  /**
   * Build the list of command line options to use.
   *
   * @return the command line options to use.
   */
  List<String> build();
}
