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

import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import java.nio.file.Path;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.PathAssert;

/**
 * Base methods to provide for a {@link PackageContainerGroup} assertion type.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PackageContainerGroupAssert
    extends AbstractAssert<PackageContainerGroupAssert, PackageContainerGroup> {

  /**
   * Initialize this assertion object.
   *
   * @param actual the container group to assert upon.
   */
  public PackageContainerGroupAssert(PackageContainerGroup actual) {
    super(actual, PackageContainerGroupAssert.class);
  }

  /**
   * Perform assertions on the class loader.
   *
   * @return the class loader assertions to perform.
   * @deprecated I have put up a pull request for AssertJ to support this functionality in AssertJ
   *     Core. Once this is merged, this return type will be changed to use the AssertJ
   *     implementation.
   */
  @Deprecated(forRemoval = true)
  @SuppressWarnings("removal")
  public ClassLoaderAssert classLoader() {
    return new ClassLoaderAssert(actual.getClassLoader());
  }

  /**
   * Perform an assertion on the location.
   *
   * @return the location assertions to perform.
   */
  public LocationAssert location() {
    return new LocationAssert(actual.getLocation());
  }

  /**
   * Perform assertions on the services that exist for the given service type that are discovered
   * from this container.
   *
   * @param serviceType the service class type to find services for.
   * @param <T>         the service type to use.
   * @return the assertions across the iterable of services discovered.
   */
  public <T> IterableAssert<T> serviceLoader(Class<T> serviceType) {
    return new IterableAssert<>(actual.getServiceLoader(serviceType));
  }

  /**
   * Perform assertions on the file at the given path, if it exists.
   *
   * <p>If the file does not exist, an empty optional is returned.
   *
   * @param path the path to get the file for.
   * @return the assertions to perform.
   */
  public MaybeAssert<PathAssert, Path> file(String path) {
    // TODO(ascopes): add in fuzzy comparison on results in error message
    return new MaybeAssert<>(actual.findFile(path).orElse(null), PathAssert::new);
  }

  /**
   * Perform an assertion on a file expected to be used for inputs.
   *
   * <p>If no file is found, an empty optional is returned.
   *
   * @param packageName  the package name that the file should reside within.
   * @param relativeName the relative name of the file in the package.
   * @return the assertions to perform.
   */
  public MaybeAssert<PathFileObjectAssert, PathFileObject> fileForInput(
      String packageName,
      String relativeName
  ) {
    return new MaybeAssert<>(
        actual.getFileForInput(packageName, relativeName).orElse(null),
        PathFileObjectAssert::new
    );
  }

  /**
   * Perform an assertion on a file expected to be used for outputs.
   *
   * <p>If no file is found, an empty optional is returned.
   *
   * @param packageName  the package name that the file should reside within.
   * @param relativeName the relative name of the file in the package.
   * @return the assertions to perform.
   */
  public MaybeAssert<PathFileObjectAssert, PathFileObject> fileForOutput(
      String packageName,
      String relativeName
  ) {
    return new MaybeAssert<>(
        actual.getFileForOutput(packageName, relativeName).orElse(null),
        PathFileObjectAssert::new
    );
  }

  /**
   * Perform an assertion on a Java file expected to be used for inputs.
   *
   * <p>If no file is found, an empty optional is returned.
   *
   * @param className the fully qualified binary name of the class to use.
   * @param kind      the kind of file.
   * @return the assertions to perform.
   */
  public MaybeAssert<PathFileObjectAssert, PathFileObject> javaFileForInput(
      String className,
      Kind kind
  ) {
    return new MaybeAssert<>(
        actual.getJavaFileForInput(className, kind).orElse(null),
        PathFileObjectAssert::new
    );
  }

  /**
   * Perform an assertion on a Java file expected to be used for outputs.
   *
   * <p>If no file is found, an empty optional is returned.
   *
   * @param className the fully qualified binary name of the class to use.
   * @param kind      the kind of file.
   * @return the assertions to perform.
   */
  public MaybeAssert<PathFileObjectAssert, PathFileObject> javaFileForOutput(
      String className,
      Kind kind
  ) {
    return new MaybeAssert<>(
        actual.getJavaFileForOutput(className, kind).orElse(null),
        PathFileObjectAssert::new
    );
  }

  /**
   * Assert that this container is empty.
   *
   * @return this assertion object for further call chaining.
   */
  public PackageContainerGroupAssert isEmpty() {
    if (!actual.isEmpty()) {
      throw failure(
          "Expected container group for location %s to be empty but it was not",
          actual.getLocation().getName()
      );
    }

    return this;
  }

  /**
   * Assert that this container is not empty.
   *
   * @return this assertion object for further call chaining.
   */
  public PackageContainerGroupAssert isNotEmpty() {
    if (actual.isEmpty()) {
      throw failure(
          "Expected container group for location %s to not be empty but it was",
          actual.getLocation().getName()
      );
    }

    return this;
  }
}
