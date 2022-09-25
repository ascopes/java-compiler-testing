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
package io.github.ascopes.jct.testing.helpers;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Helper to provide reflective access to underlying elements.
 *
 * @author Ashley Scopes
 */
public final class ReflectiveAccess {

  private ReflectiveAccess() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Get the value of a field reflectively.
   *
   * @param object the object to get the field from.
   * @param name   the name of the field.
   * @param type   the type of the field to cast to.
   * @param <T>    the type of the field as a type parameter.
   * @return the field value.
   */
  public static <T> T getField(Object object, String name, Class<T> type) {
    Objects.requireNonNull(object);
    var objectType = object.getClass();

    try {
      var field = getFieldDescriptor(objectType, name);
      field.setAccessible(true);
      var rawValue = field.get(object);
      return type.cast(rawValue);
    } catch (Exception ex) {
      var newEx = new IllegalAccessError("Failed to get " + objectType.getName() + "#" + name);
      newEx.initCause(ex);
      throw newEx;
    }
  }

  private static Field getFieldDescriptor(Class<?> type, String name) throws NoSuchFieldException {
    // Feeling like JPMS makes all of this far more complicated than it needs to be.
    NoSuchFieldException cause = null;

    do {
      // Ensure we can access the internal fields.
      ReflectiveAccess.class
          .getModule()
          .addOpens(type.getPackageName(), type.getModule());

      try {
        return type.getDeclaredField(name);
      } catch (NoSuchFieldException ex) {
        cause = wrap(type, name, ex, cause);
        type = type.getSuperclass();
      }
    } while (type != null);

    throw Objects.requireNonNull(cause);
  }

  private static NoSuchFieldException wrap(
      Class<?> type,
      String name,
      NoSuchFieldException ex,
      Throwable previous
  ) {
    var newEx = new NoSuchFieldException(type.getName() + "#" + name);
    newEx.setStackTrace(ex.getStackTrace());
    newEx.initCause(previous);
    return newEx;
  }
}
