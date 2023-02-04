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
package io.github.ascopes.jct.containers;

import javax.annotation.concurrent.ThreadSafe;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A base definition for an output-oriented container group.
 *
 * <p>These can behave as if they are module-oriented, or non-module-oriented.
 * It is down to the implementation to mediate access between modules and their files.
 *
 * <p>Operations on modules should first {@link #getModule(String) get} or
 * {@link #getOrCreateModule(String) create} the module, and then operate on that sub-container
 * group. Operations on non-module packages should operate on this container group directly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@ThreadSafe
public interface OutputContainerGroup extends PackageContainerGroup, ModuleContainerGroup {

  /**
   * Get the output-oriented location.
   *
   * @return the output-oriented location.
   */
  @Override
  Location getLocation();
}
