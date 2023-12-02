/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package org.example;

import io.avaje.http.api.Client;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.Delete;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Put;

@Client
@Path("/users")
public interface UserApi {

  @Get("/{id}")
  @Produces("application/xml")
  User getUser(String id);

  @Consumes("application/xml")
  @Produces("application/xml")
  @Put("/{id}")
  User putUser(String id, PatchedUser patchedUser);

  @Delete("/{id}")
  void deleteUser(String id);
}
