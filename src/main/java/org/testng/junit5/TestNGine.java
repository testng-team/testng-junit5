package org.testng.junit5;

import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.lang.reflect.Method;
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
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class TestNGine implements TestEngine {

  static String ENGINE_ID = "testng-junit5";

  static String ENGINE_DISPLAY_NAME = "TestNG TestEngine SPIKE";

  public String getId() {
    return ENGINE_ID;
  }

  public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
    EngineDescriptor engine = new EngineDescriptor(uniqueId, ENGINE_DISPLAY_NAME);
    // inspect "request" selectors and filters passed by the user
    // find TestNG-based test containers (classes) and tests (methods)
    //   wrap each in a new TestDescriptor
    //   add the created descriptor in a tree, below the "engine" descriptor
    ClassFilter filter =
        ClassFilter.of(buildClassNamePredicate(request), NGClassDescriptor::isCandidate);
    DiscoveryHelper helper = new DiscoveryHelper(request, filter);
    helper.discover(engine, this::handle);
    return engine;
  }

  private void handle(EngineDescriptor engine, Class<?> candidate) {
    NGClassDescriptor container =
        NGClassDescriptor.newContainerDescriptor(engine.getUniqueId(), candidate);
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
    TestDescriptor engine = request.getRootTestDescriptor();
    EngineExecutionListener listener = request.getEngineExecutionListener();
    listener.executionStarted(engine);

    // iterate engine.getChildren() recursively and process each via:
    //    1. tell the listener we started
    //    2. try to execute the container/test and evaluate its result
    //    3. tell the listener about the test execution result
    for (TestDescriptor classDescriptor : engine.getChildren()) {
      listener.executionStarted(classDescriptor);

      Class<?>[] testClasses = {((NGClassDescriptor) classDescriptor).getTestClass()};
      TestListener testListener = new TestListener(listener, classDescriptor.getUniqueId());

      TestNG testNG = new TestNG(false);
      testNG.addListener((ITestNGListener) testListener);
      testNG.setTestClasses(testClasses);
      testNG.run();

      listener.executionFinished(classDescriptor, TestExecutionResult.successful());
    }
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

  class TestListener implements ITestListener {

    final EngineExecutionListener platform;
    final UniqueId classDescriptorId;

    TestListener(EngineExecutionListener platform, UniqueId classDescriptorId) {
      this.platform = platform;
      this.classDescriptorId = classDescriptorId;
    }

    private TestDescriptor toDescriptor(ITestResult result) {
      Method method = result.getMethod().getConstructorOrMethod().getMethod();
      return NGMethodDescriptor.newMethodDescriptor(classDescriptorId, method);
    }

    @Override
    public void onTestStart(ITestResult result) {
      // System.out.println("TestListener.onTestStart" + " " + result);
      platform.executionStarted(toDescriptor(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
      // System.out.println("TestListener.onTestSuccess" + " " + result);
      platform.executionFinished(toDescriptor(result), TestExecutionResult.successful());
    }

    @Override
    public void onTestFailure(ITestResult result) {
      // System.out.println("TestListener.onTestFailure" + " " + result);
      platform.executionFinished(
          toDescriptor(result), TestExecutionResult.failed(result.getThrowable()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
      // System.out.println("TestListener.onTestSkipped" + " " + result);
      platform.executionSkipped(toDescriptor(result), "because");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      // System.out.println("TestListener.onTestFailedButWithinSuccessPercentage" + " " + result);
      platform.executionFinished(toDescriptor(result), TestExecutionResult.successful());
    }

    @Override
    public void onStart(ITestContext context) {
      // System.out.println("TestListener.onStart" + " " + context);
    }

    @Override
    public void onFinish(ITestContext context) {
      // System.out.println("TestListener.onFinish" + " " + context);
    }
  }
}
