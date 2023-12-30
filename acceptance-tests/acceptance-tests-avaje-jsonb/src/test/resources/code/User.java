/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import io.avaje.jsonb.Json;
import java.time.Instant;

/**
 * A user object.
 */
@Json
public class User {

  private final String id;
  private final String userName;
  private final Instant createdAt;

  /**
   * Init the user.
   *
   * @param id        the user ID.
   * @param userName  the username.
   * @param createdAt the date and time the user was created at.
   */
  public User(String id, String userName, Instant createdAt) {
    this.id = id;
    this.userName = userName;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getUserName() {
    return userName;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
