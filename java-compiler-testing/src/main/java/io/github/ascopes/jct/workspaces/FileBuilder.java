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
package io.github.ascopes.jct.workspaces;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Chainable builder for creating individual files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@SuppressWarnings("unused")
public interface FileBuilder {

  /**
   * Take the directory, represented by the given {@link ManagedDirectory}, and convert it into a
   * JAR file, that will be written to the file being created.
   *
   * @param directory the managed directory to use.
   * @return the root managed directory for further configuration.
   * @since 0.3.0
   */
  @API(since = "0.3.0", status = Status.STABLE)
  ManagedDirectory asJarFrom(PathRoot directory);

  /**
   * Take the directory, represented by the given path, and convert it into a JAR file, that will be
   * written to the file being created.
   *
   * <pre><code>
   *   ManagedDirectory someDirectory = ...;
   *   someDirectory
   *       .createFile("first.jar")
   *       .asJarFrom(firstClassOutputs);
   *   var secondCompilation = secondCompiler.compile(secondWorkspace);
   *   ...
   * </code></pre>
   *
   * @param directory the directory to use.
   * @return the root managed directory for further configuration.
   * @since 0.3.0
   */
  @API(since = "0.3.0", status = Status.STABLE)
  ManagedDirectory asJarFrom(Path directory);

  /**
   * Copy a resource from the given class loader into the file system.
   *
   * <pre><code>
   *   var classLoader = getClass().getClassLoader();
   *
   *   directory
   *       .createFile("org", "example", "HelloWorld.class")
   *       .copiedFromClassPath(classLoader, "org/example/HelloWorld.class");
   * </code></pre>
   *
   * @param classLoader the class loader to use.
   * @param resource    the resource to copy.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromClassPath(ClassLoader classLoader, String resource);

  /**
   * Copy a resource from the class loader on the current thread into the file system.
   *
   * <pre><code>
   *   directory
   *       .createFile("org", "example", "HelloWorld.class")
   *       .copiedFromClassPath("org/example/HelloWorld.class");
   * </code></pre>
   *
   * @param resource the resource to copy.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromClassPath(String resource);

  /**
   * Copy the contents from the given file into the file system.
   *
   * <pre><code>
   *   var file = new File("src/test/resources/code/HelloWorld.java");
   *
   *   directory
   *       .createFile("org", "example", "HelloWorld.java")
   *       .copiedFromFile(file);
   * </code></pre>
   *
   * @param file the file to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromFile(File file);

  /**
   * Copy the contents from the given path into the file system.
   *
   * <pre><code>
   *   var file = Path.of("src", "test", "resources", "code", "HelloWorld.java");
   *
   *   directory
   *       .createFile("org", "example", "HelloWorld.java")
   *       .copiedFromFile(file);
   * </code></pre>
   *
   * @param file the file to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromFile(Path file);

  /**
   * Copy the contents from the given URL into the file system.
   *
   * <pre><code>
   *   var url = URI.create("file:///code/org/example/HelloWorld.java").toURI();
   *
   *   directory
   *       .createFile("org", "example", "HelloWorld.java")
   *       .copiedFromUrl(url);
   * </code></pre>
   *
   * @param url the URL to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copiedFromUrl(URL url);

  /**
   * Copy the contents from the given input stream into the file system.
   *
   * <p>The input stream will be closed when reading completes.
   *
   * <pre><code>
   *   try (var is = getClass().getResourceAsStream("code/examples/HelloWorld.java")) {
   *     directory
   *         .createFile("org", "example", "HelloWorld.java")
   *         .fromInputStream(is);
   *   }
   * </code></pre>
   *
   * @param inputStream the input stream to read.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory fromInputStream(InputStream inputStream);

  /**
   * Create the file but do not put anything in it.
   *
   * <p>The resultant file will be 0 bytes long.
   *
   * <pre><code>
   *   directory
   *       .createFile(".gitkeep")
   *       .thatIsEmpty();
   * </code></pre>
   *
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory thatIsEmpty();


  /**
   * Create the file with the given byte contents.
   *
   * <pre><code>
   *   var classFile = new byte[]{
   *      -54, -2, -70, -66, 0, 0, 0, 63, 0, 29, 10, 0, 2, 0, 3, 7, 0, 4, 12, 0, 5, 0, 6, 1, 0,
   *      16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 6,
   *      60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86, 9, 0, 8, 0, 9, 7, 0, 10, 12, 0, 11,
   *      0, 12, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 121, 115, 116, 101,
   *      109, 1, 0, 3, 111, 117, 116, 1, 0, 21, 76, 106, 97, 118, 97, 47, 105, 111, 47, 80, 114,
   *      105, 110, 116, 83, 116, 114, 101, 97, 109, 59, 8, 0, 14, 1, 0, 13, 72, 101, 108, 108,
   *      111, 44, 32, 87, 111, 114, 108, 100, 33, 10, 0, 16, 0, 17, 7, 0, 18, 12, 0, 19, 0, 20,
   *      1, 0, 19, 106, 97, 118, 97, 47, 105, 111, 47, 80, 114, 105, 110, 116, 83, 116, 114, 101,
   *      97, 109, 1, 0, 7, 112, 114, 105, 110, 116, 108, 110, 1, 0, 21, 40, 76, 106, 97, 118, 97,
   *      47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 41, 86, 7, 0, 22, 1, 0, 10,
   *      72, 101, 108, 108, 111, 87, 111, 114, 108, 100, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76,
   *      105, 110, 101, 78, 117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 4, 109, 97, 105,
   *      110, 1, 0, 22, 40, 91, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114,
   *      105, 110, 103, 59, 41, 86, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1,
   *      0, 15, 72, 101, 108, 108, 111, 87, 111, 114, 108, 100, 46, 106, 97, 118, 97, 0, 33, 0,
   *      21, 0, 2, 0, 0, 0, 0, 0, 2, 0, 1, 0, 5, 0, 6, 0, 1, 0, 23, 0, 0, 0, 29, 0, 1, 0, 1, 0,
   *      0, 0, 5, 42, -73, 0, 1, -79, 0, 0, 0, 1, 0, 24, 0, 0, 0, 6, 0, 1, 0, 0, 0, 1, 0, 9, 0,
   *      25, 0, 26, 0, 1, 0, 23, 0, 0, 0, 37, 0, 2, 0, 1, 0, 0, 0, 9, -78, 0, 7, 18, 13, -74, 0,
   *      15, -79, 0, 0, 0, 1, 0, 24, 0, 0, 0, 10, 0, 2, 0, 0, 0, 3, 0, 8, 0, 4, 0, 1, 0, 27, 0,
   *      0, 0, 2, 0, 28
   *   };
   *
   *   directory
   *       .createFile("HelloWorld.class")
   *       .withContents(classFile);
   * </code></pre>
   *
   * @param contents the bytes to write.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory withContents(byte[] contents);

  /**
   * Create the file with the given contents.
   *
   * <pre><code>
   *   directory
   *      .createFile("org", "example", "HelloWorld.java")
   *      .withContents(StandardCharsets.US_ASCII, """
   *        package org.example;
   *
   *        public class HelloWorld {
   *          public static void main(String[] args) {
   *            System.out.println("Hello, World!");
   *          }
   *        }
   *      """);
   * </code></pre>
   *
   * <p>If the Java language level of your tests does not support multi-line strings, you can
   * alternatively pass each line of text to write as a separate string. These will be written to
   * the file using line-feed {@code '\n'} separators. For example:
   *
   * <pre><code>
   *   directory
   *      .createFile("org", "example", "HelloWorld.java")
   *      .withContents(
   *        StandardCharsets.US_ASCII,
   *        "package org.example;",
   *        "",
   *        "public class HelloWorld {",
   *        "  public static void main(String[] args) {",
   *        "    System.out.println(\"Hello, World!\");",
   *        "  }",
   *        "}"
   *      );
   * </code></pre>
   *
   * @param charset the character encoding to use.
   * @param lines   the lines to write.
   * @return the root managed directory for further configuration.
   * @see #withContents(String...)
   * @see #withContents(byte[])
   */
  ManagedDirectory withContents(Charset charset, String... lines);

  /**
   * Create the file with the given contents as UTF-8.
   *
   * <p>If you are using multi-line strings, an example of usage would be:
   *
   * <pre><code>
   *   directory
   *      .createFile("org", "example", "HelloWorld.java")
   *      .withContents("""
   *        package org.example;
   *
   *        public class HelloWorld {
   *          public static void main(String[] args) {
   *            System.out.println("Hello, World!");
   *          }
   *        }
   *      """);
   * </code></pre>
   *
   * <p>If the Java language level of your tests does not support multi-line strings, you can
   * alternatively pass each line of text to write as a separate string. These will be written to
   * the file using line-feed {@code '\n'} separators. For example:
   *
   * <pre><code>
   *   directory
   *      .createFile("org", "example", "HelloWorld.java")
   *      .withContents(
   *        "package org.example;",
   *        "",
   *        "public class HelloWorld {",
   *        "  public static void main(String[] args) {",
   *        "    System.out.println(\"Hello, World!\");",
   *        "  }",
   *        "}"
   *      );
   * </code></pre>
   *
   * @param lines the lines to write using the default charset.
   * @return the root managed directory for further configuration.
   * @see #withContents(Charset, String...)
   * @see #withContents(byte[])
   */
  ManagedDirectory withContents(String... lines);
}
