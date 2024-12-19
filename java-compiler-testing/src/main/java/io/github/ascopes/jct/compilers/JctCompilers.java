/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.compilers;

import io.github.ascopes.jct.compilers.impl.javac.JavacJctCompilerImpl;
import io.github.ascopes.jct.utils.UtilityClass;
import java.lang.reflect.InvocationTargetException;

/**
 * Helpers to create new compiler instances.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JctCompilers extends UtilityClass {

  private JctCompilers() {
    // Static-only class.
  }

  /**
   * Create a new instance of the default platform compiler that is part of the JDK ({@code javac}
   * on OpenJDK-derived implementations).
   *
   * @return the compiler instance.
   * @since 0.2.0
   */
  public static JctCompiler newPlatformCompiler() {
    return new JavacJctCompilerImpl();
  }

  /**
   * Create a new instance of the ECJ compiler.
   *
   * @return the compiler instance.
   * @throws UnsupportedOperationException if the current platform does not support ECJ (e.g.
   *     because it is prior to Java 17).
   * @since TBC
   */
  public static JctCompiler newEcjCompiler() {
    try {
      // Use reflection to avoid eagerly class-loading the Java-17 only class that may not be
      // available on Java 11.
      // We can remove this hack once Java 11 is no longer supported.
      var thisClass = JctCompilers.class;
      var constructor = thisClass.getClassLoader()
          .loadClass(thisClass.getPackageName() + ".impl.ecj.EcjJctCompilerImpl")
          .getConstructor();
      constructor.setAccessible(true);
      return (JctCompiler) constructor.newInstance();
    } catch (NoClassDefFoundError | ClassNotFoundException ex) {
      throw new UnsupportedOperationException(
          "ECJ components were not found. Ensure you are running on at least Java 17, "
              + "and have included mvn:org.eclipse.jdt:ecj in your project dependencies.",
          ex
      );
    } catch (ReflectiveOperationException ex) {
      throw new UnsupportedOperationException(
          "Failed to load various ECJ components correctly. This is probably a bug.",
          ex
      );
    }
  }
}
