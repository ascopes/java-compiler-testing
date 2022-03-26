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

package com.github.ascopes.jct.paths;

import com.github.ascopes.jct.intern.StringSlicer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods that define special JVM locations that are specific to how the current JVM was
 * invoked, or what platform it is built on.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class SpecialLocations {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpecialLocations.class);

  private SpecialLocations() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Get the paths that would be associated with {@link javax.tools.StandardLocation#SYSTEM_MODULES}
   * in the standard compiler implementation of OpenJDK.
   *
   * <p>In Java 9 and above, these appear to be stored in a {@code JIMAGE} file located at
   * {@code ${JAVA_HOME}/lib/modules}.
   *
   * <p>See {@code com.sun.tools.javac.file.JRTIndex} within the {@code jdk.compiler} module to
   * read the OpenJDK equivalent of this.
   *
   * @return a list across the runtime paths.
   */
  public static List<Path> javaRuntimeLocations() {
    // Had to do a load of digging around the OpenJDK compiler implementation to work this out, and
    // I don't know if this will work on all JDK installations yet.
    var uri = URI.create("jrt:/");
    return List.of(Path.of(uri));
  }

  /**
   * Get the classpath of the current JVM. This will usually include any dependencies you may have
   * loaded into memory, and may refer to directories of {@code *.class} files, {@code *.war} files,
   * or {@code *.jar} files.
   *
   * <p>This corresponds to the {@link javax.tools.StandardLocation#CLASS_PATH} location.
   *
   * @return a list across the paths.
   */
  public static List<Path> currentClassPathLocations() {
    return createPaths(ManagementFactory.getRuntimeMXBean().getClassPath());
  }

  /**
   * Get the boot classpath of the current JVM. This will usually include any dependencies you may
   * have loaded into memory, and may refer to directories of {@code *.class} files, {@code *.war}
   * files, or {@code *.jar} files.
   *
   * <p>This corresponds to the {@link javax.tools.StandardLocation#PLATFORM_CLASS_PATH} location.
   *
   * @return a list across the paths.
   */
  public static List<Path> currentPlatformClassPathLocations() {
    try {
      return createPaths(ManagementFactory.getRuntimeMXBean().getBootClassPath());
    } catch (UnsupportedOperationException ex) {
      LOGGER.debug("Platform classpath (boot classpath) is not supported on this JVM", ex);
      return List.of();
    }
  }

  private static List<Path> createPaths(String raw) {
    return new StringSlicer(System.getProperty("path.separator", File.pathSeparator))
        .splitToStream(raw)
        .map(Path::of)
        .collect(Collectors.toList());
  }
}
