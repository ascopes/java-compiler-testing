package com.github.ascopes.jct.utils;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;


/**
 * Adapter that converts an {@link Iterator} into an {@link Enumeration} to provide compatibility
 * with ancient parts of the JDK.
 *
 * @param <T> the type of each element the iterator returns.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class EnumerationAdapter<T> implements Enumeration<T> {

  private final Iterator<T> iterator;

  /**
   * Initialize the adapter.
   *
   * @param iterator the iterator to use.
   */
  public EnumerationAdapter(Iterator<T> iterator) {
    this.iterator = Objects.requireNonNull(iterator);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if more elements can be yielded, or {@code false} if the enumerator is
   * exhausted.
   */
  @Override
  public boolean hasMoreElements() {
    return iterator.hasNext();
  }

  /**
   * {@inheritDoc}
   *
   * @return the next element.
   * @throws java.util.NoSuchElementException if the enumerator has been exhausted.
   */
  @Override
  public T nextElement() {
    return iterator.next();
  }
}
