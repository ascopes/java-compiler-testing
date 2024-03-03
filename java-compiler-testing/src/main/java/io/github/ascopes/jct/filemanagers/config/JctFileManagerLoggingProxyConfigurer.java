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
package io.github.ascopes.jct.filemanagers.config;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.impl.LoggingFileManagerProxy;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File manager configurer that optionally wraps the file manager in a logging proxy that outputs
 * interaction details to the console logs.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JctFileManagerLoggingProxyConfigurer implements JctFileManagerConfigurer {

  private static final Logger log = LoggerFactory
      .getLogger(JctFileManagerLoggingProxyConfigurer.class);

  private final JctCompiler compiler;

  /**
   * Initialise this configurer.
   *
   * @param compiler the compiler to apply to the file manager.
   */
  public JctFileManagerLoggingProxyConfigurer(JctCompiler compiler) {
    this.compiler = compiler;
  }

  @Override
  public JctFileManager configure(JctFileManager fileManager) {
    log.debug("Configuring compiler operation audit logging");

    switch (compiler.getFileManagerLoggingMode()) {
      case STACKTRACES:
        log.trace("Decorating file manager {} in a logger proxy with stack traces", fileManager);
        return LoggingFileManagerProxy.wrap(fileManager, true);
      case ENABLED:
        log.trace("Decorating file manager {} in a logger proxy", fileManager);
        return LoggingFileManagerProxy.wrap(fileManager, false);
      default:
        throw new IllegalStateException("Cannot configure logger proxy");
    }
  }

  @Override
  public boolean isEnabled() {
    return compiler.getFileManagerLoggingMode() != LoggingMode.DISABLED;
  }
}
