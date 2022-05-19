package io.github.ascopes.jct.paths.v2;

import io.github.ascopes.jct.paths.RamPath;
import java.io.IOException;
import java.nio.file.Path;
import javax.tools.JavaFileManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface FileManager extends JavaFileManager {

  /**
   * Add a path to a given location.
   *
   * @param location the location to use.
   * @param path the path to add.
   * @throws IOException if an IO exception occurs.
   */
  void addPath(Location location, Path path) throws IOException;

  /**
   * Add a RAM path to a given location.
   *
   * @param location the location to use.
   * @param path the RAM path to add.
   * @throws IOException if an IO exception occurs.
   */
  void addPath(Location location, RamPath path) throws IOException;
}
