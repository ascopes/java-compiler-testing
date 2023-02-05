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
package io.github.ascopes.jct.tests.helpers;

import static io.github.ascopes.jct.tests.helpers.GenericMock.mockRaw;
import static java.util.stream.Collectors.joining;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Feature;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.utils.LoomPolyfill;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.mockito.quality.Strictness;

/**
 * Commonly used mock fixtures.
 *
 * @author Ashley Scopes
 */
public final class Fixtures {

  private static final Random RANDOM = new Random();

  private Fixtures() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Get some boolean value.
   *
   * @return some boolean value.
   */
  public static boolean someBoolean() {
    return RANDOM.nextBoolean();
  }

  /**
   * Get some int value between 0 and the max value.
   *
   * @param max the exclusive max value.
   * @return some int value.
   */
  public static int someInt(int max) {
    return RANDOM.nextInt(max);
  }

  /**
   * Get some int value.
   *
   * @param min the inclusive minimum value.
   * @param max the exclusive maximum value.
   * @return some int value.
   */
  public static int someInt(int min, int max) {
    // Use longs internally to deal with problems with negative values.
    return (int) someLong((long) max - min) + min;
  }

  /**
   * Get some long value between 0 and the max value.
   *
   * @param max the exclusive max value.
   * @return some long value.
   */
  public static long someLong(long max) {
    var nextLong = RANDOM.nextLong();
    // Remove sign
    nextLong &= Long.MAX_VALUE;
    return nextLong % max;
  }

  /**
   * Get some long value.
   *
   * @param min the inclusive minimum value.
   * @param max the exclusive maximum value.
   * @return some long value.
   */
  public static long someLong(long min, long max) {
    return someLong(max - min) + min;
  }

  /**
   * Get some random text.
   *
   * @return the random text.
   */
  public static String someText() {
    return UUID.randomUUID().toString();
  }

  /**
   * Get some LF-separated lines of text.
   *
   * @return some text.
   */
  public static String someLinesOfText() {
    return Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .limit(someInt(5, 15))
        .collect(joining("\n"));
  }

  /**
   * Get some random binary data.
   *
   * @return the binary data.
   */
  public static byte[] someBinaryData() {
    var boxed = Stream
        .generate(() -> RANDOM.nextInt(Byte.MAX_VALUE - Byte.MIN_VALUE) + Byte.MIN_VALUE)
        .map(Integer::byteValue)
        .limit(someInt(10, 255))
        .toArray(Byte[]::new);

    var unboxed = new byte[boxed.length];
    for (var i = 0; i < boxed.length; ++i) {
      unboxed[i] = boxed[i];
    }
    return unboxed;
  }

  /**
   * Get some random command line flags.
   *
   * @return some flags.
   */
  public static List<String> someFlags() {
    return Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .map("--"::concat)
        .limit(someInt(2, 4))
        .collect(Collectors.toList());
  }

  /**
   * Get a diagnostic mock.
   *
   * @return the mock.
   */
  public static Diagnostic<JavaFileObject> someDiagnostic() {
    var mock = mockRaw(Diagnostic.class)
        .<Diagnostic<JavaFileObject>>upcastedTo()
        .build(withSettings().strictness(Strictness.LENIENT));

    createStubMethodsFor(mock);
    return mock;
  }

  /**
   * Get a trace diagnostic mock.
   *
   * @return the mock.
   */
  public static TraceDiagnostic<JavaFileObject> someTraceDiagnostic() {
    var mock = mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build(withSettings().strictness(Strictness.LENIENT));

    createStubMethodsFor(mock);
    when(mock.getTimestamp()).thenReturn(Instant.now());
    when(mock.getThreadName()).thenReturn(Thread.currentThread().getName());
    when(mock.getThreadId()).thenReturn(LoomPolyfill.getThreadId(Thread.currentThread()));
    when(mock.getStackTrace()).thenReturn(someStackTraceList());
    return mock;
  }

  /**
   * Get a trace diagnostic mock.
   *
   * @param kind the kind of the diagnostic to create.
   * @return the mock.
   */
  public static TraceDiagnostic<JavaFileObject> someTraceDiagnostic(Diagnostic.Kind kind) {
    var mock = someTraceDiagnostic();
    when(mock.getKind()).thenReturn(kind);
    return mock;
  }

  /**
   * Get a real stack trace array.
   *
   * @return the mock.
   */
  public static StackTraceElement[] someRealStackTrace() {
    var trace = Arrays.asList(Thread.currentThread().getStackTrace());
    // No need to have 500 stack trace frames in test logs.
    var shortTrace = trace.subList(2, Math.min(10, trace.size()));
    return shortTrace.toArray(StackTraceElement[]::new);

  }

  /**
   * Get a stack trace element list mock.
   *
   * @return the mock.
   */
  public static List<StackTraceElement> someStackTraceList() {
    return mockRaw(List.class)
        .<List<StackTraceElement>>upcastedTo()
        .build();
  }

  /**
   * Get some charset.
   *
   * @return some charset.
   */
  public static Charset someCharset() {
    return oneOf(
        StandardCharsets.UTF_8,
        StandardCharsets.UTF_16BE,
        StandardCharsets.UTF_16LE,
        StandardCharsets.UTF_16,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.US_ASCII
    );
  }

  /**
   * Get some string release version.
   *
   * @return some string release version.
   */
  public static String someRelease() {
    return Integer.toString(RANDOM.nextInt(11) + 11);
  }

  /**
   * Get a valid random module name.
   *
   * @return the valid module name.
   */
  public static String someModuleName() {
    return Stream
        .generate(() -> Stream
            .generate(() -> (char) someInt('a', 'z'))
            .map(Objects::toString)
            .limit(someInt(1, 10))
            .collect(joining()))
        .limit(someInt(1, 5))
        .collect(joining("."));
  }

  /**
   * Get a valid random package name.
   *
   * @return the valid package name.
   */
  public static String somePackageName() {
    return Stream
        .generate(() -> Stream
            .generate(() -> (char) someInt('a', 'z'))
            .map(Objects::toString)
            .limit(someInt(1, 10))
            .collect(joining()))
        .limit(someInt(1, 5))
        .collect(joining("."));
  }

  /**
   * Get a valid random class name.
   *
   * @return the valid class name.
   */
  public static String someClassName() {
    var firstChar = (char) someInt('A', 'Z');
    var restOfClassName = Stream
        .generate(() -> (char) someInt('a', 'z'))
        .map(Objects::toString)
        .limit(someInt(1, 10))
        .collect(joining());

    return somePackageName() + "." + firstChar + restOfClassName;
  }

  /**
   * Get some valid binary name. It may or may not be for a module.
   *
   * @return the binary name.
   */
  public static String someBinaryName() {
    return someBoolean()
        ? someModuleName() + "/" + someClassName()
        : someClassName();
  }

  /**
   * Get some mock Java file object with a dummy name and some assigned {@link Kind}.
   *
   * @return some mock Java file object.
   */
  public static JavaFileObject someJavaFileObject() {
    var name = someText() + ".java";
    var obj = mock(JavaFileObject.class, withSettings()
        .name(name)
        .defaultAnswer(RETURNS_DEEP_STUBS)
        .strictness(Strictness.LENIENT));

    when(obj.getName()).thenReturn(name);
    when(obj.getKind()).thenReturn(JavaFileObject.Kind.SOURCE);
    return obj;
  }

  /**
   * Get some mock location.
   *
   * @return some mock location.
   */
  public static Location someLocation() {
    var name = "Location-" + someText();

    Location location = mock(withSettings()
        .strictness(Strictness.LENIENT)
        .name(name));

    when(location.getName()).thenReturn(name);
    when(location.isOutputLocation()).thenReturn(someBoolean());
    when(location.isModuleOrientedLocation()).thenReturn(someBoolean());
    return location;
  }

  /**
   * Get some mock path root object.
   *
   * @return some mock path root object.
   */
  public static PathRoot somePathRoot() {
    var name = "PathRoot-" + someText();
    var path = someRelativePath().resolve(name);
    PathRoot pathRoot = mock(withSettings()
        .strictness(Strictness.LENIENT)
        .name(path.toString()));
    when(pathRoot.getPath()).thenReturn(path);
    when(pathRoot.getUri()).thenReturn(path.toUri());

    try {
      when(pathRoot.getUrl()).thenReturn(path.toUri().toURL());
    } catch (Exception ex) {
      throw new IllegalStateException("unreachable", ex);
    }

    when(pathRoot.getParent()).thenReturn(null);
    return pathRoot;
  }

  /**
   * Get some dummy path.
   *
   * @return some dummy path.
   */
  public static Path somePath() {
    var root = FileSystems.getDefault().getRootDirectories().iterator().next();

    for (var i = 0; i < someInt(1, 4); ++i) {
      root = root.resolve(someText());
    }

    return root.resolve("some-dummy-path");
  }

  /**
   * Get some dummy absolute path.
   *
   * @return some dummy absolute path.
   */
  public static Path someAbsolutePath() {
    return somePath().resolve("some-absolute-path").toAbsolutePath();
  }

  /**
   * Get some relative path.
   *
   * @return some dummy relative path.
   */
  public static Path someRelativePath() {
    var absolutePath = someAbsolutePath();
    var relativePath = absolutePath;

    for (var i = 0; i < someInt(1, 4); ++i) {
      relativePath = absolutePath.resolve(someText());
    }

    return absolutePath.relativize(relativePath.resolve("some-relative-path"));
  }

  /**
   * Get some temporary file system.
   *
   * <p>This must be explicitly closed at the end of each test.
   *
   * @return the file system.
   */
  public static TempFileSystem someTemporaryFileSystem() {
    return new TempFileSystem();
  }

  /**
   * Return one of the given elements.
   *
   * @param items the elements to pick from.
   * @param <T>   the type.
   * @return the element.
   */
  @SafeVarargs
  public static <T> T oneOf(T... items) {
    return items[RANDOM.nextInt(items.length)];
  }

  /**
   * Return one of the members of a given enum.
   *
   * @param cls the enum class.
   * @param <E> the enum type.
   * @return one of the enum members.
   */
  public static <E extends Enum<E>> E oneOf(Class<E> cls) {
    return oneOf(cls.getEnumConstants());
  }

  /**
   * Return one of the given elements.
   *
   * @param items the elements to pick from.
   * @param <T>   the type.
   * @return the element.
   */
  public static <T> T oneOf(Collection<T> items) {
    var index = RANDOM.nextInt(items.size());
    if (items instanceof List) {
      return ((List<T>) items).get(index);
    } else {
      var iter = items.iterator();
      for (var i = 0; i < index - 1; ++i) {
        iter.next();
      }
      return iter.next();
    }
  }

  private static void createStubMethodsFor(Diagnostic<JavaFileObject> diagnostic) {
    var col = someLong(1, 100);
    when(diagnostic.getColumnNumber()).thenReturn(col);
    var line = someLong(1, 2_000);
    when(diagnostic.getLineNumber()).thenReturn(line);
    var startPos = 1 + ((line * col) / 2) + someLong(1, 10_000);
    when(diagnostic.getStartPosition()).thenReturn(startPos);
    when(diagnostic.getEndPosition()).thenReturn(someLong(startPos, startPos + 10_000));
    when(diagnostic.getPosition()).thenReturn(startPos);
    when(diagnostic.getKind()).thenReturn(oneOf(Diagnostic.Kind.class));
    when(diagnostic.getSource()).thenReturn(null);
    when(diagnostic.getMessage(any())).thenReturn(someText());
  }

  /**
   * A temporary file system.
   */
  public static final class TempFileSystem implements AutoCloseable {

    private final FileSystem fs;
    private final Path root;

    private TempFileSystem() {
      // Default to UNIX to keep behaviour consistent.
      var name = someText();
      var config = Configuration
          .builder(PathType.unix())
          .setSupportedFeatures(Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.FILE_CHANNEL)
          .setAttributeViews("basic", "posix")
          .setRoots("/")
          .setWorkingDirectory("/")
          .build();

      fs = Jimfs.newFileSystem(name, config);
      root = fs.getRootDirectories().iterator().next();

      try {
        Files.createDirectories(root);
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }

    /**
     * Get the file system object.
     *
     * @return the file system.
     */
    public FileSystem getFileSystem() {
      return fs;
    }

    /**
     * Get the root path object.
     *
     * @return the root path.
     */
    @SuppressWarnings("unused")
    public Path getRootPath() {
      return root;
    }

    /**
     * Get the URI scheme for the file system.
     *
     * @return the URI scheme.
     */
    public String getScheme() {
      return root.toUri().getScheme();
    }

    @Override
    public void close() {
      try {
        fs.close();
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
}
