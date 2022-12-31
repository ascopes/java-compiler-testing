/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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

/**
 * A dataclass for a user.
 *
 * @author Ashley Scopes
 */
public final class User {

  public final long id;
  public final String name;
  public final int age;

  /**
   * Initialise the user.
   *
   * @param id   the user ID.
   * @param name the user name.
   * @param age  the user age.
   */
  public User(long id, String name, int age) {
    this.id = id;
    this.name = name;
    this.age = age;
  }
}
