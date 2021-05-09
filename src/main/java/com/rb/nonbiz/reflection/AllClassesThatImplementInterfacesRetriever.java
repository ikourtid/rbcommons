package com.rb.nonbiz.reflection;

import com.rb.nonbiz.collections.RBSet;

import java.util.Arrays;

import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * Returns a set of classes that implement one or more of the specified interfaces.
 */
public class AllClassesThatImplementInterfacesRetriever {

  public RBSet<Class<?>> retrieve(
      AllRbTopLevelClasses allRbTopLevelClasses,
      RBSet<Class<?>> retrieveImplementersOfTheseInterfaces) {
    return newRBSet(allRbTopLevelClasses.getRawSet()
        .stream()
        .filter(clazz -> Arrays.stream(clazz.getInterfaces())
            .anyMatch(implemented -> retrieveImplementersOfTheseInterfaces.contains(implemented))));
  }

}
