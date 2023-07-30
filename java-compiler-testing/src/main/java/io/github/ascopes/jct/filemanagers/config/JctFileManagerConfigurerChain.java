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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A chain of configurers to apply to a file manager.
 *
 * <p>While not designed for concurrent use, all operations are shielded by a
 * lock to guard against potentially confusing behaviour, should this component be shared across
 * multiple physical and virtual threads.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JctFileManagerConfigurerChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(JctFileManagerConfigurerChain.class);

  private final Lock lock;
  private final Deque<JctFileManagerConfigurer> configurers;

  /**
   * Initialise this chain.
   */
  public JctFileManagerConfigurerChain() {
    lock = new ReentrantLock();
    configurers = new ArrayDeque<>(16);
  }

  /**
   * Add a configurer to the start of the chain.
   *
   * @param configurer the configurer to add.
   * @return this chain for further calls.
   */
  public JctFileManagerConfigurerChain addFirst(JctFileManagerConfigurer configurer) {
    lock.lock();
    try {
      configurers.addFirst(configurer);
    } finally {
      lock.unlock();
    }
    return this;
  }

  /**
   * Add a configurer to the end of the chain.
   *
   * @param configurer the configurer to add.
   * @return this chain for further calls.
   */
  public JctFileManagerConfigurerChain addLast(JctFileManagerConfigurer configurer) {
    lock.lock();
    try {
      configurers.addLast(configurer);
    } finally {
      lock.unlock();
    }
    return this;
  }

  /**
   * Get an immutable copy of the list of configurers.
   *
   * @return the list of configurers.
   */
  public List<JctFileManagerConfigurer> list() {
    lock.lock();
    try {
      return List.copyOf(configurers);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Apply each configurer to the given file manager in order.
   *
   * @param fileManager the file manager to configure.
   * @return the configured file manager to use. This may or may not be the same object as the input
   *     parameter, depending on how the configurers manipulate the input object.
   */
  public JctFileManager configure(JctFileManager fileManager) {
    lock.lock();
    try {
      for (var configurer : configurers) {
        if (configurer.isEnabled()) {
          LOGGER.debug("Applying {} to file manager", configurer);
          fileManager = configurer.configure(fileManager);
        } else {
          LOGGER.trace("Skipping {}", configurer);
        }
      }
    } finally {
      lock.unlock();
    }

    return fileManager;
  }
}
