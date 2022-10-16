/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.acceptancetests.mapstruct;

/**
 * Basic POJO representing a car.
 */
public class Car {

  private String make;
  private int numberOfSeats;
  private CarType type;

  public String getMake() {
    return make;
  }

  public int getNumberOfSeats() {
    return numberOfSeats;
  }

  public CarType getType() {
    return type;
  }

  public void setMake(String make) {
    this.make = make;
  }

  public void setNumberOfSeats(int numberOfSeats) {
    this.numberOfSeats = numberOfSeats;
  }

  public void setType(CarType type) {
    this.type = type;
  }
}
