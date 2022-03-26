package com.github.ascopes.jct.compilers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper to build flags for a standard Javac implementation for the OpenJDK.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JavacFlagBuilder {

  private final Stream.Builder<String> craftedFlags;
  private final Stream.Builder<String> annotationProcessorOptions;
  private final Stream.Builder<String> otherOptions;

  public JavacFlagBuilder() {
    craftedFlags = Stream.builder();
    annotationProcessorOptions = Stream.builder();
    otherOptions = Stream.builder();
  }

  /**
   * Add output-verbosity preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  public JavacFlagBuilder verbose(boolean enabled) {
    return flagIfTrue(enabled, "-verbose");
  }


  /**
   * Add preview feature preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  public JavacFlagBuilder previewFeatures(boolean enabled) {
    return flagIfTrue(enabled, "--enable-preview");
  }

  /**
   * Add warnings preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  public JavacFlagBuilder warnings(boolean enabled) {
    return flagIfTrue(!enabled, "-nowarn");
  }

  /**
   * Add deprecation warning preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  public JavacFlagBuilder deprecationWarnings(boolean enabled) {
    return flagIfTrue(enabled, "-deprecation");
  }

  /**
   * Add fail-on-warning preferences.
   *
   * @param enabled whether the feature is enabled.
   * @return this builder.
   */
  public JavacFlagBuilder failOnWarnings(boolean enabled) {
    return flagIfTrue(enabled, "-Werror");
  }

  /**
   * Add the release version.
   *
   * @param version the release version, or {@code null} if not specified.
   * @return this builder.
   */
  public JavacFlagBuilder releaseVersion(String version) {
    return versionIfPresent("--release", version);
  }

  /**
   * Add the source version.
   *
   * @param version the source version, or {@code null} if not specified.
   * @return this builder.
   */
  public JavacFlagBuilder sourceVersion(String version) {
    return versionIfPresent("-source", version);
  }

  /**
   * Add the target version.
   *
   * @param version the target version, or {@code null} if not specified.
   * @return this builder.
   */
  public JavacFlagBuilder targetVersion(String version) {
    return versionIfPresent("-target", version);
  }

  /**
   * Add annotation processor options.
   *
   * @param options the annotation processor options to use.
   * @return this builder.
   */
  public JavacFlagBuilder annotationProcessorOptions(List<String> options) {
    options.forEach(option -> annotationProcessorOptions.add("-A" + option));
    return this;
  }

  /**
   * Add runtime options
   *
   * @param options the options to pass to the runtime.
   * @return this builder.
   */
  public JavacFlagBuilder runtimeOptions(List<String> options) {
    options.forEach(option -> annotationProcessorOptions.add("-J" + option));
    return this;
  }

  /**
   * Add additional command line options.
   *
   * @param options the additional commandline options to add.
   * @return this builder.
   */
  public JavacFlagBuilder options(List<String> options) {
    options.forEach(otherOptions::add);
    return this;
  }

  /**
   * Build the list of command line options to use.
   *
   * @return the command line options to use.
   */
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
