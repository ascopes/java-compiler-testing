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
package io.github.ascopes.jct.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Builder of {@link Object#toString()} representations for POJO objects.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(status = Status.INTERNAL, since = "0.0.1")
@NotThreadSafe
public final class ToStringBuilder {

  private static final String LSQUARE = "[";
  private static final String RSQUARE = "]";
  private static final String COMMA = ", ";
  private static final String NULL = "null";
  private static final String LBRACE = "{";
  private static final String RBRACE = "}";
  private static final String ASSIGN = "=";

  private static final List<Class<?>> TYPES_TO_TREAT_AS_STRINGS = List.of(
      CharSequence.class,
      File.class,
      Locale.class,
      Pattern.class,
      Path.class,
      TemporalAccessor.class,
      UUID.class,
      URI.class,
      URL.class
  );

  private final Object owner;
  private final Map<String, Object> attributes;

  /**
   * Initialise the builder.
   *
   * @param owner the object that is being converted to a string.
   */
  public ToStringBuilder(Object owner) {
    this.owner = Objects.requireNonNull(owner, "owner must not be null");
    attributes = new LinkedHashMap<>();
  }

  /**
   * Add an attribute to the {@code toString} representation.
   *
   * @param name  the name of the attribute.
   * @param value the value of the attribute.
   * @return this builder for further calls.
   */
  public ToStringBuilder attribute(String name, @Nullable Object value) {
    attributes.put(name, value);
    return this;
  }

  /**
   * Generate the {@code toString} representation held by this builder and return it.
   *
   * @return the {@code toString} representation.
   */
  @Override
  public String toString() {
    var builder = new StringBuilder()
        .append(owner.getClass().getSimpleName())
        .append(LBRACE);

    var iterator = attributes.entrySet().iterator();

    while (iterator.hasNext()) {
      var next = iterator.next();
      builder.append(next.getKey()).append(ASSIGN);
      appendToString(builder, next.getValue());

      if (iterator.hasNext()) {
        builder.append(COMMA);
      }
    }

    return builder
        .append(RBRACE)
        .toString();
  }

  private static void appendToString(StringBuilder builder, Object object) {
    if (object == null) {
      builder.append(NULL);
    } else if (TYPES_TO_TREAT_AS_STRINGS.stream().anyMatch(cls -> cls.isInstance(object))) {
      appendToStringCharSequence(builder, object.toString());
    } else if (object instanceof Map<?, ?>) {
      appendToStringMap(builder, (Map<?, ?>) object);
    } else if (object.getClass().isArray()) {
      appendToStringArray(builder, object);
    } else if (object instanceof Iterable<?>) {
      appendToStringIterable(builder, ((Iterable<?>) object));
    } else {
      builder.append(object);
    }
  }

  private static void appendToStringCharSequence(StringBuilder builder, CharSequence chars) {
    builder.append("\"");

    var len = chars.length();
    for (var i = 0; i < len; ++i) {
      var next = chars.charAt(i);
      switch (next) {
        case '\0':
          builder.append("\\0");
          break;
        case '\r':
          builder.append("\\r");
          break;
        case '\n':
          builder.append("\\n");
          break;
        case '\t':
          builder.append("\\t");
          break;
        case '\"':
          builder.append("\\\"");
          break;
        case '\\':
          builder.append("\\\\");
          break;
        default:
          if (0x20 <= next && next <= 0x7E || 0x80 <= next && next <= 0xFF) {
            builder.append(next);
          } else {
            var hex = Integer.toHexString(next);
            builder.append("\\u")
                .append("0".repeat(4 - hex.length()))
                .append(hex);
          }
          break;
      }
    }

    builder.append("\"");
  }

  private static void appendToStringArray(StringBuilder builder, Object array) {
    var len = Array.getLength(array);

    builder.append(LSQUARE);

    for (var i = 0; i < len; ++i) {
      if (i > 0) {
        builder.append(COMMA);
      }

      var next = Array.get(array, i);
      appendToString(builder, next);
    }

    builder.append(RSQUARE);
  }

  private static void appendToStringIterable(StringBuilder builder, Iterable<?> iterable) {
    builder.append(LSQUARE);

    var iterator = iterable.iterator();

    while (iterator.hasNext()) {
      appendToString(builder, iterator.next());

      if (iterator.hasNext()) {
        builder.append(COMMA);
      }
    }

    builder.append(RSQUARE);
  }

  private static void appendToStringMap(StringBuilder builder, Map<?, ?> map) {
    builder.append(LBRACE);

    var iterator = map.entrySet().iterator();

    while (iterator.hasNext()) {
      var next = iterator.next();
      appendToString(builder, next.getKey());
      builder.append(ASSIGN);
      appendToString(builder, next.getValue());

      if (iterator.hasNext()) {
        builder.append(COMMA);
      }
    }

    builder.append(RBRACE);
  }
}
