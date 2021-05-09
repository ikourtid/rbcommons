package com.rb.nonbiz.reflection;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.reflection.ClassWithDepth.classWithDepth;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class ClassWithDepthTest extends RBTestMatcher<ClassWithDepth> {

  @Override
  public ClassWithDepth makeTrivialObject() {
    return classWithDepth(Money.class, 0);
  }

  @Override
  public ClassWithDepth makeNontrivialObject() {
    return classWithDepth(UnitFraction.class, 2);
  }

  @Override
  public ClassWithDepth makeMatchingNontrivialObject() {
    return classWithDepth(UnitFraction.class, 2);
  }

  @Override
  protected boolean willMatch(ClassWithDepth expected, ClassWithDepth actual) {
    return classWithDepthMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ClassWithDepth> classWithDepthMatcher(ClassWithDepth expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassObject()),
        matchUsingEquals(v -> v.getDepth()));
  }

}