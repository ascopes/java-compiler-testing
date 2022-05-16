package io.github.ascopes.jct.paths.v2.containers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.paths.RamPath;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An extension of the definition of a {@link DirectoryContainer} that is designed to hold a
 * {@link RamPath}.
 *
 * <p>This enables keeping a hard reference to the {@link RamPath} itself to prevent the
 * reference-counted in-memory file system from being garbage collected too early.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class RamPathContainer extends DirectoryContainer {

  // It is important to keep this reference alive, otherwise the RamPath may decide to close itself
  // before we use it if it gets garbage collected.
  private final RamPath ramPath;

  /**
   * Initialize this container.
   *
   * @param ramPath the RAM path to initialize with.
   */
  public RamPathContainer(RamPath ramPath) {
    super(requireNonNull(ramPath, "ramPath").getPath());
    this.ramPath = ramPath;
  }

  /**
   * Get the RAM path that is being held.
   *
   * @return the RAM path.
   */
  @SuppressWarnings("unused")
  public RamPath getRamPath() {
    return ramPath;
  }
}
