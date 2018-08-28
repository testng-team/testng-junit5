package org.testng.junit5;

import java.lang.reflect.Method;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class NGMethodDescriptor extends AbstractTestDescriptor {

  static NGMethodDescriptor newMethodDescriptor(UniqueId container, Method method) {
    var id = container.append("testng-method", method.getName());
    return new NGMethodDescriptor(id, method);
  }

  private final Method method;

  private NGMethodDescriptor(UniqueId uniqueId, Method method) {
    super(uniqueId, method.getName(), MethodSource.from(method));
    this.method = method;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  public Method getMethod() {
    return method;
  }
}