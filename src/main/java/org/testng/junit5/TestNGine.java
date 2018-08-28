package org.testng.junit5;

import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.util.Arrays;
import java.util.Optional;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.testng.annotations.Test;

public class TestNGine implements TestEngine {

  static String ENGINE_ID = "testng-junit5";

  static String ENGINE_DISPLAY_NAME = "TestNG TestEngine SPIKE";

  public String getId() {
    return ENGINE_ID;
  }

  public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
    var engine = new EngineDescriptor(uniqueId, ENGINE_DISPLAY_NAME);
    // inspect "request" selectors and filters passed by the user
    // find TestNG-based test containers (classes) and tests (methods)
    //   wrap each in a new TestDescriptor
    //   add the created descriptor in a tree, below the "engine" descriptor
    var filter = ClassFilter.of(buildClassNamePredicate(request), NGClassDescriptor::isCandidate);
    var helper = new DiscoveryHelper(request, filter);
    helper.discover(engine, this::handle);
    return engine;
  }

  private void handle(EngineDescriptor engine, Class<?> candidate) {
    var container = NGClassDescriptor.newContainerDescriptor(engine.getUniqueId(), candidate);
    Arrays.stream(candidate.getMethods())
        .filter(method -> method.isAnnotationPresent(Test.class))
        .map(method -> NGMethodDescriptor.newMethodDescriptor(container.getUniqueId(), method))
        .forEach(container::addChild);
    if (container.getChildren().isEmpty()) {
      return;
    }
    engine.addChild(container);
  }

  public void execute(ExecutionRequest request) {
    var engine = request.getRootTestDescriptor();
    var listener = request.getEngineExecutionListener();
    listener.executionStarted(engine);
    // iterate engine.getChildren() recursively and process each via:
    //    1. tell the listener we started
    //    2. try to execute the container/test and evaluate its result
    //    3. tell the listener about the test execution result
    for (var classDescriptor : engine.getChildren()) {
      listener.executionStarted(classDescriptor);
      for (var methodDescriptor : classDescriptor.getChildren()) {
        listener.executionStarted(methodDescriptor);
        var result = executeMethod((NGMethodDescriptor) methodDescriptor);
        listener.executionFinished(methodDescriptor, result);
      }
      listener.executionFinished(classDescriptor, TestExecutionResult.successful());
    }
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  private TestExecutionResult executeMethod(NGMethodDescriptor descriptor) {
    try {
      var target = descriptor.getMethod().getDeclaringClass().getConstructor().newInstance();
      descriptor.getMethod().invoke(target);
    } catch (ReflectiveOperationException e) {
      return TestExecutionResult.failed(e);
    }
    return TestExecutionResult.successful();
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
