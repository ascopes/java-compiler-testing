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
package io.github.ascopes.jct.assertions;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.containers.ContainerGroup;
import io.github.ascopes.jct.repr.TraceDiagnosticListRepresentation;
import io.github.ascopes.jct.utils.StringUtils;
import java.util.Collection;
import java.util.List;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.FactoryBasedNavigableListAssert;
import org.assertj.core.api.StringAssert;
import org.jspecify.annotations.Nullable;

/**
 * Assertions that apply to a {@link JctCompilation}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JctCompilationAssert extends
    AbstractAssert<JctCompilationAssert, JctCompilation> {

  /**
   * Initialize this compilation assertion.
   *
   * @param value the value to assert on.
   */
  public JctCompilationAssert(@Nullable JctCompilation value) {
    super(value, JctCompilationAssert.class);
  }

  /**
   * Assert that the arguments passed to the compiler were the expected values.
   *
   * @return a list assertion object to perform assertions on the arguments with.
   * @throws AssertionError if the compilation was null.
   */
  public AbstractListAssert<?, List<? extends String>, String, ? extends AbstractStringAssert<?>> arguments() {
    isNotNull();

    var arguments = actual.getArguments();

    // TODO(ascopes): find a way to use Assertions::assertThat here instead of passing the
    //   StringAssert constructor around.
    return FactoryBasedNavigableListAssert.assertThat(arguments, StringAssert::new)
        .as("Compiler arguments %s", StringUtils.quotedIterable(arguments));
  }

  /**
   * Assert that the compilation was successful.
   *
   * @return this assertion object.
   * @throws AssertionError if the compilation was null, or if the compilation was not successful.
   */
  public JctCompilationAssert isSuccessful() {
    isNotNull();

    if (actual.isFailure()) {
      // If we have error diagnostics, add them to the error message to provide helpful debugging
      // information. If we are treating warnings as errors, then we want to include those in this
      // as well.
      var diagnosticKinds = actual.isFailOnWarnings()
          ? DiagnosticKindAssert.WARNING_AND_ERROR_DIAGNOSTIC_KINDS
          : DiagnosticKindAssert.ERROR_DIAGNOSTIC_KINDS;

      failWithDiagnostics(diagnosticKinds, "Expected a successful compilation, but it failed.");
    }

    return myself;
  }

  /**
   * Assert that the compilation was successful and had no warnings.
   *
   * <p>If warnings were treated as errors by the compiler, then this is identical to calling
   * {@link #isSuccessful()}.
   *
   * @return this assertion object.
   * @throws AssertionError if the compilation was null, if the compilation was not successful, or
   *                        if the compilation was successful but had one or more warning
   *                        diagnostics.
   */
  public JctCompilationAssert isSuccessfulWithoutWarnings() {
    isSuccessful();
    diagnostics().hasNoErrorsOrWarnings();
    return myself;
  }

  /**
   * Assert that the compilation was a failure.
   *
   * @return this assertion object.
   * @throws AssertionError if the compilation was null, or if the compilation succeeded.
   */
  public JctCompilationAssert isFailure() {
    isNotNull();

    // If we fail due to failOnWarnings, we expect the compiler itself to have failed the
    // build because of this. If the compiler ignores this flag and succeeds, then this method will
    // follow that behaviour and treat the compilation as a success.

    if (actual.isSuccessful()) {
      // If we have any warnings, we should show them in the error message as it might be useful
      // to the user.
      failWithDiagnostics(
          DiagnosticKindAssert.WARNING_AND_ERROR_DIAGNOSTIC_KINDS,
          "Expected compilation to fail, but it succeeded."
      );
    }

    return myself;
  }

  /**
   * Get assertions for diagnostics.
   *
   * @return assertions for the diagnostics.
   * @throws AssertionError if the compilation was null.
   */
  public TraceDiagnosticListAssert diagnostics() {
    isNotNull();
    return new TraceDiagnosticListAssert(actual.getDiagnostics());
  }

  /**
   * Perform assertions on the given package group, if it has been configured.
   *
   * <p>If not configured, this will return assertions on a {@code null} value instead.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   * @throws AssertionError           if the compilation was null, or no group for the location
   *                                  was found.
   * @throws IllegalArgumentException if the location was
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}
   *                                  or {@link Location#isOutputLocation() an output location}.
   * @throws NullPointerException     if the provided location object is null.
   */
  public PackageContainerGroupAssert packageGroup(Location location) {
    requireNonNull(location, "location must not be null");

    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Expected location " + location + " to not be module-oriented"
      );
    }

    if (location.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Expected location " + location + " to not be an output location"
      );
    }

    isNotNull();

    var group = actual.getFileManager().getPackageContainerGroup(location);
    assertLocationExists(location, group);
    return new PackageContainerGroupAssert(group);
  }

  /**
   * Perform assertions on the given module group, if it has been configured.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   * @throws AssertionError           if the compilation was null, or no group for the location
   *                                  was found.
   * @throws IllegalArgumentException if the location is not
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}.
   * @throws NullPointerException     if the provided location object is null.
   */
  public ModuleContainerGroupAssert moduleGroup(Location location) {
    requireNonNull(location, "location must not be null");

    if (!location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Expected location " + location.getName() + " to be module-oriented"
      );
    }

    if (location.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Expected location " + location.getName() + " to not be an output location"
      );
    }

    isNotNull();

    var group = actual.getFileManager().getModuleContainerGroup(location);
    assertLocationExists(location, group);
    return new ModuleContainerGroupAssert(group);
  }

  /**
   * Perform assertions on the given output group, if it has been configured.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   * @throws AssertionError           if the compilation was null, or no group for the location
   *                                  was found.
   * @throws IllegalArgumentException if the location is not
   *                                  {@link Location#isOutputLocation() an output location}.
   * @throws NullPointerException     if the provided location object is null.
   */
  public OutputContainerGroupAssert outputGroup(Location location) {
    requireNonNull(location, "location must not be null");

    if (!location.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Expected location " + location.getName() + " to be an output location"
      );
    }

    isNotNull();

    var group = actual.getFileManager().getOutputContainerGroup(location);
    assertLocationExists(location, group);
    return new OutputContainerGroupAssert(group);
  }

  /**
   * Get assertions on the path containing class outputs, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the class outputs.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   */
  public OutputContainerGroupAssert classOutput() {
    return outputGroup(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get assertions on the path containing generated source outputs, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the source outputs.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   */
  public OutputContainerGroupAssert sourceOutput() {
    return outputGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get assertions on the path containing header outputs, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the header outputs.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   * @deprecated this method is rarely needed, so is being removed in v1.0.0. Use
   * {@link #outputGroup(Location)}, passing {@link StandardLocation#NATIVE_HEADER_OUTPUT} as the
   * first parameter instead.
   */
  @Deprecated(since = "0.6.0", forRemoval = true)
  @SuppressWarnings("DeprecatedIsStillUsed")
  public OutputContainerGroupAssert generatedHeaders() {
    return outputGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Get assertions on the path containing the class path, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the class path.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   */
  public PackageContainerGroupAssert classPath() {
    return packageGroup(StandardLocation.CLASS_PATH);
  }

  /**
   * Get assertions on the path containing the source path, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the source path.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   */
  public PackageContainerGroupAssert sourcePath() {
    return packageGroup(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get assertions on the path containing the source path, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the source path.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   */
  public ModuleContainerGroupAssert moduleSourcePath() {
    return moduleGroup(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get assertions on the path containing the module path, if it exists.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @return the assertions to perform on the module path.
   * @throws AssertionError if the compilation was null, or no group for the location
   *                        was found.
   */
  public ModuleContainerGroupAssert modulePath() {
    return moduleGroup(StandardLocation.MODULE_PATH);
  }

  private void failWithDiagnostics(
      Collection<? extends Kind> kindsToDisplay,
      String message,
      Object... args
  ) {
    var diagnostics = actual
        .getDiagnostics()
        .stream()
        .filter(diagnostic -> kindsToDisplay.contains(diagnostic.getKind()))
        .collect(toUnmodifiableList());

    failWithMessage(
        "%s\n\nDiagnostics:\n%s",
        message,
        TraceDiagnosticListRepresentation.getInstance().toStringOf(diagnostics)
    );
  }

  private void assertLocationExists(Location location, ContainerGroup group) {
    if (group == null) {
      throw new AssertionError("No location named " + location.getName() + " exists");
    }
  }
}
