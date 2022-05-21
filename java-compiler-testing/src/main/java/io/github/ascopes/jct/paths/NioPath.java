package io.github.ascopes.jct.paths;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.StringUtils;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A wrapper around a {@link Path Java NIO Path} that makes it compatible with {@link PathLike}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class NioPath implements PathLike {

  private final Path path;
  private final URI uri;

  /**
   * Initialize this path.
   *
   * @param path the NIO path to wrap.
   */
  public NioPath(Path path) {
    this.path = requireNonNull(path, "path");
    uri = path.toUri();
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof NioPath)) {
      return false;
    }

    var that = (NioPath) other;

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
