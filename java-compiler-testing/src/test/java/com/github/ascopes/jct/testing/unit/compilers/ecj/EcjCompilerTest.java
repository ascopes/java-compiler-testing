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

package com.github.ascopes.jct.testing.unit.compilers.ecj;

import static com.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.github.ascopes.jct.compilers.SimpleCompilation;
import com.github.ascopes.jct.compilers.ecj.EcjCompiler;
import com.github.ascopes.jct.compilers.ecj.EcjFlagBuilder;
import com.github.ascopes.jct.testing.helpers.ThreadPool;
import com.github.ascopes.jct.testing.helpers.ThreadPool.RunTestsInIsolation;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * {@link EcjCompiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("EcjCompiler tests")
class EcjCompilerTest {

  @DisplayName("compilers have the expected name")
  @Test
  void compilersHaveTheExpectedName() {
    assertThat(new EcjCompiler(stub(JavaCompiler.class)).getName())
        .isEqualTo("ecj");
  }

  @DisplayName("compilers have the expected JSR-199 compiler implementation")
  @Test
  void compilersHaveTheExpectedCompilerImplementation() {
    // Given
    var jsr199Compiler = stub(JavaCompiler.class);

    // Then
    assertThat(new EcjCompiler(jsr199Compiler).getJsr199Compiler())
        .isSameAs(jsr199Compiler);
  }

  @DisplayName("compilers have the expected flag builder")
  @Test
  void compilersHaveTheExpectedFlagBuilder() {
    assertThat(new EcjCompiler(stub(JavaCompiler.class)).getFlagBuilder())
        .isInstanceOf(EcjFlagBuilder.class);
  }

  // This just tests the global lock is acquired in the right place. It does not guarantee the
  // bug this lock protected against is actually fixed. That is dealt with by integration tests
  // instead.
  @DisplayName("calls to 'compile' acquire a global lock")
  @RunTestsInIsolation
  @Test
  @Timeout(30)
  void callsToCompileAcquireGlobalLock() {
    // We need to hold a global lock as ECJ compilers do not appear to run in a thread safe
    // way globally, with conflicts emerging when handling ZIP-like archives internally. This is
    // most likely a bug in how ECJ works.
    var counter = new AtomicInteger(0);

    // If the lock is not acquired, this will fail itself when it detects the counter being
    // greater than 1 internally. You can test this by commenting out lock.lock() and lock.unlock()
    // in EcjCompiler.class, or by extending JavacCompiler rather than EcjCompiler for
    // RunningOnceEcjCompiler. (Just make sure to change this back afterwards).
    try (var pool = new ThreadPool(500)) {
      Stream
          .generate(() -> new RunningOnceEcjCompiler(counter))
          .limit(500)
          .parallel()
          .<Callable<Void>>map(compiler -> () -> {
            compiler.compile();
            return null;
          })
          .collect(Collectors.collectingAndThen(Collectors.toList(), pool::awaitingAll))
          .join();
    }

    // Verify everything completed. I don't see where this would fail, but best to be safe
    // in case I missed anything.
    assertThat(counter).hasValue(0);
  }

  static class RunningOnceEcjCompiler extends EcjCompiler {

    private final AtomicInteger counter;

    RunningOnceEcjCompiler(AtomicInteger counter) {
      super(stub(JavaCompiler.class));
      this.counter = counter;
    }

    @Override
    protected SimpleCompilation performEntireCompilation() {
      try {
        counter.incrementAndGet();
        Thread.sleep(2);
        var counterValue = counter.getAndDecrement();
        if (counterValue != 1) {
          fail("Expected one compiler to run at once, but %s were running", counterValue);
        }
      } catch (InterruptedException ex) {
        fail("Test was interrupted", ex);
      }

      return stub(SimpleCompilation.class);
    }
  }
}
