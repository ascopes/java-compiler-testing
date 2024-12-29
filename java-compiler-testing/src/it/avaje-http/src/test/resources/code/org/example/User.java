/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
 * A user.
 */
public final class User {
  private final String id;
  private final String name;
  private final String nickName;

  /**
   * Initialise the user.
   *
   * @param id the user's ID.
   * @param name the user's name.
   * @param nickName the user's nickname.
   */
  public User(String id, String name, String nickName) {
    this.id = id;
    this.name = name;
    this.nickName = nickName;
  }

  /**
   * Get the user's ID.
   *
   * @return the user's ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Get the user's name.
   *
   * @return the user's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the user's nickname.
   *
   * @return the user's nickname.
   */
  public String getNickName() {
    return nickName;
  }
}
