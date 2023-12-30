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
package io.github.ascopes.jct.assertions;

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractByteArrayAssert;
import org.assertj.core.api.AbstractInstantAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AbstractUriAssert;
import org.jspecify.annotations.Nullable;

/**
 * Abstract assertions for {@link JavaFileObject Java file objects}.
 *
 * @param <I> the implementation class that is extending this class.
 * @param <A> the file object implementation type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public abstract class AbstractJavaFileObjectAssert<I extends AbstractJavaFileObjectAssert<I, A>, A extends JavaFileObject>
    extends AbstractAssert<I, A> {

  /**
   * Initialize this assertion.
   *
   * @param actual   the actual value to assert on.
   * @param selfType the type of the assertion implementation.
   */
  protected AbstractJavaFileObjectAssert(@Nullable A actual, Class<?> selfType) {
    super(actual, selfType);
  }

  /**
   * Get an assertion object on the URI of the file.
   *
   * @return the URI assertion.
   * @throws AssertionError if the actual value is null.
   */
  public AbstractUriAssert<?> uri() {
    isNotNull();
    return assertThat(actual.toUri());
  }

  /**
   * Get an assertion object on the name of the file.
   *
   * @return the string assertion.
   * @throws AssertionError if the actual value is null.
   */
  public AbstractStringAssert<?> name() {
    isNotNull();
    return assertThat(actual.getName());
  }

  /**
   * Get an assertion object on the binary content of the file.
   *
   * @return the byte array assertion.
   * @throws AssertionError if the actual value is null.
   */
  public AbstractByteArrayAssert<?> binaryContent() {
    isNotNull();
    return assertThat(rawContent());
  }

  /**
   * Get an assertion object on the content of the file, using {@link StandardCharsets#UTF_8 UTF-8}
   * encoding.
   *
   * @return the string assertion.
   * @throws AssertionError       if the actual value is null.
   * @throws UncheckedIOException if an IO error occurs reading the file content.
   */
  public AbstractStringAssert<?> content() {
    return content(StandardCharsets.UTF_8);
  }

  /**
   * Get an assertion object on the content of the file.
   *
   * @param charset the charset to decode the file with.
   * @return the string assertion.
   * @throws AssertionError       if the actual value is null.
   * @throws NullPointerException if the charset parameter is null.
   * @throws UncheckedIOException if an IO error occurs reading the file content.
   */
  public AbstractStringAssert<?> content(Charset charset) {
    requireNonNull(charset, "charset must not be null");
    return content(charset.newDecoder());
  }

  /**
   * Get an assertion object on the content of the file.
   *
   * @param charsetDecoder the charset decoder to use to decode the file to a string.
   * @return the string assertion.
   * @throws AssertionError       if the actual value is null.
   * @throws NullPointerException if the charset decoder parameter is null.
   * @throws UncheckedIOException if an IO error occurs reading the file content.
   */
  public AbstractStringAssert<?> content(CharsetDecoder charsetDecoder) {
    requireNonNull(charsetDecoder, "charsetDecoder must not be null");
    isNotNull();

    var content = uncheckedIo(() -> charsetDecoder
        .decode(ByteBuffer.wrap(rawContent()))
        .toString());

    return assertThat(content);
  }

  /**
   * Get an assertion object on the last modified timestamp.
   *
   * <p>This will be set to the UNIX epoch ({@code 1970-01-01T00:00:00.000Z}) if an
   * error occurs reading the file modification time, or if the information is not available.
   *
   * @return the instant assertion.
   * @throws AssertionError if the actual value is null.
   */
  public AbstractInstantAssert<?> lastModified() {
    isNotNull();

    var instant = Instant.ofEpochMilli(actual.getLastModified());
    return assertThat(instant);
  }

  /**
   * Perform an assertion on the file object kind.
   *
   * @return the assertions for the kind.
   * @throws AssertionError if the actual value is null.
   */
  public JavaFileObjectKindAssert kind() {
    isNotNull();

    return new JavaFileObjectKindAssert(actual.getKind());
  }

  private byte[] rawContent() {
    return uncheckedIo(() -> {
      var baos = new ByteArrayOutputStream();
      try (var is = actual.openInputStream()) {
        is.transferTo(baos);
        return baos.toByteArray();
      }
    });
  }
}
