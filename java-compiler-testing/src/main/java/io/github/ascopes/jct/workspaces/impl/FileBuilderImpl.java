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
package io.github.ascopes.jct.workspaces.impl;

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static io.github.ascopes.jct.utils.IterableUtils.requireAtLeastOne;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;

import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.workspaces.FileBuilder;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chainable builder for creating individual files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class FileBuilderImpl implements FileBuilder {

  private static final Logger log = LoggerFactory.getLogger(FileBuilderImpl.class);
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private final ManagedDirectory parent;
  private final Path targetPath;

  /**
   * Initialise a new file builder.
   *
   * @param parent the parent managed directory to chain calls back onto.
   * @param fragments the parts of the file path.
   */
  public FileBuilderImpl(ManagedDirectory parent, List<String> fragments) {
    requireNonNullValues(fragments, "fragments");
    requireAtLeastOne(fragments, "fragments");

    this.parent = parent;
    targetPath = FileUtils.resolvePathRecursively(parent.getPath(), fragments);
  }

  @Override
  public ManagedDirectory asJarFrom(PathRoot directory) {
    return asJarFrom(directory.getPath());
  }

  @Override
  public ManagedDirectory asJarFrom(Path directory) {
    uncheckedIo(() -> JarFactoryImpl.getInstance().createJarFrom(targetPath, directory));
    return parent;
  }

  @Override
  public ManagedDirectory copiedFromClassPath(ClassLoader classLoader, String resource) {
    return uncheckedIo(() -> {
      try (var input = classLoader.getResourceAsStream(resource)) {
        if (input == null) {
          throw new NoSuchFileException("classpath:" + resource);
        }

        return createFile(input);
      }
    });
  }

  @Override
  public ManagedDirectory copiedFromClassPath(String resource) {
    return copiedFromClassPath(currentCallerClassLoader(), resource);
  }

  @Override
  public ManagedDirectory copiedFromFile(File file) {
    return copiedFromFile(file.toPath());
  }

  @Override
  public ManagedDirectory copiedFromFile(Path file) {
    return uncheckedIo(() -> {
      try (var input = Files.newInputStream(file)) {
        return createFile(input);
      }
    });
  }

  @Override
  public ManagedDirectory copiedFromUrl(URL url) {
    return uncheckedIo(() -> createFile(url.openStream()));
  }

  @Override
  public ManagedDirectory fromInputStream(InputStream inputStream) {
    return uncheckedIo(() -> createFile(inputStream));
  }

  @Override
  public ManagedDirectory thatIsEmpty() {
    return fromInputStream(InputStream.nullInputStream());
  }

  @Override
  public ManagedDirectory withContents(byte[] contents) {
    return uncheckedIo(() -> createFile(new ByteArrayInputStream(contents)));
  }

  @Override
  public ManagedDirectory withContents(Charset charset, List<String> lines) {
    return withContents(String.join("\n", lines).getBytes(charset));
  }

  @Override
  public ManagedDirectory withContents(List<String> lines) {
    return withContents(DEFAULT_CHARSET, lines);
  }

  private ManagedDirectory createFile(InputStream input) throws IOException {
    Files.createDirectories(targetPath.getParent());

    var opts = new OpenOption[]{
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
    };

    try (
        var output = Files.newOutputStream(targetPath, opts);
        var bufferedInput = maybeBuffer(input, targetPath.toUri().getScheme())
    ) {
      bufferedInput.transferTo(output);
      return parent;
    }
  }

  private static InputStream maybeBuffer(InputStream input, @Nullable String scheme) {
    if (input instanceof BufferedInputStream || input instanceof ByteArrayInputStream) {
      return input;
    }

    scheme = scheme == null
        ? "unknown"
        : scheme.toLowerCase(Locale.ENGLISH);

    return switch (scheme) {
      case "classpath", "memory", "jrt", "ram" -> input;
      default -> {
        log.trace("Decided to wrap input {} in a buffer - scheme was {}", input, scheme);
        yield new BufferedInputStream(input);
      }
    };
  }

  private static ClassLoader currentCallerClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}
