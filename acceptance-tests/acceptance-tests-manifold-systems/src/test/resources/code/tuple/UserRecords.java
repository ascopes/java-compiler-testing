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

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

import java.util.List;
import manifold.ext.rt.api.auto;

/**
 * Operations to perform on users using Manifold tuple expressions.
 *
 * @author Ashley Scopes
 */
public class UserRecords {

  /**
   * Get the oldest users in the given list.
   *
   * @param users the users to check.
   * @return the list of user ages and user names, sorted with the oldest age first.
   */
  public static auto oldestUsers(List<User> users) {
    return users
        .stream()
        .sorted(comparingInt(user -> -user.age))
        .map(user -> (user.age, user.name))
        .collect(toList());
  }
}
