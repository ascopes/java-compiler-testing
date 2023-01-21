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
package io.github.ascopes.jct.tests.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.tests.helpers.Fixtures;
import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.utils.LoomPolyfill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/**
 * {@link LoomPolyfill} tests.
 *
 * @author Ashley Scopes
 */
class LoomPolyfillTest implements UtilityClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return LoomPolyfill.class;
  }

  @DisplayName(".getThreadId(Thread) on JRE <= 18 calls .getId()")
  @EnabledForJreRange(max = JRE.JAVA_18)
  @Test
  void getThreadIdOnJre18AndOlderUsesThreadGetId() throws Throwable {
    // Given
    var expectedThreadId = Fixtures.someLong(1, Short.MAX_VALUE);
    var thread = mock(Thread.class);
    var getIdMethod = Thread.class.getDeclaredMethod("getId");

    when((long) getIdMethod.invoke(thread)).thenReturn(expectedThreadId);

    // When
    var actualThreadId = LoomPolyfill.getThreadId(thread);

    // Then
    assertThat(actualThreadId).isEqualTo(expectedThreadId);
    getIdMethod.invoke(verify(thread));
  }

  @DisplayName(".getThreadId(Thread) on JRE >= 19 calls .threadId()")
  @EnabledForJreRange(min = JRE.JAVA_19)
  @Test
  void getThreadIdOnJre19AndNewerUsesThreadId() throws Throwable {
    // Given
    var expectedThreadId = Fixtures.someLong(1, Short.MAX_VALUE);
    var thread = mock(Thread.class);
    var threadIdMethod = Thread.class.getDeclaredMethod("threadId");

    when((long) threadIdMethod.invoke(thread)).thenReturn(expectedThreadId);

    // When
    var actualThreadId = LoomPolyfill.getThreadId(thread);

    // Then
    assertThat(actualThreadId).isEqualTo(expectedThreadId);
    threadIdMethod.invoke(verify(thread));
    verifyNoMoreInteractions(thread);
  }
}
