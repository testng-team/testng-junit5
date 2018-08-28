package org.testng.junit5;

import static org.junit.platform.commons.util.ReflectionUtils.isAbstract;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;
import static org.junit.platform.commons.util.ReflectionUtils.isPublic;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class NGClassDescriptor extends AbstractTestDescriptor {

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

  static NGClassDescriptor newContainerDescriptor(UniqueId container, Class<?> candidate) {
    UniqueId id = container.append("testng-class", candidate.getTypeName());
    return new NGClassDescriptor(id, candidate.getSimpleName(), ClassSource.from(candidate));
  }

  private NGClassDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
    super(uniqueId, displayName, source);
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }
}
