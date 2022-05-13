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

package io.github.ascopes.jct.intern;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy for creating file system links where possible.
 *
 * <p>Windows cannot create symbolic links without root, but can create hard links.
 *
 * <p>Hard links will often not work across separate mount points on Linux.
 *
 * <p>Some operating systems will support neither hard nor symbolic links, so we fall back
 * to copying the target file to the link location in these cases.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class PlatformLinkStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlatformLinkStrategy.class);
  private final Call impl;
  private final String name;

  /**
   * Initialize this strategy.
   */
  public PlatformLinkStrategy() {
    if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
      // Windows cannot create symbolic links without root.
      impl = Files::createLink;
      name = "hard link";
    } else {
      // /tmp on UNIX is usually a separate device, so hard links won't work.
      impl = Files::createSymbolicLink;
      name = "symbolic link";
    }
  }

  /**
   * Attempt to create a link, falling back to just copying the file if this is not possible.
   *
   * @param link   the link to create.
   * @param target the target to link to.
   * @return the path to the created link or file.
   * @throws IOException if something goes wrong.
   */
  public Path createLinkOrCopy(Path link, Path target) throws IOException {
    try {
      var result = impl.createLink(link, target);
      LOGGER.trace("Created {} from {} to {}", name, link, target);
      return result;
    } catch (UnsupportedOperationException | FileSystemException ex) {
      LOGGER.trace(
          "Failed to create {} from {} to {}, falling back to copying files",
          name,
          link,
          target,
          ex
      );
      return Files.copy(target, link);
    }
  }

  @FunctionalInterface
  private interface Call {

    /**
     * Create the link.
     *
     * @param link   the link to create.
     * @param target the target to link to.
     * @return the link.
     * @throws IOException if the link fails to be created.
     */
    Path createLink(Path link, Path target) throws IOException;
  }
}
