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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Feature;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.lang.module.ModuleReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.mockito.quality.Strictness;

/**
 * Commonly used mock fixtures.
 *
 * @author Ashley Scopes
 */
@SuppressWarnings("NullableProblems")
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
    return RANDOM.nextInt(max - min) + min;
  }

  /**
   * Get some long value between 0 and the max value.
   *
   * @param max the exclusive max value.
   * @return some long value.
   */
  public static long someLong(long max) {
    // nextLong(long) is not on older JDKs, so let's compute this manually.
    // This uses the same algorithm used in /java.base/jdk/internal/util/random/RandomSupport.java
    // as of JDK 19 in Amazon Corretto.
    var m = max - 1;
    var r = RANDOM.nextLong();
    if ((max & m) == 0L) {
      r &= m;
    } else {
      // This discards over-represented values for subsequent calls.
      var u = r >>> 1;
      while (u + m - (u % max) < 0L) {
        r = u % max;
        u = RANDOM.nextLong() >>> 1;
      }
    }

    return r;
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
        .collect(Collectors.joining("\n"));
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
    return mockRaw(Diagnostic.class)
        .<Diagnostic<JavaFileObject>>upcastedTo()
        .build();
  }

  /**
   * Get a tracee diagnostic mock.
   *
   * @return the mock.
   */
  public static TraceDiagnostic<JavaFileObject> someTraceDiagnostic() {
    return mockRaw(TraceDiagnostic.class)
        .<TraceDiagnostic<JavaFileObject>>upcastedTo()
        .build();
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
   * Get a list of some trace diagnostics.
   *
   * @return some trace diagnostics.
   */
  public static List<TraceDiagnostic<? extends JavaFileObject>> someTraceDiagnostics() {
    return Stream
        .generate(Fixtures::someTraceDiagnostic)
        .limit(someInt(3, 8))
        .collect(Collectors.toList());
  }

  /**
   * Get some compilation units.
   *
   * @return some compilation units.
   */
  public static List<JavaFileObject> someCompilationUnits() {
    return Stream
        .generate(() -> mock(JavaFileObject.class, withSettings().strictness(Strictness.LENIENT)))
        .peek(mock -> when(mock.getName()).thenReturn(someText()))
        .limit(someInt(3, 8))
        .collect(Collectors.toList());
  }

  /**
   * Get some unchecked exception with a stacktrace.
   *
   * @return some exception.
   */
  public static Throwable someUncheckedException() {
    var message = Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .limit(someInt(1, 4))
        .collect(Collectors.joining(" blah blah "));
    return new RuntimeException(message)
        .fillInStackTrace();
  }

  /**
   * Get some IO exception with a stacktrace.
   *
   * @return some exception.
   */
  public static Throwable someIoException() {
    var message = Stream
        .generate(UUID::randomUUID)
        .map(UUID::toString)
        .limit(someInt(1, 4))
        .collect(Collectors.joining(" blah blah "));
    return new IOException(message)
        .fillInStackTrace();
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
   * Get some locale.
   *
   * @return some locale.
   */
  public static Locale someLocale() {
    return oneOf(
        Locale.ROOT,
        Locale.US,
        Locale.UK,
        Locale.ENGLISH,
        Locale.GERMAN,
        Locale.JAPAN,
        Locale.GERMANY,
        Locale.JAPANESE,
        Locale.SIMPLIFIED_CHINESE,
        Locale.TRADITIONAL_CHINESE,
        Locale.CHINESE,
        Locale.CHINA
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
    when(obj.getKind()).thenReturn(Kind.SOURCE);
    return obj;
  }

  /**
   * Get some mock location.
   *
   * @return some mock location.
   */
  public static Location someLocation() {
    return mock(Location.class, "Location-" + someText());
  }

  /**
   * Get some mock path root object.
   *
   * @return some mock path root object.
   */
  public static PathRoot somePathRoot() {
    return mock(PathRoot.class, "PathRoot-" + someText());
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
   * Get some module reference.
   *
   * @return the module reference.
   */
  public static ModuleReference someModuleReference() {
    return mock(ModuleReference.class);
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
   * Get some annotation processor.
   *
   * @return some annotation processor.
   */
  public static Processor someAnnotationProcessor() {
    return mock(Processor.class, someText() + " processor");
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
          .setPathEqualityUsesCanonicalForm(true)
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
