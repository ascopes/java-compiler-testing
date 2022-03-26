package com.github.ascopes.jct.intern;

import java.util.Objects;

/**
 * Utilities for string manipulation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class StringUtils {

  private StringUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Wrap the string representation of the given argument in double-quotes.
   *
   * <p>Backslashes will be doubled (i.e. '\' will become '\\'), and double-quotes
   * within the string representation of the object will have a backslash prefixed (i.e. '"' will
   * become '\"').
   *
   * @param object the object to produce a quoted string representation of.
   * @return the string representation, surrounded by double quotes.
   */
  public static String quoted(Object object) {
    if (object == null) {
      return "null";
    }

    var builder = new StringBuilder();
    appendQuoted(builder, object);
    return builder.toString();
  }

  /**
   * Produce a string representation of the iterable, quoting each member.
   *
   * @param iterable the iterable to process.
   * @return the string representation of the iterable, with each member quoted (unless null).
   */
  public static String quotedIterable(Iterable<?> iterable) {
    if (iterable == null) {
      return "null";
    }

    var builder = new StringBuilder("[");
    var first = true;

    for (var item : iterable) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      appendQuoted(builder, item);
    }

    return builder.append("]").toString();
  }

  private static void appendQuoted(StringBuilder builder, Object object) {
    if (object == null) {
      builder.append("null");
      return;
    }

    var objectStr = Objects.toString(object);

    builder.append("\"");

    for (var i = 0; i < objectStr.length(); ++i) {
      var c = objectStr.charAt(i);
      switch (c) {
        case '\\':
          builder.append("\\\\");
          break;
        case '"':
          builder.append("\\\"");
          break;
        default:
          builder.append(c);
      }
    }

    builder.append("\"");
  }
}
