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

package com.github.ascopes.jct.test.compilations;

import com.github.ascopes.jct.compilations.StandardCompilation;
import com.github.ascopes.jct.paths.PathLocationRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests for {@link StandardCompilation}.
 *
 * @author Ashley Scopes
 */
@DisplayName("StandardCompilation tests")
class StandardCompilationTest {

  static StandardCompilation.Builder someBuilder() {
    return StandardCompilation.builder()
        .outputLines(List.of())
        .compilationUnits(Set.of())
        .success(true)
        .warningsAsErrors(false)
        .diagnostics(List.of())
        .fileRepository(new PathLocationRepository());
  }
}
