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

package com.github.ascopes.jct.compilers.impl;

import java.util.List;

/**
 * Interface for defining a common flag builder for standard compilers.
 *
 * @author Ashley Scopes
 */
public interface FlagBuilder {

  /**
   * Add output-verbosity preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  FlagBuilder verbose(boolean enabled);


  /**
   * Add preview feature preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  FlagBuilder previewFeatures(boolean enabled);

  /**
   * Add warnings preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  FlagBuilder warnings(boolean enabled);

  /**
   * Set whether to treat warnings as errors or not.
   *
   * @param enabled whether to treat warnings as errors.
   * @return this builder.
   */
  FlagBuilder warningsAsErrors(boolean enabled);

  /**
   * Add deprecation warning preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  FlagBuilder deprecationWarnings(boolean enabled);

  /**
   * Add the release version.
   *
   * @param version the release version, or {@code null} if not specified.
   * @return this builder.
   */
  FlagBuilder releaseVersion(String version);

  /**
   * Add the source version.
   *
   * @param version the source version, or {@code null} if not specified.
   * @return this builder.
   */
  FlagBuilder sourceVersion(String version);

  /**
   * Add the target version.
   *
   * @param version the target version, or {@code null} if not specified.
   * @return this builder.
   */
  FlagBuilder targetVersion(String version);

  /**
   * Add annotation processor options.
   *
   * @param options the annotation processor options to use.
   * @return this builder.
   */
  FlagBuilder annotationProcessorOptions(List<String> options);

  /**
   * Add runtime options.
   *
   * @param options the options to pass to the runtime.
   * @return this builder.
   */
  FlagBuilder runtimeOptions(List<String> options);

  /**
   * Add additional command line options.
   *
   * @param options the additional commandline options to add.
   * @return this builder.
   */
  FlagBuilder options(List<String> options);

  /**
   * Build the list of command line options to use.
   *
   * @return the command line options to use.
   */
  List<String> build();
}
