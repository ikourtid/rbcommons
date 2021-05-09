package com.rb.nonbiz.reflection;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.sequence.ArithmeticProgression;
import com.rb.nonbiz.math.sequence.DoubleSequence;
import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.reflection.ClassWithDepth.classWithDepth;
import static com.rb.nonbiz.reflection.ClassWithDepthTest.classWithDepthMatcher;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.emptyUniqueClassesWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.uniqueClassesWithDepth;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class UniqueClassesWithDepthTest extends RBTestMatcher<UniqueClassesWithDepth> {

  @Override
  public UniqueClassesWithDepth makeTrivialObject() {
    return emptyUniqueClassesWithDepth();
  }

  @Override
  public UniqueClassesWithDepth makeNontrivialObject() {
    return uniqueClassesWithDepth(ImmutableList.of(
        classWithDepth(DoubleSequence.class, 0),
        classWithDepth(ArithmeticProgression.class, 1),
        classWithDepth(GeometricProgression.class, 1)));
  }

  @Override
  public UniqueClassesWithDepth makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return uniqueClassesWithDepth(ImmutableList.of(
        classWithDepth(DoubleSequence.class, 0),
        classWithDepth(ArithmeticProgression.class, 1),
        classWithDepth(GeometricProgression.class, 1)));
  }

  @Override
  protected boolean willMatch(UniqueClassesWithDepth expected, UniqueClassesWithDepth actual) {
    return classesWithDepthMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<UniqueClassesWithDepth> classesWithDepthMatcher(UniqueClassesWithDepth expected) {
    return makeMatcher(expected,
        matchList(v -> v.getRawList(), f -> classWithDepthMatcher(f)));
  }

}