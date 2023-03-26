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
package io.github.ascopes.jct.tests.integration;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abstract base class for integration tests to use.
 *
 * @author Ashley Scopes
 */
public abstract class AbstractIntegrationTest {

  /**
   * Get the resources directory for this test. This will be a directory named the full
   * canonical class name.
   *
   * @return the resource directory path.
   */
  protected Path resourcesDirectory() {
    var path = Path.of("src", "test", "resources");
    for (var part : getClass().getCanonicalName().split("[./]")) {
      path = path.resolve(part);
    }

    if (!Files.isDirectory(path)) {
      throw new IllegalStateException(
          "Please ensure the directory " + path + " exists, and try again"
      );
    }

    return path;
  }
}
