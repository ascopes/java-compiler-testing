/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
import io.github.ascopes.jct.compilers.DebuggingInfo;
import io.github.ascopes.jct.compilers.JctFlagBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Helper to build flags for a standard Javac implementation for the OpenJDK.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JavacJctFlagBuilderImpl implements JctFlagBuilder {

  private static final String VERBOSE = "-verbose";
  private static final String ENABLE_PREVIEW = "--enable-preview";
  private static final String NOWARN = "-nowarn";
  private static final String WERROR = "-Werror";
  private static final String DEPRECATION = "-deprecation";
  private static final String RELEASE = "--release";
  private static final String SOURCE = "-source";
  private static final String TARGET = "-target";
  private static final String ANNOTATION_OPT = "-A";
  private static final String PROC_NONE = "-proc:none";
  private static final String PROC_ONLY = "-proc:only";
  private static final String PROC_FULL = "-proc:full";
  private static final String DEBUG_LINES = "-g:lines";
  private static final String DEBUG_VARS = "-g:vars";
  private static final String DEBUG_SOURCE = "-g:source";
  private static final String DEBUG_NONE = "-g:none";
  private static final String PARAMETERS = "-parameters";

  private final List<String> craftedFlags;

  /**
   * Initialize this flag builder.
   */
  public JavacJctFlagBuilderImpl() {
    craftedFlags = new ArrayList<>(3);
  }

  @Override
  public JavacJctFlagBuilderImpl verbose(boolean enabled) {
    return addFlagIfTrue(enabled, VERBOSE);
  }

  @Override
  public JavacJctFlagBuilderImpl previewFeatures(boolean enabled) {
    return addFlagIfTrue(enabled, ENABLE_PREVIEW);
  }

  @Override
  public JavacJctFlagBuilderImpl showWarnings(boolean enabled) {
    return addFlagIfFalse(enabled, NOWARN);
  }

  @Override
  public JavacJctFlagBuilderImpl failOnWarnings(boolean enabled) {
    return addFlagIfTrue(enabled, WERROR);
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
        if (Runtime.version().feature() >= 22) {
          // In Java 22, the default is to disable all annotation processing by default
          // Prior to Java 22, the default was to enable all annotation processing by default.
          craftedFlags.add(PROC_FULL);
        }
        break;
    }

    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl showDeprecationWarnings(boolean enabled) {
    return addFlagIfTrue(enabled, DEPRECATION);
  }

  @Override
  public JavacJctFlagBuilderImpl release(@Nullable String version) {
    return addVersionIfPresent(RELEASE, version);
  }

  @Override
  public JavacJctFlagBuilderImpl source(@Nullable String version) {
    return addVersionIfPresent(SOURCE, version);
  }

  @Override
  public JavacJctFlagBuilderImpl target(@Nullable String version) {
    return addVersionIfPresent(TARGET, version);
  }

  @Override
  public JctFlagBuilder debuggingInfo(Set<DebuggingInfo> set) {
    if (set.isEmpty()) {
      craftedFlags.add(DEBUG_NONE);
      return this;
    }

    if (set.contains(DebuggingInfo.LINES)) {
      craftedFlags.add(DEBUG_LINES);
    }

    if (set.contains(DebuggingInfo.SOURCE)) {
      craftedFlags.add(DEBUG_SOURCE);
    }

    if (set.contains(DebuggingInfo.VARS)) {
      craftedFlags.add(DEBUG_VARS);
    }

    return this;
  }

  @Override
  public JctFlagBuilder parameterInfoEnabled(boolean enabled) {
    return addFlagIfTrue(enabled, PARAMETERS);
  }

  @Override
  public JavacJctFlagBuilderImpl annotationProcessorOptions(List<String> options) {
    options.forEach(option -> craftedFlags.add(ANNOTATION_OPT + option));
    return this;
  }

  @Override
  public JavacJctFlagBuilderImpl compilerOptions(List<String> options) {
    craftedFlags.addAll(options);
    return this;
  }

  @Override
  public List<String> build() {
    // Immutable copy.
    return List.copyOf(craftedFlags);
  }

  private JavacJctFlagBuilderImpl addFlagIfTrue(boolean condition, String flag) {
    if (condition) {
      craftedFlags.add(flag);
    }

    return this;
  }

  @SuppressWarnings("SameParameterValue")
  private JavacJctFlagBuilderImpl addFlagIfFalse(boolean condition, String flag) {
    return addFlagIfTrue(!condition, flag);
  }

  private JavacJctFlagBuilderImpl addVersionIfPresent(String flagPrefix, @Nullable String version) {
    if (version != null) {
      craftedFlags.add(flagPrefix);
      craftedFlags.add(version);
    }

    return this;
  }
}
