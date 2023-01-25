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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import io.github.ascopes.jct.workspaces.FileBuilder;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.ThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chainable builder for creating individual files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@ThreadSafe
public final class FileBuilderImpl implements FileBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileBuilderImpl.class);
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private final ManagedDirectory parent;
  private final Path targetPath;

  /**
   * Initialise a new file builder.
   *
   * @param parent the parent managed directory to chain calls back onto.
   * @param first  the first part of the file path.
   * @param rest   additional parts of the file path.
   */
  public FileBuilderImpl(ManagedDirectory parent, String first, String... rest) {
    this.parent = parent;

    var targetPath = parent.getPath().resolve(first);
    for (var next : rest) {
      targetPath = targetPath.resolve(next);
    }

    this.targetPath = targetPath;
  }

  @Override
  public ManagedDirectory withContents(String... lines) {
    return withContents(DEFAULT_CHARSET, lines);
  }

  @Override
  public ManagedDirectory withContents(Charset charset, String... lines) {
    return withContents(String.join("\n", lines).getBytes(charset));
  }

  @Override
  public ManagedDirectory withContents(byte[] contents) {
    return uncheckedIo(() -> createFile(new ByteArrayInputStream(contents)));
  }

  @Override
  public ManagedDirectory copiedFromClassPath(String resource) {
    return copiedFromClassPath(currentCallerClassLoader(), resource);
  }

  @Override
  public ManagedDirectory copiedFromClassPath(ClassLoader classLoader, String resource) {
    return uncheckedIo(() -> {
      try (var input = classLoader.getResourceAsStream(resource)) {
        if (input == null) {
          throw new FileNotFoundException("classpath:" + resource);
        }

        return createFile(input);
      }
    });
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
  @SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD")
  public ManagedDirectory copiedFromUrl(URL url) {
    return uncheckedIo(() -> createFile(url.openStream()));
  }

  @Override
  public ManagedDirectory thatIsEmpty() {
    return fromInputStream(InputStream.nullInputStream());
  }

  @Override
  public ManagedDirectory fromInputStream(@WillClose InputStream inputStream) {
    return uncheckedIo(() -> createFile(inputStream));
  }

  @CheckReturnValue
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

  @CheckReturnValue
  private static InputStream maybeBuffer(InputStream input, @Nullable String scheme) {
    if (input instanceof BufferedInputStream || input instanceof ByteArrayInputStream) {
      return input;
    }

    scheme = scheme == null
        ? "unknown"
        : scheme.toLowerCase(Locale.ENGLISH);

    switch (scheme) {
      case "classpath":
      case "jimfs":
      case "jrt":
      case "ram":
        return input;
      default:
        LOGGER.trace("Decided to wrap input {} in a buffer - scheme was {}", input, scheme);
        return new BufferedInputStream(input);
    }
  }

  @CheckReturnValue
  private static ClassLoader currentCallerClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}
