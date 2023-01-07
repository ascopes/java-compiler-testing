package io.github.ascopes.jct.tests.unit.filemanagers;

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JctFileManager Tests")
public class JctFileManagerImplTest {

  @Test
  @DisplayName("Tests whether we get instance of JctFileManager")
  void testGettingJctFileManagerImplInstance() {
    assertTrue(JctFileManagerImpl.forRelease("test") instanceof JctFileManagerImpl);
  }

  @Test
  @DisplayName("Tests whether we get instance of JctFileManager")
  void testIfNullPointerExceptionThrownIfReleaseNull() {
    assertThrows(NullPointerException.class,() -> {
      JctFileManagerImpl.forRelease(null);
    });
  }

}
