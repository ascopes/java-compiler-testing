/*
 * Copyright (C) 2022 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ascopes.jct.assertions;

import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractListAssert;

/**
 * Assertions to perform on a list of {@link JavaFileObject}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class JavaFileObjectListAssert
    extends
    AbstractListAssert<JavaFileObjectListAssert, List<? extends JavaFileObject>, JavaFileObject, JavaFileObjectAssert> {

  /**
   * Initialize this assertion object.
   *
   * @param actual the list of {@link JavaFileObject}s to assert on.
   */
  public JavaFileObjectListAssert(Iterable<? extends JavaFileObject> actual) {
    super(listify(actual), JavaFileObjectListAssert.class);
  }

  @Override
  protected JavaFileObjectAssert toAssert(JavaFileObject javaFileObject, String description) {
    return new JavaFileObjectAssert(javaFileObject).describedAs(description);
  }

  @Override
  protected JavaFileObjectListAssert newAbstractIterableAssert(
      Iterable<? extends JavaFileObject> iterable
  ) {
    var list = new ArrayList<JavaFileObject>();
    iterable.forEach(list::add);
    return new JavaFileObjectListAssert(list);
  }

  private static List<? extends JavaFileObject> listify(Iterable<? extends JavaFileObject> iter) {
    var list = new ArrayList<JavaFileObject>();
    iter.forEach(list::add);
    return list;
  }
}
