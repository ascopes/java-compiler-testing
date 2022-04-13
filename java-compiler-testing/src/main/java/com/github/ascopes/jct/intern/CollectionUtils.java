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

package com.github.ascopes.jct.intern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Collection helper utilities.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class CollectionUtils {

  private CollectionUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Convert variadic arguments with an enforced first element into a list of those elements.
   *
   * <p>This pattern is used to ensure vararg overloads take at least one element, by enforcing
   * this at compile-time.
   *
   * @param first the enforced first element.
   * @param rest  the rest of the elements.
   * @param <T>   the type of the elements.
   * @return the list of the elements.
   */
  @SafeVarargs
  public static <T> List<T> combineOneOrMore(T first, T... rest) {
    var list = new ArrayList<T>();
    list.add(first);
    list.addAll(Arrays.asList(rest));
    return list;
  }
}
