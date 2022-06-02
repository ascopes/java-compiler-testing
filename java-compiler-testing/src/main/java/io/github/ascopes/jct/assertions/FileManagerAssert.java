/*
 * Copyright (C) 2022 Ashley Scopes
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

import io.github.ascopes.jct.jsr199.FileManager;
import java.util.Optional;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for a file manager.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class FileManagerAssert extends AbstractAssert<FileManagerAssert, FileManager> {

  /**
   * Initialize this file manager assertion object.
   *
   * @param fileManager the file manager to perform assertions upon.
   */
  public FileManagerAssert(FileManager fileManager) {
    super(fileManager, FileManagerAssert.class);
  }

  /**
   * Perform assertions on the classloader for a given location.
   *
   * @param location the location to assert upon.
   * @return the assertions for the classloader.
   */
  public OptionalAssert<ClassLoaderAssert, ClassLoader> classLoader(Location location) {
    return new OptionalAssert<>(
        Optional.ofNullable(actual.getClassLoader(location)),
        ClassLoaderAssert::new
    );
  }

  /**
   * Perform assertions on a given listing of {@link JavaFileObject Java file objects}.
   *
   * @param location    the location to fetch.
   * @param packageName the package to list in.
   * @param kinds       the kinds of file to list.
   * @param recurse     whether to recurse into subdirectories or not.
   * @return the assertions for the results.
   */
  public JavaFileObjectListAssert listing(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) {
    return uncheckedIo(() -> {
      var results = actual.list(location, packageName, kinds, recurse);
      return new JavaFileObjectListAssert(results);
    });
  }

}
