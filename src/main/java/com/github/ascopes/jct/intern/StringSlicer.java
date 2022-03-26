package com.github.ascopes.jct.intern;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * Utility to efficiently split strings using a delimiter without using regular expressions.
 *
 * <p>This implementation is stateless and threadsafe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class StringSlicer {

  private final String delimiter;

  /**
   * Initialize the splitter.
   *
   * @param delimiter the delimiter to split on.
   * @throws IllegalArgumentException if the delimiter is zero-length.
   */
  public StringSlicer(String delimiter) {
    this.delimiter = Objects.requireNonNull(delimiter);

    if (delimiter.isEmpty()) {
      throw new IllegalArgumentException("Cannot split on a zero-length string");
    }
  }

  /**
   * Split the given text.
   *
   * @param text the text to split.
   * @return the stream of split elements.
   */
  public Stream<String> splitToStream(String text) {
    return splitToArrayList(text).stream();
  }

  /**
   * Split the given text.
   *
   * @param text the text to split.
   * @return the array of split elements.
   */
  public String[] splitToArray(String text) {
    // No need to trim the size, we won't be keeping it in memory after this anyway.
    return splitToArrayList(text).toArray(String[]::new);
  }

  /**
   * {@inheritDoc}
   *
   * @return a string representation of this object.
   */
  @Override
  public String toString() {
    return "StringSlicer{"
        + "delimiter=" + StringUtils.quoted(delimiter)
        + "}";
  }

  private ArrayList<String> splitToArrayList(String text) {
    var list = new ArrayList<String>();
    var buffer = new StringBuilder();

    var index = 0;
    while (index < text.length()) {
      if (startsWith(text, index)) {
        list.add(buffer.toString());
        buffer.setLength(0);
        index += delimiter.length();
      } else {
        buffer.append(text.charAt(index));
        ++index;
      }
    }

    // We will always have one element left that has not been added.
    list.add(buffer.toString());

    return list;
  }

  private boolean startsWith(String text, int offset) {
    if (offset + delimiter.length() > text.length()) {
      return false;
    }

    for (var delimiterIndex = 0; delimiterIndex < delimiter.length(); ++delimiterIndex) {
      var textIndex = delimiterIndex + offset;
      if (text.charAt(textIndex) != delimiter.charAt(delimiterIndex)) {
        return false;
      }
    }

    return true;
  }
}
