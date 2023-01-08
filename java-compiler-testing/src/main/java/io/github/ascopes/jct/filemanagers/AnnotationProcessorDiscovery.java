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
package io.github.ascopes.jct.filemanagers;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Mode for annotation processor discovery when no explicit processors are provided.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public enum AnnotationProcessorDiscovery {
  /**
   * Discovery is enabled, and will also scan any dependencies in the classpath or module path.
   */
  INCLUDE_DEPENDENCIES,

  /**
   * Discovery is enabled using the provided processor paths.
   */
  ENABLED,

  /**
   * Discovery is disabled.
   */
  DISABLED,
}
