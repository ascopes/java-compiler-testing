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

import static java.util.function.Predicate.not;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods that expose special JVM locations.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class SpecialLocationUtils extends UtilityClass {

  // Files we don't want to propagate by default as they may clash with the environment.
  private static final Set<String> BLACKLISTED_FILE_NAMES = Set.of(
      // IntelliJ's idea_rt.jar causes problems on IDEA 2022.3:
      //  - [ERROR] compiler.err.package.clash.from.requires.in.unnamed
      //       the unnamed module reads package com.intellij.rt.execution.junit from both idea.rt
      //       and junit.rt
      // - [ERROR] compiler.err.package.clash.from.requires
      //       module spring.core reads package com.intellij.rt.execution.junit from both idea.rt
      //       and junit.rt
      "idea_rt.jar"
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(SpecialLocationUtils.class);
  private static final String NO_PATH = "";
  private static final URI JAVA_RUNTIME_URI = URI.create("jrt:/");
  private static final String JDK_MODULE_PROPERTY = "jdk.module.path";
  private static final StringSlicer SEPARATOR = new StringSlicer(
      System.getProperty("path.separator", File.pathSeparator)
  );

  private SpecialLocationUtils() {
    // Disallow initialisation.
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
    return List.of(Path.of(JAVA_RUNTIME_URI).toAbsolutePath());
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
   * Get the module path of the current JVM. This will usually include any dependencies you may have
   * loaded into memory, and may refer to directories of {@code *.class} files, {@code *.war} files,
   * or {@code *.jar} files.
   *
   * <p>This corresponds to the {@link javax.tools.StandardLocation#MODULE_PATH} location, but
   * is also added in the {@link javax.tools.StandardLocation#CLASS_PATH} to handle some otherwise
   * confusing behaviours.
   *
   * @return a list across the paths.
   */
  public static List<Path> currentModulePathLocations() {
    return createPaths(System.getProperty(JDK_MODULE_PROPERTY, NO_PATH));
  }

  /**
   * Get the boot classpath of the current JVM. This will usually include any dependencies you may
   * have loaded into memory, and may refer to directories of {@code *.class} files, {@code *.war}
   * files, or {@code *.jar} files.
   *
   * <p>This corresponds to the {@link javax.tools.StandardLocation#PLATFORM_CLASS_PATH} location.
   *
   * <p>Most OpenJDK implementations do not appear to support this. In this case, an empty list
   * will be returned here.
   *
   * @return a list across the paths.
   */
  public static List<Path> currentPlatformClassPathLocations() {
    var mxBean = ManagementFactory.getRuntimeMXBean();

    if (mxBean.isBootClassPathSupported()) {
      LOGGER.debug("Platform (boot) classpath is supported on this JVM, so will be inspected");
      return createPaths(mxBean.getBootClassPath());
    }

    LOGGER.trace("Platform (boot) classpath is not supported on this JVM, so will be ignored");
    return List.of();
  }

  private static List<Path> createPaths(String raw) {
    return SEPARATOR
        .splitToStream(raw)
        .filter(not(String::isBlank))
        .map(Path::of)
        .filter(not(SpecialLocationUtils::isBlacklistedFile))
        // We have to check this, annoyingly, because some tools like Maven (Surefire) will report
        // paths that don't actually exist to the class path, and Java will just ignore this
        // normally. It will cause random failures during builds, however, if directories such as
        // src/main/java do not exist.
        .distinct()
        .filter(SpecialLocationUtils::exists)
        .collect(Collectors.toUnmodifiableList());
  }

  private static boolean exists(Path path) {
    if (Files.exists(path)) {
      return true;
    }

    LOGGER.trace("Environment-provided path {} does not exist, so will be skipped", path);
    return false;
  }

  private static boolean isBlacklistedFile(Path path) {
    var fileName = path.getFileName().toString();

    if (BLACKLISTED_FILE_NAMES.contains(fileName)) {
      LOGGER.debug("Excluding {} from classpath as it is a blacklisted file", fileName);
      return true;
    }

    return false;
  }
}
