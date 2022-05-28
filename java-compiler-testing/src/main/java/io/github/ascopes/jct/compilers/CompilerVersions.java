package io.github.ascopes.jct.compilers;


import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;


/**
 * Helper class that documents the range of allowable versions for compilers provided within this
 * API.
 *
 * <p>This can be useful for writing parameterised test cases which test on multiple versions.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class CompilerVersions {

  private CompilerVersions() {
    throw new UnsupportedOperationException("static-only class");
  }
}
