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

package com.github.ascopes.jct.test.helpers;

import java.lang.reflect.Modifier;

/**
 * Helper to access internal details via reflection.
 *
 * @author Ashley Scopes
 */
public final class ReflectionAccess {

  private ReflectionAccess() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Get a field from the given object.
   *
   * <p>This completely ignores all type safety, and should only be used as a last resort.
   *
   * @param object the object.
   * @param name   the name of the field.
   * @param <T>    the type of the field.
   * @return the value of the field.
   */
  public static <T> T getField(Object object, String name) {
    try {
      var field = object.getClass().getDeclaredField(name);
      if (!field.trySetAccessible()) {
        throw new IllegalStateException("Cannot set accessibility of " + name + " on " + object);
      }

      if (Modifier.isStatic(field.getModifiers())) {
        // Static fields should only consume a null param for the object.
        object = null;
      }

      @SuppressWarnings("unchecked")
      var value = (T) field.get(object);

      return value;
    } catch (NoSuchFieldException ex) {
      throw new IllegalArgumentException("No such field " + name + " on " + object, ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Failed to get " + name + " on " + object, ex);
    }
  }

  /**
   * Set a field in a given object.
   *
   * <p>This completely ignores all type safety, and should only be used as a last resort.
   *
   * @param object the object.
   * @param name   the name of the field.
   * @param value  the value of the field.
   */
  public static void setField(Object object, String name, Object value) {
    try {
      var field = object.getClass().getDeclaredField(name);
      if (!field.trySetAccessible()) {
        throw new IllegalStateException("Cannot set accessibility of " + name + " on " + object);
      }

      if (Modifier.isStatic(field.getModifiers())) {
        // Static fields should only consume a null param for the object.
        object = null;
      }

      field.set(object, value);
    } catch (NoSuchFieldException ex) {
      throw new IllegalArgumentException("No such field " + name + " on " + object, ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Failed to set " + name + " on " + object, ex);
    }
  }
}
