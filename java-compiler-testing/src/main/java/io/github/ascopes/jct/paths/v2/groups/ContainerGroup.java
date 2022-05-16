package io.github.ascopes.jct.paths.v2.groups;

import java.io.Closeable;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base container group interface.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface ContainerGroup extends Closeable {

  /**
   * Get the location of this container group.
   *
   * @return the location.
   */
  Location getLocation();
}
