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

package com.github.ascopes.jct.testing.unit.compilers.javac;

import com.github.ascopes.jct.compilers.javac.JavacFlagBuilder;
import com.github.ascopes.jct.testing.unit.compilers.FlagBuilderTestSupport;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * {@link JavacFlagBuilder} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavacFlagBuilder tests")
class JavacFlagBuilderTest extends FlagBuilderTestSupport<JavacFlagBuilder> {

  @Override
  protected JavacFlagBuilder initialize() {
    return new JavacFlagBuilder();
  }

  @DisplayName("Verbose flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> verboseFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled("verbose", "-verbose", JavacFlagBuilder::verbose);
  }

  @DisplayName("Preview features flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> enablePreviewFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled(
        "preview features",
        "--enable-preview",
        JavacFlagBuilder::previewFeatures
    );
  }

  @DisplayName("No-warnings flag should be set when warnings disabled")
  @TestFactory
  Stream<DynamicTest> noWarnFlagShouldBeSetWhenDisabled() {
    return flagAddedIfDisabled(
        "warnings",
        "-nowarn",
        JavacFlagBuilder::warnings
    );
  }

  @DisplayName("Warnings-as-errors flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> warningsAsErrorsFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled(
        "warnings-as-errors",
        "-Werror",
        JavacFlagBuilder::warningsAsErrors
    );
  }

  @DisplayName("Deprecation warnings flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> deprecationWarningsFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled(
        "deprecation warnings",
        "-deprecation",
        JavacFlagBuilder::deprecationWarnings
    );
  }

  @DisplayName("Release flag should be set if specified")
  @TestFactory
  Stream<DynamicTest> releaseFlagIsSetWhenSpecified() {
    return argAddedIfProvided(
        "release version",
        "--release",
        JavacFlagBuilder::releaseVersion,
        Function.identity(),
        "10", "11", "17"
    );
  }

  @DisplayName("Source flag should be set if specified")
  @TestFactory
  Stream<DynamicTest> sourceFlagIsSetWhenSpecified() {
    return argAddedIfProvided(
        "source version",
        "-source",
        JavacFlagBuilder::sourceVersion,
        Function.identity(),
        "10", "11", "17"
    );
  }

  @DisplayName("Annotation processor options should be set when provided")
  @TestFactory
  Stream<DynamicTest> annotationProcessorOptionsShouldBeSetWhenProvided() {
    return flagWithArgsSetIfProvided(
        "annotation processor options",
        "-A",
        JavacFlagBuilder::annotationProcessorOptions,
        Function.identity(),
        "foo=bar", "baz=bork", "qux=quxx"
    );
  }

  @DisplayName("Runtime options should be set when provided")
  @TestFactory
  Stream<DynamicTest> runtimeOptionsShouldBeSetWhenProvided() {
    return flagWithArgsSetIfProvided(
        "runtime options",
        "-J",
        JavacFlagBuilder::runtimeOptions,
        Function.identity(),
        "do=ray", "me.far=so", "lah.tea.do=blah"
    );
  }

  @DisplayName("Other options should be set when provided")
  @TestFactory
  Stream<DynamicTest> otherOptionsShouldBeSetWhenProvided() {
    return otherArgsAddedWhenProvided(
        JavacFlagBuilder::options,
        "--foo.bar=baz", "--explode-on-error", "-rainbow"
    );
  }
}
