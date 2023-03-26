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
package io.github.ascopes.jct.workspaces.impl;

import static io.github.ascopes.jct.utils.FileUtils.retrieveRequiredUrl;
import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.DirectoryBuilder;
import io.github.ascopes.jct.workspaces.FileBuilder;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base for implementing a reusable managed wrapper around a directory of some sort.
 *
 * <p>This is designed to simplify the creation of file and directory trees, and manage the release
 * of resources once no longer needed automatkeep test logic simple and clean.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public abstract class AbstractManagedDirectory implements ManagedDirectory {

  private final String name;
  private final Path rootDirectory;
  private final URI uri;
  private final URL url;

  /**
   * Initialise this abstract test directory.
   *
   * @param name          the name of the test directory.
   * @param rootDirectory the root directory of the test directory.
   */
  protected AbstractManagedDirectory(String name, Path rootDirectory) {
    this.name = requireNonNull(name, "name");
    this.rootDirectory = requireNonNull(rootDirectory, "rootDirectory");
    uri = this.rootDirectory.toUri();
    url = retrieveRequiredUrl(this.rootDirectory);
  }

  /**
   * {@inheritDoc}
   *
   * @return the bytes that make up the JAR that was created from this directory.
   */
  @Override
  public final byte[] asJar() {
    return uncheckedIo(() -> {
      try (var baos = new ByteArrayOutputStream()) {
        JarFactoryImpl.getInstance().createJarFrom(baos, getPath());
        return baos.toByteArray();
      }
    });
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code null} in all cases. This implementation cannot have a parent path.
   */
  @Nullable
  @Override
  public PathRoot getParent() {
    return null;
  }

  @Override
  public Path getPath() {
    return rootDirectory;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public URL getUrl() {
    return url;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public FileBuilder createFile(String fragment, String... fragments) {
    return new FileBuilderImpl(this, fragment, fragments);
  }

  @Override
  public DirectoryBuilder createDirectory(String first, String... rest) {
    return new DirectoryBuilderImpl(this, first, rest);
  }

  @Override
  public ManagedDirectory copyContentsFrom(String first, String... rest) {
    return rootDirectory().copyContentsFrom(first, rest);
  }

  @Override
  public ManagedDirectory copyContentsFrom(File dir) {
    return rootDirectory().copyContentsFrom(dir);
  }

  @Override
  public ManagedDirectory copyContentsFrom(Path rootDir) {
    return rootDirectory().copyContentsFrom(rootDir);
  }

  @Override
  public ManagedDirectory thatIsEmpty() {
    return this;
  }

  @Override
  public boolean equals(@Nullable Object that) {
    return that instanceof AbstractManagedDirectory
        && ((AbstractManagedDirectory) that).uri.equals(uri);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("name", name)
        .attribute("uri", uri)
        .toString();
  }

  private DirectoryBuilder rootDirectory() {
    return new DirectoryBuilderImpl(this, "");
  }
}
