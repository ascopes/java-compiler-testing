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
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

/**
 * Command line app to calculate the nth number of the fibonacci sequence.
 */
public class Fibonacci {

  /**
   * Main method.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("USAGE: java Fibonacci <n>");
      System.out.println("    <n>    Fibonacci number to generate");
    }

    BigInteger n = new BigInteger(args[0], 10);
    BigInteger a = ZERO;
    BigInteger b = ONE;

    for (BigInteger i = ZERO; i.compareTo(n) < 0; i = i.add(ONE)) {
      BigInteger oldB = b;
      b = a.add(b);
      a = oldB;
    }

    System.out.println(a);
  }
}
