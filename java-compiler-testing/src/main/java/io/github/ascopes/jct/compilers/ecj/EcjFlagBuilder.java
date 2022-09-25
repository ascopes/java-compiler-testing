/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.compilers.ecj;

import io.github.ascopes.jct.compilers.FlagBuilder;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper to build flags for ECJ.
 *
 * <p>This mostly uses the same flags as javac, but some behaviour differs, so the implementations
 * are kept separate to prevent issues in the future if the implementations totally diverge.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class EcjFlagBuilder implements FlagBuilder {

  private static final String VERBOSE = "-verbose";
  private static final String ENABLE_PREVIEW = "--enable-preview";
  private static final String NOWARN = "-nowarn";
  private static final String FAIL_ON_WARNING = "--failOnWarning";
  private static final String DEPRECATION = "-deprecation";
  private static final String RELEASE = "--release";
  private static final String SOURCE = "-source";
  private static final String TARGET = "-target";
  private static final String ANNOTATION_OPT = "-A";
  private static final String RUNTIME_OPT = "-J";


  private final Stream.Builder<String> craftedFlags;
  private final Stream.Builder<String> annotationProcessorOptions;
  private final Stream.Builder<String> otherOptions;

  /**
   * Initialize this flag builder.
   */
  public EcjFlagBuilder() {
    craftedFlags = Stream.builder();
    annotationProcessorOptions = Stream.builder();
    otherOptions = Stream.builder();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("craftedFlags", craftedFlags)
        .attribute("annotationProcessorOptions", annotationProcessorOptions)
        .attribute("otherOptions", otherOptions)
        .toString();
  }

  @Override
  public EcjFlagBuilder verbose(boolean enabled) {
    return flagIfTrue(enabled, VERBOSE);
  }

  @Override
  public EcjFlagBuilder previewFeatures(boolean enabled) {
    return flagIfTrue(enabled, ENABLE_PREVIEW);
  }

  @Override
  public EcjFlagBuilder showWarnings(boolean enabled) {
    return flagIfTrue(!enabled, NOWARN);
  }

  @Override
  public EcjFlagBuilder failOnWarnings(boolean enabled) {
    // Differs to javac for some reason.
    return flagIfTrue(enabled, FAIL_ON_WARNING);
  }

  @Override
  public EcjFlagBuilder showDeprecationWarnings(boolean enabled) {
    return flagIfTrue(enabled, DEPRECATION);
  }

  @Override
  public EcjFlagBuilder release(String version) {
    return versionIfPresent(RELEASE, version);
  }

  @Override
  public EcjFlagBuilder source(String version) {
    return versionIfPresent(SOURCE, version);
  }

  @Override
  public EcjFlagBuilder target(String version) {
    return versionIfPresent(TARGET, version);
  }

  @Override
  public EcjFlagBuilder annotationProcessorOptions(List<String> options) {
    options.forEach(option -> annotationProcessorOptions.add(ANNOTATION_OPT + option));
    return this;
  }

  @Override
  public EcjFlagBuilder runtimeOptions(List<String> options) {
    options.forEach(option -> annotationProcessorOptions.add(RUNTIME_OPT + option));
    return this;
  }

  @Override
  public EcjFlagBuilder compilerOptions(List<String> options) {
    options.forEach(otherOptions::add);
    return this;
  }

  @Override
  public List<String> build() {
    return Stream
        .of(craftedFlags.build(), annotationProcessorOptions.build(), otherOptions.build())
        .flatMap(Function.identity())
        .collect(Collectors.toList());
  }

  private EcjFlagBuilder flagIfTrue(boolean condition, String... flags) {
    if (condition) {
      for (var flag : flags) {
        craftedFlags.add(flag);
      }
    }

    return this;
  }

  private EcjFlagBuilder versionIfPresent(String flagPrefix, String nullableVersion) {
    Optional
        .ofNullable(nullableVersion)
        .ifPresent(version -> craftedFlags.add(flagPrefix).add(version));
    return this;
  }
}
