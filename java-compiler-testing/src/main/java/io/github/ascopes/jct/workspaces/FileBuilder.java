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
package io.github.ascopes.jct.workspaces;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import javax.annotation.WillClose;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Chainable builder for creating individual files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface FileBuilder {

  /**
   * Create the file with the given contents.
   *
   * @param lines the lines to write using the default charset.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory withContents(String... lines);

  /**
   * Create the file with the given contents.
   *
   * @param charset the character encoding to use.
   * @param lines   the lines to write.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory withContents(Charset charset, String... lines);

  /**
   * Create the file with the given byte contents.
   *
   * @param contents the bytes to write.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory withContents(byte[] contents);

  /**
   * Copy a resource from the class loader on the current thread into the file system.
   *
   * @param resource the resource to copy.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromClassPath(String resource);

  /**
   * Copy a resource from the given class loader into the file system.
   *
   * @param classLoader the class loader to use.
   * @param resource    the resource to copy.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromClassPath(ClassLoader classLoader, String resource);

  /**
   * Copy the contents from the given file into the file system.
   *
   * @param file the file to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromFile(File file);

  /**
   * Copy the contents from the given path into the file system.
   *
   * @param file the file to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromFile(Path file);

  /**
   * Copy the contents from the given URL into the file system.
   *
   * @param url the URL to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromUrl(URL url);

  /**
   * Create an empty file with nothing in it.
   *
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory thatIsEmpty();

  /**
   * Copy the contents from the given input stream into the file system.
   *
   * <p>The input stream will be closed when reading completes.
   *
   * @param inputStream the input stream to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory fromInputStream(@WillClose InputStream inputStream);
}
