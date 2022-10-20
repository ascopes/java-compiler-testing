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
package org.example;

import javax.inject.Inject;

/**
 * Dummy web server stub.
 */
public class WebServer {
  private final WebServerConfiguration config;

  /**
   * Initialise the web server.
   */
  @Inject
  public WebServer(WebServerConfiguration config) {
    this.config = config;
  }

  public void serve() {
    // do something.
  }
}
