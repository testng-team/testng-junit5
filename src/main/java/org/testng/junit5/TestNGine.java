package org.testng.junit5;

import java.util.Optional;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

public class TestNGine implements TestEngine {

  static String ENGINE_ID = "testng-junit5";

  static String ENGINE_DISPLAY_NAME = "TestNG TestEngine SPIKE";

  public String getId() {
    return ENGINE_ID;
  }

  public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
    var engine = new EngineDescriptor(uniqueId, ENGINE_DISPLAY_NAME);
    // inspect "engineDiscoveryRequest" selectors and filters passed by the user
    // find TestNG-based test containers (classes) and tests (methods)
    //   wrap each in a new TestDescriptor
    //   add the created descriptor in a tree, below the "engine" descriptor
    return engine;
  }

  public void execute(ExecutionRequest request) {
    var engine = request.getRootTestDescriptor();
    var listener = request.getEngineExecutionListener();
    listener.executionStarted(engine);
    // iterate engine.getChildren() recursively and process each via:
    //    1. tell the listener we started
    //    2. try to execute the container/test and evaluate its result
    //    3. tell the listener about the test execution result
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  public Optional<String> getGroupId() {
    return Optional.of("org.testng");
  }

  public Optional<String> getArtifactId() {
    return Optional.of("testng-junit5");
  }

  public Optional<String> getVersion() {
    return Optional.of("DEVELOPMENT");
  }
}
