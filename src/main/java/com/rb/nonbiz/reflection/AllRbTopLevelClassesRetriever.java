package com.rb.nonbiz.reflection;

import com.google.common.reflect.ClassPath;

import java.io.IOException;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.reflection.AllRbTopLevelClasses.allRbTopLevelClasses;

/**
 * Retrieves all top-level (non-inner classes) in the com.rb package in the rb module, including test ones.
 *
 * @see AllRbTopLevelClasses
 */
public class AllRbTopLevelClassesRetriever {

  @SuppressWarnings("UnstableApiUsage")
  public AllRbTopLevelClasses retrieve() {
    try {
      ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
      return allRbTopLevelClasses(newRBSet(classPath.getTopLevelClassesRecursive("com.rb")
          .stream()
          .map(classInfo -> classInfo.load())));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
