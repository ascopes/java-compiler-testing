/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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
package io.github.ascopes.jct.compilers.javac;

import io.github.ascopes.jct.compilers.JctFlagBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper to build flags for a standard Javac implementation for the OpenJDK.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
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
  private static final String RUNTIME_OPT = "-J";

  private final List<String> craftedFlags;

  /**
   * Initialize this flag builder.
   */
  public JavacJctFlagBuilderImpl() {
    craftedFlags = new ArrayList<>();
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
    return addFlagIfTrue(!enabled, NOWARN);
  }

  @Override
  public JavacJctFlagBuilderImpl failOnWarnings(boolean enabled) {
    return addFlagIfTrue(enabled, WERROR);
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

  private JavacJctFlagBuilderImpl addVersionIfPresent(String flagPrefix, @Nullable String version) {
    if (version != null) {
      craftedFlags.add(flagPrefix);
      craftedFlags.add(version);
    }

    return this;
  }
}
