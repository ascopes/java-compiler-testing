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
package io.github.ascopes.jct.compilers;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An enum representing the various types of compilation mode that a compiler can run under.
 *
 * <p>This corresponds to the {@code -proc} flag in the OpenJDK Javac implementation.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M6)
 */
@API(since = "0.0.1", status = Status.STABLE)
public enum CompilationMode {

  /**
   * Run compilation and run the annotation processors, if configured.
   *
   * <p>This is usually the default mode.
   */
  COMPILATION_AND_ANNOTATION_PROCESSING,

  /**
   * Run compilation, but skip any annotation processing that may run.
   *
   * <p>This corresponds to providing {@code -proc:none} in the OpenJDK Javac implementation.
   */
  COMPILATION_ONLY,

  /**
   * Skip compilation, but run any annotation processing that may be enabled.
   *
   * <p>This corresponds to providing {@code -proc:only} in the OpenJDK Javac implementation.
   */
  ANNOTATION_PROCESSING_ONLY,
}
