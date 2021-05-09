package com.rb.nonbiz.reflection;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.reflection.ClassWithDepth.classWithDepth;
import static com.rb.nonbiz.text.Strings.formatListInExistingOrder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This is a listing of classes, together with their 'depth' in a search. For example, if there's a base class A
 * and two subclasses A1 and A2, then we'll have { A : 0, A1 : 1, A2 : 1 }.
 */
public class UniqueClassesWithDepth {

  private final List<ClassWithDepth> rawList;

  private UniqueClassesWithDepth(List<ClassWithDepth> rawList) {
    this.rawList = rawList;
  }

  public static UniqueClassesWithDepth uniqueClassesWithDepth(List<ClassWithDepth> rawList) {
    RBPreconditions.checkUnique(
        Iterators.transform(rawList.iterator(), v -> v.getClassObject()),
        "No class may appear more than once");
    return new UniqueClassesWithDepth(rawList);
  }

  public static UniqueClassesWithDepth emptyUniqueClassesWithDepth() {
    return uniqueClassesWithDepth(emptyList());
  }

  public static UniqueClassesWithDepth singletonUniqueClassesWithDepth(Class<?> clazz, int depth) {
    return uniqueClassesWithDepth(singletonList(classWithDepth(clazz, depth)));
  }

  public List<ClassWithDepth> getRawList() {
    return rawList;
  }

  @Override
  public String toString() {
    // It's better to use ClassesWithDepthMultilineStringFormatter if you can.
    return Strings.format("[CWD %s CWD]", formatListInExistingOrder(rawList));
  }

}
