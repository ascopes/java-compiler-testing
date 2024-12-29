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
package org.example;

import java.util.Locale;
import java.util.function.Function;

/**
 * Configuration properties for a web server.
 */
public class WebServerConfiguration {

  final String basePath;
  final Integer port;
  final Boolean https;

  /**
   * Initialise the web server configuration.
   */
  public WebServerConfiguration() {
    basePath = propertyOrEnv("web.server.base.path", Function.identity());
    port = propertyOrEnv("web.server.port", Integer::parseInt);
    https = propertyOrEnv("web.server.https", Boolean::parseBoolean);
  }

  static <T> T propertyOrEnv(String name, Function<String, T> converter) {
    String raw = System.getProperty(name);

    if (raw == null) {
      System.getenv(name.replace('.', '_').toUpperCase(Locale.ROOT));
    }

    if (raw == null) {
      return null;
    }

    return converter.apply(raw);
  }
}
