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
package org.example;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This should pass error-prone because it always closes an auto-closeable resource.
 */
public class HappyCase {

  /**
   * Do something.
   *
   * @param args arguments.
   * @throws IOException any exception.
   */
  public static void main(String[] args) throws IOException {
    try (InputStream inputStream = inputStream()) {
      inputStream.read();
    }
  }

  @MustBeClosed
  private static InputStream inputStream() throws IOException {
    Path tempFile = Files.createTempFile("foo", "bar");
    return Files.newInputStream(tempFile);
  }
}
