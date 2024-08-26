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
package io.github.ascopes.jct.compilers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An enum representing the various types of debugger info that can be included in compilations.
 *
 * <p>This corresponds to the {@code -g} flag in the OpenJDK Javac implementation.
 *
 * <p>Debugging info flags are designed to be combined using the helper methods on this class.
 *
 * @author Ashley Scopes
 * @since 3.0.0
 */
@API(since = "3.0.0", status = Status.STABLE)
public enum DebuggingInfo {

  /**
   * Include line numbers.
   */
  LINES,

  /**
   * Include local variable names.
   */
  VARS,

  /**
   * Include source code.
   */
  SOURCE;

  /**
   * Return a set of none of the debugger info flags.
   *
   * @return a set containing no debugger flags.
   */
  public static Set<DebuggingInfo> none() {
    return EnumSet.noneOf(DebuggingInfo.class);
  }

  /**
   * Return a set of the given debugger info flags.
   *
   * @param flags flags.
   * @return the set of the debugger info flags.
   */
  public static Set<DebuggingInfo> just(DebuggingInfo... flags) {
    var set = none();
    Collections.addAll(set, flags);
    return set;
  }

  /**
   * Return a set of all the debugger info flags.
   *
   * @return a set containing all the debugger flags.
   */
  public static Set<DebuggingInfo> all() {
    return EnumSet.allOf(DebuggingInfo.class);
  }
}
