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

package com.github.ascopes.jct.diagnostics;

import java.util.Locale;
import java.util.Objects;
import javax.tools.Diagnostic;

/**
 * Partial {@link Diagnostic} implementation that delegates to a provided diagnostic implementation
 * internally.
 *
 * @param <S> the source file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class ForwardingDiagnostic<S> implements Diagnostic<S> {

  protected final Diagnostic<? extends S> original;

  /**
   * Initialize this forwarding diagnostic.
   *
   * @param original the original diagnostic to delegate to.
   */
  protected ForwardingDiagnostic(Diagnostic<? extends S> original) {
    this.original = Objects.requireNonNull(original);
  }

  @Override
  public Kind getKind() {
    return original.getKind();
  }

  @Override
  public S getSource() {
    return original.getSource();
  }

  @Override
  public long getPosition() {
    return original.getPosition();
  }

  @Override
  public long getStartPosition() {
    return original.getStartPosition();
  }

  @Override
  public long getEndPosition() {
    return original.getEndPosition();
  }

  @Override
  public long getLineNumber() {
    return original.getLineNumber();
  }

  @Override
  public long getColumnNumber() {
    return original.getColumnNumber();
  }

  @Override
  public String getCode() {
    return original.getCode();
  }

  @Override
  public String getMessage(Locale locale) {
    return original.getMessage(locale);
  }

  @Override
  public String toString() {
    return original.toString();
  }
}
