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
package io.github.ascopes.jct.utils;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;


/**
 * Adapter that converts an {@link Iterator} into an {@link Enumeration} to provide compatibility
 * with ancient parts of the JDK.
 *
 * @param <T> the type of each element the iterator returns.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public class EnumerationAdapter<T> implements Enumeration<T> {

  private final Iterator<T> iterator;

  /**
   * Initialize the adapter.
   *
   * @param iterator the iterator to use.
   */
  public EnumerationAdapter(Iterator<T> iterator) {
    this.iterator = Objects.requireNonNull(iterator, "iterator");
  }

  @Override
  public boolean hasMoreElements() {
    return iterator.hasNext();
  }

  @Override
  public T nextElement() {
    return iterator.next();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("iterator", iterator)
        .toString();
  }
}
