package org.testng.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class TestNGineTests {

  private final TestNGine engine = new TestNGine();

  @TestFactory
  DynamicTest[] mavenCoordinates() {
    return new DynamicTest[] {
      dynamicTest("groupId", () -> assertEquals("org.testng", engine.getGroupId().get())),
      dynamicTest("artifactId", () -> assertEquals("testng-junit5", engine.getArtifactId().get())),
      dynamicTest("version", () -> assertEquals("DEVELOPMENT", engine.getVersion().get()))
    };
  }
}
