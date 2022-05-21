package io.github.ascopes.jct.paths;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.IterableUtils;
import io.github.ascopes.jct.utils.StringUtils;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A wrapper around an existing {@link PathLike} which contains a path to some sub-location in the
 * original path.
 *
 * <p>This mechanism enables keeping the original path-like reference alive, which
 * enables handling {@link RamPath} garbage collection correctly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class SubPath implements PathLike {

  private final PathLike parent;
  private final Path root;
  private final URI uri;

  /**
   * Initialize this path.
   *
   * @param path         the path-like path to wrap.
   * @param subPathParts the parts of the subpath to point to.
   */
  public SubPath(PathLike path, String... subPathParts) {
    parent = requireNonNull(path, "path");

    var root = path.getPath();
    for (var subPathPart : IterableUtils.requireNonNullValues(subPathParts, "subPathParts")) {
      root = root.resolve(subPathPart);
    }
    this.root = root;
    uri = root.toUri();
  }

  @Override
  public Path getPath() {
    return root;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SubPath)) {
      return false;
    }

    var that = (SubPath) other;

    return uri.equals(that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{path=" + StringUtils.quoted(uri) + "}";
  }
}
