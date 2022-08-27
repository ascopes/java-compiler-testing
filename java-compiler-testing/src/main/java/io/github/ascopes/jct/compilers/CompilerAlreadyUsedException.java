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

package io.github.ascopes.jct.compilers;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Exception that is thrown when reusing a compiler that was already used.
 *
 * @author Ashley Scopes
 * @see CompilerException
 * @since 0.0.1
 */
@API(status = Status.EXPERIMENTAL, since = "0.0.1")
public class CompilerAlreadyUsedException extends CompilerException {

  public CompilerAlreadyUsedException() {
    super("This compiler has already been used, create a new one to run another compilation");
  }
}
