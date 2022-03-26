package com.github.ascopes.jct.compilers;


/**
 * Function representing a configuration operation that can be applied to a compiler.
 *
 * <p>This can allow encapsulating common configuration logic across tests into a single place.
 *
 * @param <C> the compiler type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@FunctionalInterface
public interface CompilerConfigurer<C extends Compiler<C, ?>> {

  /**
   * Apply configuration logic to the given compiler.
   *
   * @param compiler the compiler.
   */
  void configure(C compiler);
}
