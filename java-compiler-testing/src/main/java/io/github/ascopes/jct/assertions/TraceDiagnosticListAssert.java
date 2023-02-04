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

import static io.github.ascopes.jct.utils.IterableUtils.combineOneOrMore;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.repr.TraceDiagnosticListRepresentation;
import io.github.ascopes.jct.utils.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractListAssert;

/**
 * Assertions for a list of diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class TraceDiagnosticListAssert
    extends
    AbstractListAssert<TraceDiagnosticListAssert, List<? extends TraceDiagnostic<? extends JavaFileObject>>, TraceDiagnostic<? extends JavaFileObject>, TraceDiagnosticAssert> {

  /**
   * Initialize this assertion.
   *
   * @param traceDiagnostics the diagnostics to perform assertions on.
   */
  public TraceDiagnosticListAssert(
      List<? extends TraceDiagnostic<? extends JavaFileObject>> traceDiagnostics
  ) {
    super(traceDiagnostics, TraceDiagnosticListAssert.class);
    info.useRepresentation(TraceDiagnosticListRepresentation.getInstance());
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} across all diagnostics that have the {@link Kind#ERROR}
   * kind.
   *
   * @return the assertion object for {@link Kind#ERROR} diagnostics.
   * @throws AssertionError if the list is null.
   */
  public TraceDiagnosticListAssert errors() {
    return filteringByKinds(DiagnosticKindAssert.ERROR_DIAGNOSTIC_KINDS);
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} across all diagnostics that have the
   * {@link Kind#WARNING} or {@link Kind#MANDATORY_WARNING} kind.
   *
   * @return the assertion object for {@link Kind#WARNING} and {@link Kind#MANDATORY_WARNING}
   *     diagnostics.
   * @throws AssertionError if the list is null.
   */
  public TraceDiagnosticListAssert warnings() {
    return filteringByKinds(DiagnosticKindAssert.WARNING_DIAGNOSTIC_KINDS);
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} across all diagnostics that have the
   * {@link Kind#WARNING} kind.
   *
   * @return the assertion object for {@link Kind#WARNING} diagnostics.
   * @throws AssertionError if the list is null.
   */
  public TraceDiagnosticListAssert customWarnings() {
    return filteringByKinds(Kind.WARNING);
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} across all diagnostics that have the
   * {@link Kind#MANDATORY_WARNING} kind.
   *
   * @return the assertion object for {@link Kind#MANDATORY_WARNING} diagnostics.
   * @throws AssertionError if the list is null.
   */
  public TraceDiagnosticListAssert mandatoryWarnings() {
    return filteringByKinds(Kind.MANDATORY_WARNING);
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} across all diagnostics that have the {@link Kind#NOTE}
   * kind.
   *
   * @return the assertion object for {@link Kind#NOTE} diagnostics.
   * @throws AssertionError if the list is null.
   */
  public TraceDiagnosticListAssert notes() {
    return filteringByKinds(Kind.NOTE);
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} across all diagnostics that have the {@link Kind#OTHER}
   * kind.
   *
   * @return the assertion object for {@link Kind#OTHER} diagnostics.
   * @throws AssertionError if the list is null.
   */
  public TraceDiagnosticListAssert others() {
    return filteringByKinds(Kind.OTHER);
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} that contains diagnostics corresponding to any of the
   * given {@link Kind kinds}.
   *
   * @param kind      the first kind to match.
   * @param moreKinds additional kinds to match.
   * @return the assertion object for the filtered diagnostics.
   * @throws AssertionError       if this list is null.
   * @throws NullPointerException if any of the kinds are null.
   */
  public TraceDiagnosticListAssert filteringByKinds(Kind kind, Kind... moreKinds) {
    requireNonNull(kind, "kind must not be null");
    requireNonNullValues(moreKinds, "moreKinds");
    return filteringByKinds(combineOneOrMore(kind, moreKinds));
  }


  /**
   * Get a {@link TraceDiagnosticListAssert} that contains diagnostics corresponding to any of the
   * given {@link Kind kinds}.
   *
   * @param kinds the kinds to match.
   * @return the assertion object for the filtered diagnostics.
   * @throws AssertionError       if this list is null.
   * @throws NullPointerException if any of the kinds are null.
   */
  public TraceDiagnosticListAssert filteringByKinds(Iterable<Kind> kinds) {
    requireNonNullValues(kinds, "kinds");
    return filteringBy(kind(kinds));
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} that contains diagnostics corresponding to none of the
   * given {@link Kind kinds}.
   *
   * @param kind      the first kind to ensure are not matched.
   * @param moreKinds additional kinds to ensure are not matched.
   * @return the assertion object for the filtered diagnostics.
   * @throws AssertionError       if this list is null.
   * @throws NullPointerException if any of the kinds are null.
   */
  public TraceDiagnosticListAssert excludingKinds(Kind kind, Kind... moreKinds) {
    requireNonNull(kind, "kind must not be null");
    requireNonNullValues(moreKinds, "moreKinds");
    return excludingKinds(combineOneOrMore(kind, moreKinds));
  }

  /**
   * Get a {@link TraceDiagnosticListAssert} that contains diagnostics corresponding to none of the
   * given {@link Kind kinds}.
   *
   * @param kinds the kinds to filter out.
   * @return the assertion object for the filtered diagnostics.
   * @throws AssertionError       if this list is null.
   * @throws NullPointerException if any of the kinds are null.
   */
  public TraceDiagnosticListAssert excludingKinds(Iterable<Kind> kinds) {
    requireNonNullValues(kinds, "kinds");
    return filteringBy(not(kind(kinds)));
  }

  /**
   * Assert that this list has no {@link Kind#ERROR} diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoErrors() {
    return hasNoDiagnosticsOfKinds(DiagnosticKindAssert.ERROR_DIAGNOSTIC_KINDS);
  }

  /**
   * Assert that this list has no {@link Kind#ERROR}, {@link Kind#WARNING}, or
   * {@link Kind#MANDATORY_WARNING} diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoErrorsOrWarnings() {
    return hasNoDiagnosticsOfKinds(DiagnosticKindAssert.WARNING_AND_ERROR_DIAGNOSTIC_KINDS);
  }

  /**
   * Assert that this list has no {@link Kind#WARNING} or {@link Kind#MANDATORY_WARNING}
   * diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoWarnings() {
    return hasNoDiagnosticsOfKinds(DiagnosticKindAssert.WARNING_DIAGNOSTIC_KINDS);
  }

  /**
   * Assert that this list has no {@link Kind#WARNING} diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoCustomWarnings() {
    return hasNoDiagnosticsOfKinds(Kind.WARNING);
  }

  /**
   * Assert that this list has no {@link Kind#MANDATORY_WARNING} diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoMandatoryWarnings() {
    return hasNoDiagnosticsOfKinds(Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that this list has no {@link Kind#NOTE} diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoNotes() {
    return hasNoDiagnosticsOfKinds(Kind.NOTE);
  }

  /**
   * Assert that this list has no {@link Kind#OTHER} diagnostics.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if this list is null.
   */
  public TraceDiagnosticListAssert hasNoOtherDiagnostics() {
    return hasNoDiagnosticsOfKinds(Kind.OTHER);
  }

  /**
   * Assert that this list has no diagnostics matching any of the given kinds.
   *
   * @param kind      the first kind to check for.
   * @param moreKinds any additional kinds to check for.
   * @return this assertion object for further call chaining.
   * @throws AssertionError       if the diagnostic list is null.
   * @throws NullPointerException if the kind or more kinds are null.
   */
  public TraceDiagnosticListAssert hasNoDiagnosticsOfKinds(Kind kind, Kind... moreKinds) {
    requireNonNull(kind, "kind must not be null");
    requireNonNullValues(moreKinds, "moreKinds");
    return hasNoDiagnosticsOfKinds(combineOneOrMore(kind, moreKinds));
  }

  /**
   * Assert that this list has no diagnostics matching any of the given kinds.
   *
   * @param kinds the kinds to check for.
   * @return this assertion object for further call chaining.
   * @throws AssertionError       if the diagnostic list is null.
   * @throws NullPointerException if any of the kinds are null.
   */
  public TraceDiagnosticListAssert hasNoDiagnosticsOfKinds(Iterable<Kind> kinds) {
    requireNonNullValues(kinds, "kinds");

    var actualDiagnostics = actual
        .stream()
        .filter(kind(kinds))
        .collect(Collectors.toList());

    if (!actualDiagnostics.isEmpty()) {
      var allKindsString = StreamSupport.stream(kinds.spliterator(), false)
          .map(next -> next.name().toLowerCase(Locale.ROOT).replace('_', ' '))
          .sorted()
          .collect(Collectors.collectingAndThen(
              Collectors.toUnmodifiableList(),
              names -> StringUtils.toWordedList(names, ", ", ", or ")
          ));

      failWithActualExpectedAndMessage(
          actualDiagnostics.size(),
          0,
          "Expected no %s diagnostics.\n\nDiagnostics:\n%s",
          allKindsString,
          TraceDiagnosticListRepresentation.getInstance().toStringOf(actualDiagnostics)
      );
    }

    return myself;
  }

  /**
   * Filter diagnostics by a given predicate and return an assertion object that applies to all
   * diagnostics that match that predicate.
   *
   * @param predicate the predicate to match.
   * @return the assertion object for the diagnostics that match.
   * @throws NullPointerException if the predicate is null.
   * @throws AssertionError       if the diagnostic list is null.
   */
  public TraceDiagnosticListAssert filteringBy(
      Predicate<TraceDiagnostic<? extends JavaFileObject>> predicate
  ) {
    requireNonNull(predicate, "predicate must not be null");

    isNotNull();

    return actual
        .stream()
        .filter(predicate)
        .collect(Collectors.collectingAndThen(
            Collectors.toUnmodifiableList(),
            TraceDiagnosticListAssert::new
        ));
  }

  @Override
  protected TraceDiagnosticAssert toAssert(
      TraceDiagnostic<? extends JavaFileObject> value,
      String description
  ) {
    return new TraceDiagnosticAssert(value).describedAs(description);
  }

  @Override
  protected TraceDiagnosticListAssert newAbstractIterableAssert(
      Iterable<? extends TraceDiagnostic<? extends JavaFileObject>> iterable
  ) {
    var list = new ArrayList<TraceDiagnostic<? extends JavaFileObject>>();
    iterable.forEach(list::add);
    return new TraceDiagnosticListAssert(list);
  }

  private Predicate<TraceDiagnostic<? extends JavaFileObject>> kind(Iterable<Kind> kinds) {
    var kindsSet = new LinkedHashSet<Kind>();
    kinds.forEach(kindsSet::add);

    return diagnostic -> kindsSet.contains(diagnostic.getKind());
  }
}
