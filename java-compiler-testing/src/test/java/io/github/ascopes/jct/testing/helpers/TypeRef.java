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

package io.github.ascopes.jct.testing.helpers;

import java.lang.reflect.ParameterizedType;

/**
 * A reified type reference that can be anonymously subclassed to provide reflection data about the
 * parameterized type.
 *
 * @param <T> the type to reference.
 * @author Ashley Scopes.
 */
public abstract class TypeRef<T> {

  private final Class<T> type;

  /**
   * Initialize the reified type reference.
   */
  @SuppressWarnings("unchecked")
  public TypeRef() {
    var selfType = getClass().getGenericSuperclass();
    if (selfType instanceof Class<?>) {
      throw new IllegalArgumentException(
          "No type information found, only found class of type " + ((Class<?>) selfType).getName()
      );
    }

    var parameterizedType = ((ParameterizedType) selfType).getActualTypeArguments()[0];

    type = parameterizedType instanceof Class<?>
        ? (Class<T>) parameterizedType
        : (Class<T>) ((ParameterizedType) parameterizedType).getRawType();
  }

  public Class<T> getType() {
    return type;
  }
}
