package com.rb.nonbiz.reflection;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

public class ClassWithDepth {

  private final Class<?> clazz;
  private final int depth;

  private ClassWithDepth(Class<?> clazz, int depth) {
    this.clazz = clazz;
    this.depth = depth;
  }

  public static ClassWithDepth classWithDepth(Class<?> clazz, int depth) {
    RBPreconditions.checkArgument(depth >= 0);
    return new ClassWithDepth(clazz, depth);
  }

  public Class<?> getClassObject() {
    return clazz;
  }

  public int getDepth() {
    return depth;
  }

  @Override
  public String toString() {
    return Strings.format("[CWD %s %s CWD]", clazz, depth);
  }

}
