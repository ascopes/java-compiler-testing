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
package io.github.ascopes.jct.tests.unit.containers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.impl.AbstractPackageContainerGroup;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractPackageContainerGroup} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("AbstractPackageContainerGroup tests")
class AbstractPackageContainerGroupTest {

  @DisplayName("listAllFiles returns a multimap of all files in all containers")
  @SuppressWarnings("resource")
  @Test
  void listAllFilesReturnsMultimapOfAllFilesInAllContainers() throws IOException {
    // Given
    var container1 = mock(Container.class, "container 1");
    var container1Path1 = mock(Path.class, "container 1 - path 1");
    var container1Path2 = mock(Path.class, "container 1 - path 2");
    var container1Path3 = mock(Path.class, "container 1 - path 3");
    var container1Path4 = mock(Path.class, "container 1 - path 4");
    when(container1.listAllFiles())
        .thenReturn(List.of(container1Path1, container1Path2, container1Path3, container1Path4));

    var container2 = mock(Container.class, "container 2");
    var container2Path1 = mock(Path.class, "container 2 - path 1");
    var container2Path2 = mock(Path.class, "container 2 - path 2");
    when(container2.listAllFiles())
        .thenReturn(List.of(container2Path1, container2Path2));

    var container3 = mock(Container.class, "container 3");
    var container3Path1 = mock(Path.class, "container 3 - path 1");
    var container3Path2 = mock(Path.class, "container 3 - path 2");
    var container3Path3 = mock(Path.class, "container 3 - path 3");
    when(container3.listAllFiles())
        .thenReturn(List.of(container3Path1, container3Path2, container3Path3));

    var packageContainerGroup = mock(AbstractPackageContainerGroup.class);
    when(packageContainerGroup.listAllFiles())
        .thenCallRealMethod();
    when(packageContainerGroup.getPackages())
        .thenReturn(List.of(container1, container2, container3));

    // When
    var allFiles = packageContainerGroup.listAllFiles();

    // Then
    assertThat(allFiles)
        .hasSize(3)
        .hasEntrySatisfying(container1, files -> assertThat(files)
            .hasSize(4)
            .containsExactly(container1Path1, container1Path2, container1Path3, container1Path4))
        .hasEntrySatisfying(container2, files -> assertThat(files)
            .hasSize(2)
            .containsExactly(container2Path1, container2Path2))
        .hasEntrySatisfying(container3, files -> assertThat(files)
            .hasSize(3)
            .containsExactly(container3Path1, container3Path2, container3Path3));
  }
}
