/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A chain of configurers to apply to a file manager.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.STABLE)
@NotThreadSafe
public final class JctFileManagerConfigurerChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(JctFileManagerConfigurerChain.class);

  private final Deque<JctFileManagerConfigurer> configurers;

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
        LOGGER.trace("Applying {} to file manager", configurer);
        fileManager = configurer.configure(fileManager);
      } else {
        LOGGER.trace("Skipping {}", configurer);
      }
    }

    return fileManager;
  }
}
