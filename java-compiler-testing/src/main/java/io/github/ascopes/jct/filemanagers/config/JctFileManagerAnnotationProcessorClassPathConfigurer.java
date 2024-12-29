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
package io.github.ascopes.jct.filemanagers.config;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import java.util.Map;
import javax.tools.StandardLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurer for a file manager that makes annotation processors in the classpath accessible to the
 * annotation processor path.
 *
 * <p>If annotation processor discovery is disabled for dependencies, this will be skipped.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JctFileManagerAnnotationProcessorClassPathConfigurer
    implements JctFileManagerConfigurer {

  private static final Logger log = LoggerFactory
      .getLogger(JctFileManagerAnnotationProcessorClassPathConfigurer.class);

  private static final Map<StandardLocation, StandardLocation> INHERITED_AP_PATHS = Map.of(
      // https://stackoverflow.com/q/53084037
      // Seems that javac will always use the classpath to implement this behaviour, and never
      // the module path. Let's keep this simple and mimic this behaviour. If someone complains
      // about it being problematic in the future, then I am open to change how this works to
      // keep it sensible.
      // (from -> to)
      StandardLocation.CLASS_PATH, StandardLocation.ANNOTATION_PROCESSOR_PATH
  );

  private final JctCompiler compiler;

  /**
   * Initialise the configurer with the desired compiler.
   *
   * @param compiler the compiler to wrap.
   */
  public JctFileManagerAnnotationProcessorClassPathConfigurer(JctCompiler compiler) {
    this.compiler = compiler;
  }

  @Override
  public JctFileManager configure(JctFileManager fileManager) {
    log.debug("Configuring annotation processor discovery mechanism");

    switch (compiler.getAnnotationProcessorDiscovery()) {
      case ENABLED:
        log.trace("Annotation processor discovery is enabled, ensuring empty location exists");

        INHERITED_AP_PATHS.values().forEach(fileManager::createEmptyLocation);

        return fileManager;

      case INCLUDE_DEPENDENCIES:
        log.trace("Annotation processor discovery is enabled, copying classpath dependencies "
            + "into the annotation processor path");

        INHERITED_AP_PATHS.forEach(fileManager::copyContainers);
        INHERITED_AP_PATHS.values().forEach(fileManager::createEmptyLocation);

        return fileManager;

      default:
        throw new JctIllegalInputException("Cannot configure annotation processor discovery");
    }
  }

  @Override
  public boolean isEnabled() {
    return compiler.getAnnotationProcessorDiscovery() != AnnotationProcessorDiscovery.DISABLED;
  }
}
