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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper to build flags for a standard Javac implementation for the OpenJDK.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public class JavacFlagBuilder implements FlagBuilder {

  protected final Stream.Builder<String> craftedFlags;
  protected final Stream.Builder<String> annotationProcessorOptions;
  protected final Stream.Builder<String> otherOptions;

  /**
   * Initialize this flag builder.
   */
  public JavacFlagBuilder() {
    craftedFlags = Stream.builder();
    annotationProcessorOptions = Stream.builder();
    otherOptions = Stream.builder();
  }

  @Override
  public JavacFlagBuilder verbose(boolean enabled) {
    return flagIfTrue(enabled, "-verbose");
  }

  @Override
  public JavacFlagBuilder previewFeatures(boolean enabled) {
    return flagIfTrue(enabled, "--enable-preview");
  }

  @Override
  public JavacFlagBuilder warnings(boolean enabled) {
    return flagIfTrue(!enabled, "-nowarn");
  }

  @Override
  public JavacFlagBuilder warningsAsErrors(boolean enabled) {
    return flagIfTrue(enabled, "-Werror");
  }

  @Override
  public JavacFlagBuilder deprecationWarnings(boolean enabled) {
    return flagIfTrue(enabled, "-deprecation");
  }

  @Override
  public JavacFlagBuilder releaseVersion(String version) {
    return versionIfPresent("--release", version);
  }

  @Override
  public JavacFlagBuilder sourceVersion(String version) {
    return versionIfPresent("-source", version);
  }

  @Override
  public JavacFlagBuilder targetVersion(String version) {
    return versionIfPresent("-target", version);
  }

  @Override
  public JavacFlagBuilder annotationProcessorOptions(List<String> options) {
    options.forEach(option -> annotationProcessorOptions.add("-A" + option));
    return this;
  }

  @Override
  public JavacFlagBuilder runtimeOptions(List<String> options) {
    options.forEach(option -> annotationProcessorOptions.add("-J" + option));
    return this;
  }

  @Override
  public JavacFlagBuilder options(List<String> options) {
    options.forEach(otherOptions::add);
    return this;
  }

  @Override
  public List<String> build() {
    return Stream
        .of(craftedFlags.build(), annotationProcessorOptions.build(), otherOptions.build())
        .reduce(Stream.empty(), Stream::concat)
        .collect(Collectors.toList());
  }

  private JavacFlagBuilder flagIfTrue(boolean condition, String... flags) {
    if (condition) {
      for (var flag : flags) {
        craftedFlags.add(flag);
      }
    }

    return this;
  }

  private JavacFlagBuilder versionIfPresent(String flagPrefix, String nullableVersion) {
    Optional
        .ofNullable(nullableVersion)
        .ifPresent(version -> craftedFlags.add(flagPrefix).add(version));
    return this;
  }
}
