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
package io.github.ascopes.jct.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.utils.IoExceptionUtils;
import java.io.ByteArrayOutputStream;
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

/**
 * Abstract assertions for {@link JavaFileObject Java file objects}.
 *
 * @param <I> the implementation class that is extending this class.
 * @param <A> the file object implementation type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractJavaFileObjectAssert<I extends AbstractJavaFileObjectAssert<I, A>, A extends JavaFileObject>
    extends AbstractAssert<I, A> {

  /**
   * Initialize this assertion.
   *
   * @param actual   the actual value to assert on.
   * @param selfType the type of the assertion implementation.
   */
  protected AbstractJavaFileObjectAssert(A actual, Class<?> selfType) {
    super(actual, selfType);
  }

  /**
   * Get an assertion object on the URI of the file.
   *
   * @return the URI assertion.
   */
  public AbstractUriAssert<?> uri() {
    return assertThat(actual.toUri());
  }

  /**
   * Get an assertion object on the name of the file.
   *
   * @return the string assertion.
   */
  public AbstractStringAssert<?> name() {
    return assertThat(actual.getName());
  }

  /**
   * Get an assertion object on the binary content of the file.
   *
   * @return the byte array assertion.
   */
  public AbstractByteArrayAssert<?> binaryContent() {
    return assertThat(rawContent());
  }

  /**
   * Get an assertion object on the content of the file, using {@link StandardCharsets#UTF_8 UTF-8}
   * encoding.
   *
   * @return the string assertion.
   */
  public AbstractStringAssert<?> content() {
    return content(StandardCharsets.UTF_8);
  }

  /**
   * Get an assertion object on the content of the file.
   *
   * @param charset the charset to decode the file with.
   * @return the string assertion.
   */
  public AbstractStringAssert<?> content(Charset charset) {
    return content(charset.newDecoder());
  }

  /**
   * Get an assertion object on the content of the file.
   *
   * @param charsetDecoder the charset decoder to use to decode the file to a string.
   * @return the string assertion.
   */
  public AbstractStringAssert<?> content(CharsetDecoder charsetDecoder) {
    var content = IoExceptionUtils.uncheckedIo(() -> charsetDecoder
        .decode(ByteBuffer.wrap(rawContent()))
        .toString());

    return assertThat(content);
  }

  /**
   * Get an assertion object on the last modified timestamp.
   *
   * @return the instant assertion.
   */
  public AbstractInstantAssert<?> lastModified() {
    var instant = Instant.ofEpochMilli(actual.getLastModified());
    return assertThat(instant);
  }

  /**
   * Perform an assertion on the file object kind.
   *
   * @return the assertions for the kind.
   */
  public JavaFileObjectKindAssert kind() {
    return new JavaFileObjectKindAssert(actual.getKind());
  }

  private byte[] rawContent() {
    return IoExceptionUtils.uncheckedIo(() -> {
      var baos = new ByteArrayOutputStream();
      try (var is = actual.openInputStream()) {
        is.transferTo(baos);
      }
      return baos.toByteArray();
    });
  }
}
