package com.rb.nonbiz.reflection;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.reflection.ClassWithDepth.classWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.emptyUniqueClassesWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.singletonUniqueClassesWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.uniqueClassesWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepthTest.classesWithDepthMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class DataClassRecursiveEnumeratorTest extends RBTest<DataClassRecursiveEnumerator> {

  private static class ClassA1 { }
  private static class ClassA2 { }
  private static class ClassB1 { }
  private static class ClassB2 { }

  private static class ClassA {

    private ClassA1 classA1;
    private ClassA2[] classA2Array;

  }

  private static class ClassB {

    private ClassB1 classB1;
    private ClassB2[] classB2Array;

  }

  private static class ClassAB {
    private ClassA classA;
    private ClassB classB;
  }

  private static class TopClass {

    private ClassA classA;
    private ClassB classB;
    private ClassAB classAB;
    private TopClass topClass1;
    private TopClass topClass2;

  }

  @Test
  public void testNonRecursively() {
    assertThat(
        makeTestObject().enumerateNonRecursively(TopClass.class, emptyRBSet()),
        classesWithDepthMatcher(
            singletonUniqueClassesWithDepth(
                TopClass.class, 0)));
    assertThat(
        makeTestObject().enumerateNonRecursively(TopClass.class, singletonRBSet(TopClass.class)),
        classesWithDepthMatcher(
            emptyUniqueClassesWithDepth()));
  }

  @Test
  public void testRecursively_noExclusions() {
    // Note that the results are deterministic, but there are multiple valid answers, depending on the ordering of
    // the fields. For example, the classAB field of type ClassAB appears after the ClassA and ClassB ones,
    // so ClassA and ClassB are enumerated directly off of TopClass, not off of ClassAB, which itself holds a
    // ClassA and ClassB.
    assertThat(
        makeTestObject().enumerateRecursively(TopClass.class, emptyRBSet()),
        classesWithDepthMatcher(
            uniqueClassesWithDepth(ImmutableList.of(
                classWithDepth(TopClass.class, 0),
                classWithDepth(ClassA.class, 1),
                classWithDepth(ClassA1.class, 2),
                classWithDepth(ClassA2.class, 2),
                classWithDepth(ClassB.class, 1),
                classWithDepth(ClassB1.class, 2),
                classWithDepth(ClassB2.class, 2),
                classWithDepth(ClassAB.class, 1)))));
  }

  @Test
  public void testRecursively_withExclusions() {
    assertThat(
        makeTestObject().enumerateRecursively(TopClass.class, singletonRBSet(ClassA.class)),
        classesWithDepthMatcher(
            uniqueClassesWithDepth(ImmutableList.of(
                classWithDepth(TopClass.class, 0),
                classWithDepth(ClassB.class, 1),
                classWithDepth(ClassB1.class, 2),
                classWithDepth(ClassB2.class, 2),
                classWithDepth(ClassAB.class, 1)))));
  }

  @Override
  protected DataClassRecursiveEnumerator makeTestObject() {
    return new DataClassRecursiveEnumerator();
  }

}