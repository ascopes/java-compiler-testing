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

  /**
   * {@inheritDoc}
   */
  @Override
  public Kind getKind() {
    return original.getKind();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public S getSource() {
    return original.getSource();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getPosition() {
    return original.getPosition();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getStartPosition() {
    return original.getStartPosition();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getEndPosition() {
    return original.getEndPosition();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLineNumber() {
    return original.getLineNumber();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getColumnNumber() {
    return original.getColumnNumber();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCode() {
    return original.getCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage(Locale locale) {
    return original.getMessage(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object other) {
    return other instanceof Diagnostic<?> && original.equals(other);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return original.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ForwardingDiagnostic{original=" + original + "}";
  }
}
