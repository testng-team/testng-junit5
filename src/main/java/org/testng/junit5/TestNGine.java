package org.testng.junit5;

import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.util.Arrays;
import java.util.Optional;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class TestNGine implements TestEngine {

  static String ENGINE_ID = "testng-junit5";

  static String ENGINE_DISPLAY_NAME = "TestNG TestEngine SPIKE";

  public String getId() {
    return ENGINE_ID;
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

  public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
    EngineDescriptor engine = new EngineDescriptor(uniqueId, ENGINE_DISPLAY_NAME);
    // inspect "request" selectors and filters passed by the user
    // find TestNG-based test containers (classes) and tests (methods)
    //   wrap each in a new TestDescriptor
    //   add the created descriptor in a tree, below the "engine" descriptor
    ClassFilter filter =
        ClassFilter.of(buildClassNamePredicate(request), ClassDescriptor::isCandidate);
    DiscoveryHelper helper = new DiscoveryHelper(request, filter);
    helper.discover(engine, this::handle);
    return engine;
  }

  private void handle(EngineDescriptor engine, Class<?> candidate) {
    ClassDescriptor container =
        ClassDescriptor.newContainerDescriptor(engine.getUniqueId(), candidate);
    Arrays.stream(candidate.getMethods())
        .filter(method -> method.isAnnotationPresent(Test.class))
        .map(method -> MethodDescriptor.newMethodDescriptor(container.getUniqueId(), method))
        .forEach(container::addChild);
    if (container.getChildren().isEmpty()) {
      return;
    }
    engine.addChild(container);
  }

  public void execute(ExecutionRequest request) {
    TestDescriptor engine = request.getRootTestDescriptor();
    EngineExecutionListener engineListener = request.getEngineExecutionListener();
    engineListener.executionStarted(engine);

    for (TestDescriptor classDescriptor : engine.getChildren()) {
      engineListener.executionStarted(classDescriptor);

      UniqueId classId = classDescriptor.getUniqueId();
      Class<?>[] testClasses = {((ClassDescriptor) classDescriptor).getTestClass()};

      TestNG testNG = new TestNG(false);
      testNG.addListener(new TestListener(engineListener, classId));
      testNG.setTestClasses(testClasses);
      testNG.run();

      engineListener.executionFinished(classDescriptor, TestExecutionResult.successful());
    }
    engineListener.executionFinished(engine, TestExecutionResult.successful());
  }
}
