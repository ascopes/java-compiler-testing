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
package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.assertions.impl.DiagnosticListRepresentation;
import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions that apply to a {@link JctCompilation}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
@SuppressWarnings("UnusedReturnValue")
public final class CompilationAssert extends AbstractAssert<CompilationAssert, JctCompilation> {

  private static final Set<Kind> WARNING_DIAGNOSTIC_KINDS = Stream
      .of(Kind.WARNING, Kind.MANDATORY_WARNING)
      .collect(Collectors.toUnmodifiableSet());

  private static final Set<Kind> ERROR_DIAGNOSTIC_KINDS = Stream
      .of(Kind.ERROR)
      .collect(Collectors.toUnmodifiableSet());

  private static final Set<Kind> WARNING_AND_ERROR_DIAGNOSTIC_KINDS = Stream
      .of(WARNING_DIAGNOSTIC_KINDS, ERROR_DIAGNOSTIC_KINDS)
      .flatMap(Set::stream)
      .collect(Collectors.toUnmodifiableSet());

  /**
   * Initialize this compilation assertion.
   *
   * @param value the value to assert on.
   */
  public CompilationAssert(JctCompilation value) {
    super(value, CompilationAssert.class);
  }

  /**
   * Assert that the compilation was successful.
   *
   * @return this assertion object.
   */
  public CompilationAssert isSuccessful() {
    if (actual.isFailure()) {
      // If we have error diagnostics, add them to the error message to provide helpful debugging
      // information. If we are treating warnings as errors, then we want to include those in this
      // as well.
      Predicate<TraceDiagnostic<?>> isErrorDiagnostic = actual.isFailOnWarnings()
          ? diag -> WARNING_AND_ERROR_DIAGNOSTIC_KINDS.contains(diag.getKind())
          : diag -> ERROR_DIAGNOSTIC_KINDS.contains(diag.getKind());

      var diagnostics = actual
          .getDiagnostics()
          .stream()
          .filter(isErrorDiagnostic)
          .collect(Collectors.toUnmodifiableList());

      failWithDiagnostics(diagnostics, "Expected a successful compilation, but it failed.");
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
   */
  public CompilationAssert isSuccessfulWithoutWarnings() {
    isSuccessful();
    diagnostics().hasNoErrorsOrWarnings();
    return myself;
  }

  /**
   * Assert that the compilation was a failure.
   *
   * @return this assertion object.
   */
  public CompilationAssert isFailure() {
    if (actual.isSuccessful()) {
      var warnings = actual
          .getDiagnostics()
          .stream()
          .filter(kind -> WARNING_DIAGNOSTIC_KINDS.contains(kind.getKind()))
          .collect(Collectors.toUnmodifiableList());

      failWithDiagnostics(warnings, "Expected compilation to fail, but it succeeded.");
    }

    return myself;
  }

  /**
   * Get assertions for diagnostics.
   *
   * @return assertions for the diagnostics.
   */
  public DiagnosticListAssert diagnostics() {
    return new DiagnosticListAssert(actual.getDiagnostics());
  }

  /**
   * Assert that the file manager is closed.
   *
   * @return this assertion object to perform further assertions upon, if desired.
   * @throws AssertionError if the file manager is unexpectedly open.
   */
  public CompilationAssert fileManagerIsClosed() {
    if (!actual.getFileManager().isClosed()) {
      throw failure("Expected file manager to be closed but it was open");
    }

    return myself;
  }

  /**
   * Assert that the file manager is open.
   *
   * @return this assertion object to perform further assertions upon, if desired.
   * @throws AssertionError if the file manager is unexpectedly closed.
   */
  public CompilationAssert fileManagerIsOpen() {
    if (actual.getFileManager().isClosed()) {
      throw failure("Expected file manager to be open but it was closed");
    }

    return myself;
  }

  /**
   * Perform assertions on the given package group, if it has been configured.
   *
   * <p>If not configured, this will return an empty optional.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   */
  public PackageContainerGroupAssert packageGroup(Location location) {
    return new PackageContainerGroupAssert(
        actual.getFileManager().getPackageContainerGroup(location)
    );
  }

  /**
   * Perform assertions on the given module group, if it has been configured.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   */
  public ModuleContainerGroupAssert moduleGroup(Location location) {
    return new ModuleContainerGroupAssert(
        actual.getFileManager().getModuleContainerGroup(location)
    );
  }

  /**
   * Perform assertions on the given output group, if it has been configured.
   *
   * <p>If not configured, the value being asserted on will be {@code null} in value.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   */
  public OutputContainerGroupAssert outputGroup(Location location) {
    return new OutputContainerGroupAssert(
        actual.getFileManager().getOutputContainerGroup(location)
    );
  }

  /**
   * Get assertions on the path containing class outputs, if it exists.
   *
   * @return the assertions to perform on the class outputs.
   */
  public OutputContainerGroupAssert classOutput() {
    return outputGroup(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get assertions on the path containing generated source outputs, if it exists.
   *
   * @return the assertions to perform on the source outputs.
   */
  public OutputContainerGroupAssert sourceOutput() {
    return outputGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get assertions on the path containing header outputs, if it exists.
   *
   * @return the assertions to perform on the header outputs.
   */
  public OutputContainerGroupAssert generatedHeaders() {
    return outputGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Get assertions on the path containing the class path, if it exists.
   *
   * @return the assertions to perform on the class path.
   */
  public PackageContainerGroupAssert classPath() {
    return packageGroup(StandardLocation.CLASS_PATH);
  }

  /**
   * Get assertions on the path containing the source path, if it exists.
   *
   * @return the assertions to perform on the source path, or an empty optional if no group exists
   *     for that location.
   */
  public PackageContainerGroupAssert sourcePath() {
    return packageGroup(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get assertions on the path containing the source path, if it exists.
   *
   * @return the assertions to perform on the source path, or an empty optional if no group exists
   *     for that location.
   */
  public ModuleContainerGroupAssert moduleSourcePath() {
    return moduleGroup(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get assertions on the path containing the module path, if it exists.
   *
   * @return the assertions to perform on the module path, or an empty optional if no group exists
   *     for that location.
   */
  public ModuleContainerGroupAssert modulePath() {
    return moduleGroup(StandardLocation.MODULE_PATH);
  }

  private void failWithDiagnostics(
      List<? extends TraceDiagnostic<?>> diagnostics,
      String message,
      Object... args
  ) {
    if (diagnostics.isEmpty()) {
      failWithMessage(message, args);
    } else {
      var fullMessage = String.join(
          "\n\n",
          args.length > 0
              ? String.format(message, args)
              : message,
          "Diagnostics:",
          DiagnosticListRepresentation.getInstance().toStringOf(diagnostics)
      );

      failWithMessage(fullMessage);
    }
  }
}
