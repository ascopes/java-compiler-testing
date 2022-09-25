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
package io.github.ascopes.jct.testing.unit.compilers.ecj;

import io.github.ascopes.jct.compilers.ecj.EcjFlagBuilder;
import io.github.ascopes.jct.testing.unit.compilers.FlagBuilderTestSupport;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * {@link EcjFlagBuilder} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("EcjFlagBuilder tests")
class EcjFlagBuilderTest extends FlagBuilderTestSupport<EcjFlagBuilder> {

  @Override
  protected EcjFlagBuilder initialize() {
    return new EcjFlagBuilder();
  }

  @DisplayName("Verbose flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> verboseFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled("verbose", "-verbose", EcjFlagBuilder::verbose);
  }

  @DisplayName("Preview features flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> enablePreviewFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled(
        "preview features",
        "--enable-preview",
        EcjFlagBuilder::previewFeatures
    );
  }

  @DisplayName("No-warnings flag should be set when warnings disabled")
  @TestFactory
  Stream<DynamicTest> noWarnFlagShouldBeSetWhenDisabled() {
    return flagAddedIfDisabled(
        "warnings",
        "-nowarn",
        EcjFlagBuilder::showWarnings
    );
  }

  @DisplayName("Warnings-as-errors flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> warningsAsErrorsFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled(
        "warnings-as-errors",
        "--failOnWarning",
        EcjFlagBuilder::failOnWarnings
    );
  }

  @DisplayName("Deprecation warnings flag should be set when enabled")
  @TestFactory
  Stream<DynamicTest> deprecationWarningsFlagShouldBeSetWhenEnabled() {
    return flagAddedIfEnabled(
        "deprecation warnings",
        "-deprecation",
        EcjFlagBuilder::showDeprecationWarnings
    );
  }

  @DisplayName("Release flag should be set if specified")
  @TestFactory
  Stream<DynamicTest> releaseFlagIsSetWhenSpecified() {
    return argAddedIfProvided(
        "release version",
        "--release",
        EcjFlagBuilder::release,
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
        EcjFlagBuilder::source,
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
        EcjFlagBuilder::annotationProcessorOptions,
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
        EcjFlagBuilder::runtimeOptions,
        Function.identity(),
        "do=ray", "me.far=so", "lah.tea.do=blah"
    );
  }

  @DisplayName("Other options should be set when provided")
  @TestFactory
  Stream<DynamicTest> otherOptionsShouldBeSetWhenProvided() {
    return otherArgsAddedWhenProvided(
        EcjFlagBuilder::compilerOptions,
        "--foo.bar=baz", "--explode-on-error", "-rainbow"
    );
  }
}
