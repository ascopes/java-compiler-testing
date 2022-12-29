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
package io.github.ascopes.jct.utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Utility to efficiently split strings using a delimiter without using regular expressions.
 *
 * <p>This implementation is stateless and threadsafe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
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

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("delimiter", delimiter)
        .toString();
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
