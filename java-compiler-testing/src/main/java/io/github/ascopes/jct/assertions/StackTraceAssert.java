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

package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.assertions.repr.StackTraceRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractListAssert;

/**
 * Assertions for a list of {@link StackTraceElement stack trace frames}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @deprecated I have put up a pull request for AssertJ to support this functionality in AssertJ
 *     Core. Once this is merged, this class will be removed from this API.
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public final class StackTraceAssert
    extends AbstractListAssert<StackTraceAssert, List<? extends StackTraceElement>, StackTraceElement, StackTraceElementAssert> {

  /**
   * Initialize a new assertions object.
   *
   * @param actual the list of stack trace elements to assert upon.
   */
  public StackTraceAssert(List<? extends StackTraceElement> actual) {
    super(actual, StackTraceAssert.class);
    info.useRepresentation(StackTraceRepresentation.getInstance());
  }

  @Override
  @SuppressWarnings("removal")
  protected StackTraceElementAssert toAssert(StackTraceElement value, String description) {
    return new StackTraceElementAssert(value).describedAs(description);
  }

  @Override
  protected StackTraceAssert newAbstractIterableAssert(
      Iterable<? extends StackTraceElement> iterable
  ) {
    var list = new ArrayList<StackTraceElement>();
    iterable.forEach(list::add);
    return new StackTraceAssert(list);
  }
}
