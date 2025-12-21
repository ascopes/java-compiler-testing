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
package io.github.ascopes.jct.filemanagers.config;

import io.github.ascopes.jct.filemanagers.JctFileManager;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection representing a chain of configurers to apply to a file manager.
 *
 * <p>This is used to provide a set of ordered configuration operations to perform on newly
 * created file managers to prepare them for consumption in test cases. Common operations
 * may include:
 *
 * <ul>
 *   <li>Automatically configuring the annotation processor paths;</li>
 *   <li>Creating empty locations to prevent compilers raising exceptions;</li>
 *   <li>Installing logging interceptors.</li>
 * </ul>
 *
 * <p>When configuring a file manager with this chain, each configurer is invoked in the
 * provided order. Configurers themselves must return a file manager as a result of the
 * configure operation. This operation may return the input file manager if the operation
 * mutated the input, or it may return a different file manager instance. In the latter
 * case, subsequent configuration operations internally will use the new file manager.
 * The last returned file manager in the chain will be used as the final returned result.
 * This enables configurers to wrap file managers in proxies or delegating implementations
 * to intercept or override existing behaviours.
 *
 * <p>This class is not thread-safe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class JctFileManagerConfigurerChain {

  private static final Logger log = LoggerFactory.getLogger(JctFileManagerConfigurerChain.class);

  private final LinkedList<JctFileManagerConfigurer> configurers;

  /**
   * Initialise this chain.
   */
  public JctFileManagerConfigurerChain() {
    configurers = new LinkedList<>();
  }

  /**
   * Add a configurer to the start of the chain.
   *
   * @param configurer the configurer to add.
   * @return this chain for further calls.
   */
  public JctFileManagerConfigurerChain addFirst(JctFileManagerConfigurer configurer) {
    configurers.addFirst(configurer);
    return this;
  }

  /**
   * Add a configurer to the end of the chain.
   *
   * @param configurer the configurer to add.
   * @return this chain for further calls.
   */
  public JctFileManagerConfigurerChain addLast(JctFileManagerConfigurer configurer) {
    configurers.addLast(configurer);
    return this;
  }

  /**
   * Get an immutable copy of the list of configurers.
   *
   * @return the list of configurers.
   */
  public List<JctFileManagerConfigurer> list() {
    return List.copyOf(configurers);
  }

  /**
   * Apply each configurer to the given file manager in order.
   *
   * @param fileManager the file manager to configure.
   * @return the configured file manager to use. This may or may not be the same object as the input
   *     parameter, depending on how the configurers manipulate the input object.
   */
  public JctFileManager configure(JctFileManager fileManager) {
    for (var configurer : configurers) {
      if (configurer.isEnabled()) {
        log.debug("Applying {} to file manager {}", configurer, fileManager);
        // Configurers can totally replace the existing file manager
        // if they choose.
        fileManager = configurer.configure(fileManager);
      } else {
        log.trace("Skipping {}", configurer);
      }
    }
    return fileManager;
  }
}
