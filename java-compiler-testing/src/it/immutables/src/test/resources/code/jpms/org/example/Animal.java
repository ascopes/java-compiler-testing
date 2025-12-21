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
package org.example;

import java.lang.annotation.Inherited;
import org.immutables.value.Value;

/**
 * Interface that immutables will generate an implementation for.
 *
 * <p>We have to set the inherited annotation here to force immutables to not try and use
 * javax.annotation.Generated which is only available in JDK-8 and older. Not sure why it is doing
 * this, but it looks like a bug in Immutables rather than JCT itself, since it appears to be
 * resolving the class somewhere. Guessing there is an issue with the usage of the --release javac
 * flag somewhere?
 */
@Value.Immutable
@Value.Style(allowedClasspathAnnotations = Inherited.class)
public interface Animal {

  String name();

  int legCount();

  int age();
}
