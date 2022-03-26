package com.github.ascopes.jct.paths;

import com.github.ascopes.jct.intern.StringUtils;
import java.util.Objects;
import javax.tools.JavaFileManager.Location;


/**
 * Handle that represents the location of a module.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class ModuleLocation implements Location {

  private final Location parent;
  private final String moduleName;
  private final String name;

  /**
   * Initialize the location.
   *
   * @param parent     the parent location.
   * @param moduleName the module name.
   * @throws IllegalArgumentException if the parent location is not an output location or a
   *                                  module-oriented location.
   */
  public ModuleLocation(Location parent, String moduleName) {
    Objects.requireNonNull(parent);
    Objects.requireNonNull(moduleName);

    if (!parent.isOutputLocation() && !parent.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "The parent of a module location must be either an output location or be module-oriented:"
              + parent.getName()
      );
    }

    this.parent = parent;
    this.moduleName = moduleName;
    name = parent.getName() + "[" + moduleName + "]";
  }

  /**
   * Get the parent location.
   *
   * @return the parent location.
   */
  public Location getParent() {
    return parent;
  }

  /**
   * Get the module name.
   *
   * @return the module name.
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * {@inheritDoc}
   *
   * @return the name of the location.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if an output location, {@code false} if not.
   */
  @Override
  public boolean isOutputLocation() {
    return parent.isOutputLocation();
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false} always.
   */
  @Override
  public boolean isModuleOrientedLocation() {
    // Module locations cannot be module-oriented, but their parents can be.
    return false;
  }

  /**
   * Determine if this object equals another object.
   *
   * @param other the other object to compare to.
   * @return {@code true} if the other object is a {@link ModuleLocation} with an equal {@link
   * #getParent()} and {@link #getModuleName()} to this object. Otherwise, {@code false}.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ModuleLocation)) {
      return false;
    }

    var that = (ModuleLocation) other;

    return parent.equals(that.parent)
        && moduleName.equals(that.moduleName);
  }

  /**
   * Determine the hash code for this object.
   *
   * @return the hash code of this object.
   */
  @Override
  public int hashCode() {
    return Objects.hash(parent, moduleName);
  }

  /**
   * {@inheritDoc}
   *
   * @return the string representation of this object.
   */
  @Override
  public String toString() {
    return "ModuleLocation{"
        + "name=" + StringUtils.quoted(name)
        + "}";
  }
}
