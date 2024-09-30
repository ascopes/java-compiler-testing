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
package io.github.ascopes.jct.workspaces;

import io.github.ascopes.jct.workspaces.impl.AbstractManagedDirectory;
import io.github.ascopes.jct.workspaces.impl.RamDirectoryImpl;
import io.github.ascopes.jct.workspaces.impl.TempDirectoryImpl;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;
import javax.annotation.processing.Filer;

/**
 * Strategy to use for creating new test directories.
 *
 * <p>This is used to define whether to use a totally isolated in-memory file system,
 * or whether to use temporary directories on the default file system.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum PathStrategy {
  /**
   * Use RAM-based directories for any created directories.
   *
   * <p>This is faster as everything remains in-memory. It also prevents the risk of resources
   * not being tidied up correctly if the JVM is suddenly shut down. Test directories are also kept
   * isolated from other tests that may be running in parallel, and isolated from the host operating
   * system.
   *
   * <p>If any annotation processors rely on being run in the {@link File default file system}
   * rather than using {@link Path the NIO path API} or {@link Filer filers} directly, then they
   * will not be compatible with reading files from this implementation of test directory. In this
   * situation, users should opt to use {@link #TEMP_DIRECTORIES} instead.
   *
   * <p>Some non-Javac compiler implementations (such as ECJ) may also have some difficulties
   * dealing with these paths.
   */
  RAM_DIRECTORIES(RamDirectoryImpl::newRamDirectory),

  /**
   * Use OS-level temporary directories for any created directories.
   *
   * <p>This will write files to the OS temporary directory. These files will be deleted once the
   * owning workspace is closed, but may be missed if the JVM is forcefully terminated or crashes.
   *
   * <p>There are fewer guarantees of speed and isolation compared to using
   * {@link #RAM_DIRECTORIES}. However, you do gain the ability to set a breakpoint and inspect the
   * contents of the directory in a file explorer.
   *
   * <p>Since the temporary directories are usually created on the
   * {@link File default file system}, they are compatible with any annotation processors or
   * compiler implementations that expect to be run on the default file system only.
   *
   * <p>Some restrictions regarding file naming may be present depending on the platform that
   * is in use, such as file name lengths on Windows. See your system documentation for more
   * details.
   */
  TEMP_DIRECTORIES(TempDirectoryImpl::newTempDirectory);

  private final Function<String, AbstractManagedDirectory> constructor;

  PathStrategy(Function<String, AbstractManagedDirectory> constructor) {
    this.constructor = constructor;
  }

  /**
   * Create a new instance of the test directory type with the given name.
   *
   * <p>Note that calling this directly will return an object that you will have to manually
   * manage the lifetime for. Failing to do so will result in resources being leaked.
   *
   * <p><strong>Users should not call this method by default unless they know what they are
   * doing.</strong> As a result of this constraint, this method is not part of the public API and
   * may be subject to change without notice.
   *
   * @param name the name to use.
   * @return the new test directory.
   */
  public ManagedDirectory newInstance(String name) {
    return constructor.apply(name);
  }

  /**
   * Determine the default strategy to fall back onto.
   *
   * <p>This will be {@link #RAM_DIRECTORIES} by default, but this may be subject to change between
   * minor versions without notice, so do not rely on this if you are testing code that may be
   * sensitive to the type of file system being used.
   *
   * @return the path strategy to use by default.
   */
  public static PathStrategy defaultStrategy() {
    return RAM_DIRECTORIES;
  }
}
