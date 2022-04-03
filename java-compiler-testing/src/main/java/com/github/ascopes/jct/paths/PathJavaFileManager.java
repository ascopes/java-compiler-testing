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

package com.github.ascopes.jct.paths;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;


/**
 * Implementation of a {@link JavaFileManager} that works on a set of paths from any loaded {@link
 * java.nio.file.FileSystem}s.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@SuppressWarnings("RedundantThrows")  // We keep the API contract to prevent breaking changes.
public class PathJavaFileManager implements JavaFileManager {

  private final PathLocationRepository repository;
  private volatile boolean closed;

  /**
   * Initialize the manager.
   *
   * @param repository the path repository to use.
   */
  public PathJavaFileManager(PathLocationRepository repository) {
    this.repository = repository;
    closed = false;
  }

  /**
   * Close the file manager. This does nothing if already closed.
   *
   * @throws IOException if an IO error occurred.
   */
  @Override
  public void close() throws IOException {
    closed = true;
  }

  @Override
  public boolean contains(Location location, FileObject fileObject)
      throws IllegalStateException, IOException {
    assertOpen();

    return repository
        .get(location)
        .map(manager -> manager.contains(fileObject))
        .orElse(false);
  }

  /**
   * Flush the file manager.
   *
   * @throws IllegalStateException if the file manager is already closed.
   * @throws IOException           if an IO error occurs.
   */
  @Override
  public void flush() throws IOException {
    assertOpen();
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return repository
        .get(location)
        .map(PathLocationManager::getClassLoader)
        .orElse(null);
  }

  /**
   * Get a service loader for the given service type in the given location.
   *
   * @param location the location to get.
   * @param service  the service class to get.
   * @param <S>      the type of the service.
   * @return the service loader.
   * @throws IOException                   if an IO error occurred.
   * @throws NoSuchElementException        if the loader cannot be found.
   * @throws UnsupportedOperationException if the operation is unsupported for the given location.
   */
  @Override
  public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service)
      throws IOException {
    return repository
        .get(location)
        .map(manager -> manager.getServiceLoader(service))
        .orElseThrow(() -> formatException(
            NoSuchElementException::new,
            "Cannot find a service loader for %s in location %s (%s)",
            service.getName(),
            location.getName(),
            location.getClass().getName()
        ));
  }

  /**
   * Get a file for input.
   *
   * @param location     the location of the file.
   * @param packageName  the name of the package.
   * @param relativeName the name of the file.
   * @return the file object, or {@code null} if not found in the location.
   * @throws IllegalArgumentException if the location is not known to this manager, or if the
   *                                  location is module-oriented.
   * @throws IllegalStateException    if the file manager is already closed.
   * @throws IOException              if an IO exception occurred.
   */
  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {

    assertNotModuleOrientedLocation(location);
    assertKnownLocation(location);

    return repository
        .get(location)
        .flatMap(manager -> manager.getFileForInput(packageName, relativeName))
        .orElse(null);
  }

  /**
   * Get a file for output.
   *
   * @param location     the location to output to.
   * @param packageName  the name of the package.
   * @param relativeName the file name.
   * @param sibling      any sibling file if one exists, or {@code null} otherwise.
   * @return the file to use for output, or {@code null} if no paths have been registered for the
   *     location.
   * @throws IllegalArgumentException if the location is not known to this manager, or if the
   *                                  location is not an output location.
   * @throws IllegalStateException    if the file manager is already closed.
   * @throws IOException              if an IO exception occurred.
   */
  @Override
  public FileObject getFileForOutput(
      Location location,
      String packageName,
      String relativeName,
      FileObject sibling
  ) throws IOException {
    assertOutputLocation(location);
    assertKnownLocation(location);

    return repository
        .get(location)
        .flatMap(manager -> manager.getFileForOutput(packageName, relativeName))
        .orElse(null);
  }

  /**
   * Get a file for input.
   *
   * @param location  the location of the file.
   * @param className the name of the class.
   * @param kind      the kind of the file.
   * @return the file object, or {@code null} if not found in the location.
   * @throws IllegalArgumentException if the location is not known to this manager, or if the
   *                                  location is module-oriented.
   * @throws IllegalStateException    if the file manager is already closed.
   * @throws IOException              if an IO exception occurred.
   */
  @Override
  public JavaFileObject getJavaFileForInput(
      Location location,
      String className,
      Kind kind
  ) throws IOException {
    assertNotModuleOrientedLocation(location);
    assertKnownLocation(location);

    return repository
        .get(location)
        .flatMap(manager -> manager.getJavaFileForInput(className, kind))
        .orElse(null);
  }

  /**
   * Get a file for output.
   *
   * @param location  the location to output to.
   * @param className the name of the class.
   * @param kind      the kind of the file.
   * @param sibling   any sibling file if one exists, or {@code null} otherwise.
   * @return the file to use for output, or {@code null} if no paths have been registered for the
   *     location.
   * @throws IllegalArgumentException if the location is not known to this manager, or if the
   *                                  location is not an output location.
   * @throws IllegalStateException    if the file manager is already closed.
   * @throws IOException              if an IO exception occurred.
   */
  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location,
      String className,
      Kind kind,
      FileObject sibling
  ) throws IOException {
    assertOutputLocation(location);
    assertKnownLocation(location);

    return repository
        .get(location)
        .flatMap(manager -> manager.getJavaFileForOutput(className, kind))
        .orElse(null);
  }

  @Override
  public Location getLocationForModule(Location location, String moduleName) throws IOException {
    assertOpen();
    assertModuleOrientedOrOutputLocation(location);
    assertKnownLocation(location);

    return new ModuleLocation(location, moduleName);
  }

  @Override
  public Location getLocationForModule(Location location, JavaFileObject fileObject)
      throws IOException {
    assertOpen();
    assertModuleOrientedOrOutputLocation(location);

    return repository
        .get(location)
        .map(ParentPathLocationManager.class::cast)
        .orElseThrow(() -> unknownLocation(location))
        .getModuleLocationFor(fileObject)
        .orElseThrow(() -> formatException(
            IllegalArgumentException::new,
            "File %s is not known to any modules within location %s (%s)",
            fileObject.toUri(),
            location.getName(),
            location.getClass().getName()
        ));
  }

  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    assertOpen();
    return false;
  }

  @Override
  public boolean hasLocation(Location location) {
    return repository
        .get(location)
        .isPresent();
  }


  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    return repository
        .get(location)
        .flatMap(manager -> manager.inferBinaryName(file))
        .orElse(null);
  }

  @Override
  public String inferModuleName(Location location) throws IOException {
    assertKnownLocation(location);
    assertModuleLocation(location);

    return ((ModuleLocation) location).getModuleName();
  }

  @Override
  public boolean isSameFile(FileObject a, FileObject b) {
    assertPathJavaFileObject(a);
    assertPathJavaFileObject(b);

    return Objects.equals(a.toUri(), b.toUri());
  }

  @Override
  public int isSupportedOption(String option) {
    return -1;
  }

  @Override
  public Iterable<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException {
    var manager = repository.get(location);

    return manager.isPresent()
        ? manager.get().list(packageName, kinds, recurse)
        : Collections.emptyList();
  }

  @Override
  public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {

    assertOpen();
    assertModuleOrientedOrOutputLocation(location);

    return repository
        .get(location)
        .map(ParentPathLocationManager.class::cast)
        .map(ParentPathLocationManager::listLocationsForModules)
        .map(List::of)
        .orElseGet(Collections::emptyList);
  }

  @Override
  public String toString() {
    return "PathJavaFileManager{}";
  }

  private void assertKnownLocation(Location location) {
    if (!repository.contains(location)) {
      throw unknownLocation(location);
    }
  }

  private void assertModuleLocation(Location location) {
    if (!(location instanceof ModuleLocation)) {
      throw formatException(
          IllegalArgumentException::new,
          "Location %s (%s) is not a module location. This is disallowed here.",
          location.getName(),
          location.getClass().getName()
      );
    }
  }

  private void assertModuleOrientedOrOutputLocation(Location location) {
    if (!location.isModuleOrientedLocation() && !location.isOutputLocation()) {
      throw formatException(
          IllegalArgumentException::new,
          "Location %s (%s) is not a module-oriented or output location. This is disallowed here.",
          location.getName(),
          location.getClass().getName()
      );
    }
  }

  private void assertNotModuleOrientedLocation(Location location) {
    if (location.isModuleOrientedLocation()) {
      throw formatException(
          IllegalArgumentException::new,
          "Location %s (%s) is a module-oriented location. This is disallowed here.",
          location.getName(),
          location.getClass().getName()
      );
    }
  }

  private void assertOutputLocation(Location location) {
    if (!location.isOutputLocation()) {
      throw formatException(
          IllegalArgumentException::new,
          "Location %s (%s) is not an output location. This is disallowed here.",
          location.getName(),
          location.getClass().getName()
      );
    }
  }

  private void assertPathJavaFileObject(FileObject fileObject) {
    if (!(fileObject instanceof PathJavaFileObject)) {
      throw formatException(
          IllegalArgumentException::new,
          "File object %s (%s pointing at %s) was not created by this file manager",
          fileObject.getName(),
          fileObject.getClass().getName(),
          fileObject.toUri()
      );
    }
  }

  private void assertOpen() {
    if (closed) {
      throw formatException(
          IllegalStateException::new,
          "File manager is closed"
      );
    }
  }

  private IllegalArgumentException unknownLocation(Location location) {
    return formatException(
        IllegalArgumentException::new,
        "Location %s (%s) is not known to this file manager",
        location.getName(),
        location.getClass().getName()
    );
  }

  private static <T extends Throwable> T formatException(
      Function<String, T> initializer,
      String template,
      Object... args
  ) {
    return args.length > 0
        ? initializer.apply(String.format(template, args))
        : initializer.apply(template);
  }
}
