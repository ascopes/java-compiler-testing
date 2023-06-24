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

import io.github.ascopes.jct.compilers.CompilationMode;
import io.github.ascopes.jct.compilers.JctFlagBuilder;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Helper to build flags for the ECJ compiler implementation.
 *
 * @author Ashley Scopes
 * @since TBC
 */
@API(since = "TBC", status = Status.INTERNAL)
public final class EcjJctFlagBuilderImpl implements JctFlagBuilder {

  private static final String VERBOSE = "-verbose";
  private static final String PRINT_ANNOTATION_PROCESSOR_INFO = "-XprintProcessorInfo";
  private static final String PRINT_ANNOTATION_PROCESSOR_ROUNDS = "-XprintRounds";
  private static final String ENABLE_PREVIEW = "--enable-preview";
  private static final String NOWARN = "-nowarn";
  private static final String FAIL_ON_WARNING = "--failOnWarning";
  private static final String DEPRECATION = "-deprecation";
  private static final String RELEASE = "--release";
  private static final String SOURCE = "-source";
  private static final String TARGET = "-target";
  private static final String ANNOTATION_OPT = "-A";
  private static final String PROC_NONE = "-proc:none";
  private static final String PROC_ONLY = "-proc:only";

  private final List<String> craftedFlags;

  /**
   * Initialize this flag builder.
   */
  public EcjJctFlagBuilderImpl() {
    craftedFlags = new ArrayList<>();
  }

  @Override
  public EcjJctFlagBuilderImpl verbose(boolean enabled) {
    return addFlagIfTrue(enabled, VERBOSE)
        .addFlagIfTrue(enabled, PRINT_ANNOTATION_PROCESSOR_INFO)
        .addFlagIfTrue(enabled, PRINT_ANNOTATION_PROCESSOR_ROUNDS);
  }

  @Override
  public EcjJctFlagBuilderImpl previewFeatures(boolean enabled) {
    return addFlagIfTrue(enabled, ENABLE_PREVIEW);
  }

  @Override
  public EcjJctFlagBuilderImpl showWarnings(boolean enabled) {
    return addFlagIfTrue(!enabled, NOWARN);
  }

  @Override
  public EcjJctFlagBuilderImpl failOnWarnings(boolean enabled) {
    return addFlagIfTrue(enabled, FAIL_ON_WARNING);
  }

  @Override
  public JctFlagBuilder compilationMode(CompilationMode compilationMode) {
    switch (compilationMode) {
      case COMPILATION_ONLY:
        craftedFlags.add(PROC_NONE);
        break;

      case ANNOTATION_PROCESSING_ONLY:
        craftedFlags.add(PROC_ONLY);
        break;

      default:
        // Do nothing. The default behaviour is to allow this.
        break;
    }

    return this;
  }

  @Override
  public EcjJctFlagBuilderImpl showDeprecationWarnings(boolean enabled) {
    return addFlagIfTrue(enabled, DEPRECATION);
  }

  @Override
  public EcjJctFlagBuilderImpl release(@Nullable String version) {
    return addVersionIfPresent(RELEASE, version);
  }

  @Override
  public EcjJctFlagBuilderImpl source(@Nullable String version) {
    return addVersionIfPresent(SOURCE, version);
  }

  @Override
  public EcjJctFlagBuilderImpl target(@Nullable String version) {
    return addVersionIfPresent(TARGET, version);
  }

  @Override
  public EcjJctFlagBuilderImpl annotationProcessorOptions(List<String> options) {
    options.forEach(option -> craftedFlags.add(ANNOTATION_OPT + option));
    return this;
  }

  @Override
  public EcjJctFlagBuilderImpl compilerOptions(List<String> options) {
    craftedFlags.addAll(options);
    return this;
  }

  @Override
  public List<String> build() {
    // Immutable copy.
    return List.copyOf(craftedFlags);
  }

  private EcjJctFlagBuilderImpl addFlagIfTrue(boolean condition, String flag) {
    if (condition) {
      craftedFlags.add(flag);
    }

    return this;
  }

  private EcjJctFlagBuilderImpl addVersionIfPresent(String flagPrefix, @Nullable String version) {
    if (version != null) {
      craftedFlags.add(flagPrefix);
      craftedFlags.add(version);
    }

    return this;
  }
}
