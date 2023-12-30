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

import com.google.auto.value.AutoBuilder;
import java.time.Instant;

/**
 * Abstract representation of a builder for a user.
 */
@AutoBuilder(ofClass = AutoValue_User.class)
public abstract class UserBuilder {

  /**
   * Create a new builder for a user.
   *
   * @return the builder.
   */
  public static UserBuilder builder() {
    return new AutoBuilder_UserBuilder();
  }

  /**
   * Set the user ID.
   *
   * @param id the ID to set.
   * @return this builder.
   */
  public abstract UserBuilder setId(String id);

  /**
   * Set the user name.
   *
   * @param name the name to set.
   * @return this builder.
   */
  public abstract UserBuilder setName(String name);

  /**
   * Set the user's creation date.
   *
   * @param createdAt the creation date to set.
   * @return this builder.
   */
  public abstract UserBuilder setCreatedAt(Instant createdAt);

  /**
   * Build the user object.
   *
   * @return the new user object..
   */
  public abstract AutoValue_User build();
}
