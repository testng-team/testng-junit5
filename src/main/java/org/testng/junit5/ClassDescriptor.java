package org.testng.junit5;

import static org.junit.platform.commons.util.ReflectionUtils.isAbstract;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;
import static org.junit.platform.commons.util.ReflectionUtils.isPublic;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class ClassDescriptor extends AbstractTestDescriptor {

  static boolean isCandidate(Class<?> candidate) {
    if (!isPublic(candidate)) {
      return false;
    }
    if (isAbstract(candidate)) {
      return false;
    }
    if (isInnerClass(candidate)) {
      return false;
    }
    return true;
  }

  static ClassDescriptor newContainerDescriptor(UniqueId container, Class<?> testClass) {
    UniqueId id = container.append("testng-class", testClass.getTypeName());
    return new ClassDescriptor(id, testClass);
  }

  private final Class<?> testClass;

  private ClassDescriptor(UniqueId uniqueId, Class<?> testClass) {
    super(uniqueId, testClass.getSimpleName(), ClassSource.from(testClass));
    this.testClass = testClass;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  public Class<?> getTestClass() {
    return testClass;
  }
}
