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
package io.github.ascopes.jct.containers.impl;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

/**
 * An extension of the Java {@link URLClassLoader} that wraps around container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class PackageContainerGroupUrlClassLoader extends URLClassLoader {

  /**
   * Initialise this class loader.
   *
   * @param group the container group to use.
   */
  public PackageContainerGroupUrlClassLoader(PackageContainerGroup group) {
    super(
        "Packages within " + group.getLocation().getName(),
        extractUrls(group),
        ClassLoader.getSystemClassLoader()
    );
  }

  private static URL[] extractUrls(PackageContainerGroup group) {
    return group.getPackages()
        .stream()
        .map(Container::getPathRoot)
        .map(PathRoot::getUrl)
        .map(PackageContainerGroupUrlClassLoader::fixMemoryFileSystemUrls)
        .toArray(URL[]::new);
  }

  // This can be removed once https://github.com/marschall/memoryfilesystem/pull/145/files is
  // addressed.
  private static URL fixMemoryFileSystemUrls(URL url) {
    if (url.getPath().endsWith("/") || isProbablyArchive(url)) {
      return url;
    }

    return uncheckedIo(() -> new URL(
        url.getProtocol(), url.getHost(), url.getPort(), url.getFile() + "/"
    ));
  }

  private static boolean isProbablyArchive(URL url) {
    return Stream
        .of(".jar", ".war", ".ear", ".zip")
        .anyMatch(url.getFile().toLowerCase(Locale.ROOT)::endsWith);
  }
}
