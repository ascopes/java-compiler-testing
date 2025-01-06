/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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

import java.util.List;
import java.util.stream.StreamSupport;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.AssertFactory;
import org.jspecify.annotations.Nullable;

//@formatter:off
/**
 * An implementation of {@link AbstractListAssert} that can perform type-specific assertions on the
 * members of the container being asserted upon.
 *
 * <p>This acts as a bridge with AssertJ now that
 * {@link org.assertj.core.api.FactoryBasedNavigableListAssert} has been deprecated. Users should
 * treat this object like any other kind of {@link AbstractListAssert} for all purposes.
 *
 * @param <E> the element type to assert upon.
 * @param <A> the assertion type for the element.
 * @author Ashley Scopes
 * @since 3.1.0
 */
public final class TypeAwareListAssert<E, A extends AbstractAssert<A, @Nullable E>>
    extends AbstractListAssert<TypeAwareListAssert<@Nullable E, A>, @Nullable List<? extends @Nullable E>, @Nullable E, A> {
  //@formatter:on

  private final AssertFactory<@Nullable E, A> assertFactory;

  TypeAwareListAssert(@Nullable List<? extends E> list, AssertFactory<E, A> assertFactory) {
    super(list, TypeAwareListAssert.class);
    this.assertFactory = assertFactory;
  }

  @Override
  protected A toAssert(@Nullable E value, String description) {
    return assertFactory.createAssert(value).describedAs(description);
  }

  @Override
  protected TypeAwareListAssert<@Nullable E, A> newAbstractIterableAssert(
      Iterable<? extends @Nullable E> iterable
  ) {
    var list = StreamSupport.stream(iterable.spliterator(), false).toList();
    return new TypeAwareListAssert<>(list, assertFactory);
  }
}
