package com.rb.nonbiz.reflection;

import com.rb.nonbiz.collections.MutableRBSet;
import com.rb.nonbiz.collections.RBSet;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.reflection.ClassWithDepth.classWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.emptyUniqueClassesWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.singletonUniqueClassesWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.uniqueClassesWithDepth;

/**
 * Enumerates a data class and its fields, and - if specified as recursive, its fields' fields as well.
 */
public class DataClassRecursiveEnumerator {

  public UniqueClassesWithDepth enumerateRecursively(Class<?> rootClass, RBSet<Class<?>> ignoreThese) {
    MutableRBSet<Class<?>> encountered = newMutableRBSetWithExpectedSize(100); // best guess
    // Technically, the set of classes to include is not the same as 'encountered' in the search,
    // but this simplifies things, because we only need to pass a mutable set to enumerateHelper.
    encountered.addAll(ignoreThese.asSet());
    return enumerateHelper(rootClass, 0, true, encountered);
  }

  public UniqueClassesWithDepth enumerateNonRecursively(Class<?> rootClass, RBSet<Class<?>> ignoreThese) {
    MutableRBSet<Class<?>> encountered = newMutableRBSetWithExpectedSize(100); // best guess
    // Technically, the set of classes to include is not the same as 'encountered' in the search,
    // but this simplifies things, because we only need to pass a mutable set to enumerateHelper.
    encountered.addAll(ignoreThese.asSet());
    return enumerateHelper(rootClass, 0, false, encountered);
  }

  private UniqueClassesWithDepth enumerateHelper(
      Class<?> rootClass,
      int depth,
      boolean recursively,
      MutableRBSet<Class<?>> encountered) {
    if (encountered.contains(rootClass) || rootClass.isPrimitive()) {
      // Break the recursion so we won't include anything twice. Also, skip primitives (int/boolean/long/double/float).
      return emptyUniqueClassesWithDepth();
    }
    if (rootClass.isArray()) {
      // For Foo[], print out the details of Foo instead.
      return enumerateHelper(rootClass.getComponentType(), depth, recursively, encountered);
    }
    // OK, this is a regular class.
    encountered.add(rootClass);

    return !recursively
        ? singletonUniqueClassesWithDepth(rootClass, depth)
        : uniqueClassesWithDepth(
        Stream.concat(
            Stream.of(classWithDepth(rootClass, depth)),
            Arrays.stream(rootClass.getDeclaredFields())
                .flatMap(field ->
                    enumerateHelper(field.getType(), depth + 1, recursively, encountered).getRawList().stream()))
            .collect(Collectors.toList()));
  }

}
