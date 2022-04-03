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

package com.github.ascopes.jct.test.helpers;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.withSettings;

import org.mockito.Mockito;

/**
 * Helper to create lightweight stubs and mocks.
 *
 * @author Ashley Scopes
 */
public final class Mocks {

  private Mocks() {
    throw new UnsupportedOperationException("static-only class");
  }

  public static <T> T mock(Class<T> type) {
    return Mockito.mock(type);
  }

  public static <T> T mock(TypeRef<T> typeRef) {
    return mock(typeRef.getType());
  }

  public static <T> T stub(Class<T> type) {
    return Mockito.mock(type, withSettings().stubOnly());
  }

  public static <T> T stub(TypeRef<T> typeRef) {
    return stub(typeRef.getType());
  }

  public static <T> T deepStub(Class<T> type) {
    return Mockito.mock(type, withSettings().stubOnly().defaultAnswer(RETURNS_DEEP_STUBS)
    );
  }

  public static <T> T deepStub(TypeRef<T> typeRef) {
    return deepStub(typeRef.getType());
  }
}
