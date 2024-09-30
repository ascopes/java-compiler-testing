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
package io.github.ascopes.jct.containers;

import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.jspecify.annotations.Nullable;

/**
 * Base interface representing a group of package-oriented paths.
 *
 * <p><strong>Warning</strong>: container group APIs are not designed to allow reuse between
 * compilation runs due the behaviour around providing access to class loaders. See the notes for
 * {@link #getClassLoader} for more details.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface PackageContainerGroup extends ContainerGroup {

  /**
   * Add a container to this group.
   *
   * <p>The provided container will be closed when this group is closed.
   *
   * <p>Note that this will destroy the {@link #getClassLoader class loader} if one is already
   * allocated from a previous request.
   *
   * @param container the container to add.
   */
  void addPackage(Container container);

  /**
   * Add a path to this group.
   *
   * <p>Note that this will destroy the {@link #getClassLoader class loader} if one is already
   * allocated from a previous request.
   *
   * <p>If the path points to some form of archive (such as a JAR), then this may open that archive
   * in a new resource internally. If this occurs, then the resource will always be freed by this
   * class by calling {@link #close}.
   *
   * <p>Any other closable resources passed to this function will not be closed by this
   * implementation. You must handle the lifecycle of those objects yourself.
   *
   * @param path the path to add.
   */
  void addPackage(PathRoot path);

  /**
   * Get a class loader for this group of containers.
   *
   * <p>If a class loader has not yet been created, then calling this method is expected to create
   * a class loader first.
   *
   * <p>This method is primarily provided to allow JCT to load components like annotation
   * processors from provided class paths dynamically during compilation, but is also suitable for
   * use by users to load classes compiled as part of test cases into memory to perform further
   * tests on the results via standard reflection APIs.
   *
   * <p>While not strictly required, it is recommended that any implementations of this class
   * provide a subclass of {@link java.net.URLClassLoader} to ensure similar behaviour to the
   * internals within OpenJDK's {@code javac} implementation.
   *
   * <p><strong>Warning</strong>: adding additional containers to this group after accessing this
   * class loader may result in the class loader being destroyed or re-created. This can result in
   * confusing behaviour where classes may get loaded multiple times. Generally this shouldn't be an
   * issue since the class loader is only accessed once the files have been added, but this does
   * mean that container group types should not be reused between compilation runs if possible. Due
   * to how the JCT API works, this means that you should avoid calling this method prior to
   * invoking the compiler itself, and likewise should try to avoid adding new packages to
   * implementations of container groups after the compiler has been invoked.
   *
   * <p><strong>Example of usage with Java:</strong>
   *
   * <pre><code>
   *   // Checked exception handling has been omitted from this example for
   *   // brevity. See the java.lang.reflect documentation for full details.
   *
   *   ClassLoader cl = containerGroup.getClassLoader();
   *   Class&lt;?&gt; cls = cl.loadClass("org.example.NumberAdder");
   *   Object adder = cls.getDeclaredConstructor().newInstance();
   *   Method addMethod = cls.getMethod("add", int.class, int.class);
   *   int result = (int) addMethod.invoke(adder, 9, 18);
   *
   *   assertThat(result).isEqualTo(27);
   * </code></pre>
   *
   * <p><strong>Example of usage with Groovy:</strong>
   *
   * <pre><code class="language-groovy">
   *   // Groovy is a great option if you are writing lots of tests like this,
   *   // since it will avoid much of the boilerplate around using the reflection
   *   // APIs directly due to the ability to dynamically infer the types of
   *   // objects at runtime. You also avoid having to deal with checked exceptions.
   *
   *   def cl = containerGroup.getClassLoader()
   *   def cls = cl.loadClass("org.example.NumberAdder")
   *   def adder = cls.getDeclaredConstructor().newInstance()
   *   def result = adder.add(9, 18)
   *
   *   assertThat(result).isEqualTo(27)
   * </code></pre>
   *
   * <p><strong>Example working with resources:</strong>
   *
   * <pre><code>
   *   // Checked exception handling has been omitted from this example for
   *   // brevity. See the java.lang.reflect documentation for full details.
   *   //
   *   // Consider the .getFile method on the PackageContainerGroup class instead
   *   // to achieve the same outcome with simpler syntax.
   *
   *   ClassLoader cl = containerGroup.getClassLoader();
   *   try (InputStream inputStream = cl.getResourceAsStream("META-INF/spring.factories")) {
   *     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
   *     inputStream.transferTo(outputStream);
   *     String content = new String(outputStream.toByteArray(), ...);
   *     ...
   *   }
   * </code></pre>
   *
   * @return a class loader for the contents of this container group.
   * @see java.lang.ClassLoader
   * @see java.lang.Class
   * @see java.lang.reflect.Method
   * @see java.lang.reflect.Field
   * @see java.lang.reflect.Constructor
   * @see java.net.URLClassLoader
   */
  ClassLoader getClassLoader();

  /**
   * Find the first occurrence of a given path to a file in packages or modules.
   *
   * <p>Paths should be relative to the root of this package group. Absolute paths
   * will be treated as erroneous inputs.
   *
   * <p>Modules are treated as subdirectories where supported.
   *
   * <p>This method accepts multiple strings to prevent users from having to
   * hard-code OS-specific file path separators that may create flaky tests. For example,
   * {@code .getFile("foo", "bar", "baz")} is equivalent to {@code .getFile("foo/bar/baz")} on most
   * systems.
   *
   * <p>Unlike {@link #getClassLoader}, this will allow access to the files
   * directly without needing to handle class loading exceptions.
   *
   * <pre><code>
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   containerGroup.getFile("foo", "bar", "baz.txt")...;
   *
   *   // Using platform-specific separators.
   *   containerGroup.getFile("foo/bar/baz.txt")...;
   * </code></pre>
   *
   * @param fragments parts of the path.
   * @return the first occurrence of the path in this group, or null if not found.
   * @throws IllegalArgumentException if the provided path is absolute.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @see java.nio.file.Path
   * @see java.nio.file.Files
   */
  @Nullable
  Path getFile(String... fragments);

  /**
   * Get a {@link FileObject} that can have content read from it.
   *
   * <p>This will return {@code null} if no file is found matching the criteria.
   *
   * @param packageName  the package name of the file to read.
   * @param relativeName the relative name of the file to read.
   * @return the file object, or null if the file is not found.
   */
  @Nullable
  PathFileObject getFileForInput(String packageName, String relativeName);

  /**
   * Get a {@link FileObject} that can have content written to it for the given file.
   *
   * <p>This will attempt to write to the first writeable path in this group. {@code null}
   * will be returned if no writeable paths exist in this group.
   *
   * @param packageName  the name of the package the file is in.
   * @param relativeName the relative name of the file within the package.
   * @return the {@link FileObject} to write to, or null if this group has no paths that can be
   *     written to.
   */
  @Nullable
  PathFileObject getFileForOutput(String packageName, String relativeName);

  /**
   * Get a {@link JavaFileObject} that can have content read from it for the given file.
   *
   * <p>This will return {@code null} if no file is found matching the criteria.
   *
   * @param className the binary name of the class to read.
   * @param kind      the kind of file to read.
   * @return the {@link JavaFileObject} to write to, or null if this group has no paths that can be
   *     written to.
   */
  @Nullable
  PathFileObject getJavaFileForInput(String className, Kind kind);

  /**
   * Get a {@link JavaFileObject} that can have content written to it for the given class.
   *
   * <p>This will attempt to write to the first writeable path in this group. {@code null}
   * will be returned if no writeable paths exist in this group.
   *
   * @param className the name of the class.
   * @param kind      the kind of the class file.
   * @return the {@link JavaFileObject} to write to, or null if this group has no paths that can be
   *     written to.
   */
  @Nullable
  PathFileObject getJavaFileForOutput(String className, Kind kind);

  /**
   * Get the package-oriented location that this group of paths is for.
   *
   * @return the package-oriented location.
   */
  @Override
  Location getLocation();

  /**
   * Get the package containers in this group.
   *
   * <p>Returned packages are presented in the order that they were registered. This is the
   * resolution order that the compiler will use.
   *
   * @return the containers.
   */
  List<Container> getPackages();

  /**
   * Try to infer the binary name of a given file object.
   *
   * @param fileObject the file object to infer the binary name for.
   * @return the binary name if known, or null otherwise.
   */
  @Nullable
  String inferBinaryName(PathFileObject fileObject);

  /**
   * Determine if this group has no paths registered.
   *
   * @return {@code true} if no paths are registered. {@code false} if paths are registered.
   */
  boolean isEmpty();

  /**
   * List all the file objects that match the given criteria in this group.
   *
   * <p>File objects are returned in an unordered collection, but lookup will be
   * performed in a deterministic order corresponding to the same order as the containers returned
   * by {@link #getPackages}.
   *
   * @param packageName the package name to look in.
   * @param kinds       the kinds of file to look for.
   * @param recurse     {@code true} to recurse subpackages, {@code false} to only consider the
   *                    given package.
   * @return the file objects that were found.
   * @throws IOException if the file lookup fails due to an IO error somewhere.
   */
  Set<JavaFileObject> listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException;

  /**
   * List all files recursively in this container group, returning a multimap of each container and
   * all files within that container.
   *
   * @return a multimap of containers mapping to collections of all files in that container.
   * @throws IOException if the file lookup fails due to an IO error somewhere.
   * @since 0.6.0
   */
  Map<Container, Collection<Path>> listAllFiles() throws IOException;
}
