package com.rb.nonbiz.reflection;

import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSetFromPossibleDuplicates;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.reflection.RBClass.rbClass;
import static com.rb.nonbiz.reflection.RBClass.shallowGenericRbClass;
import static com.rb.nonbiz.reflection.RBClass.nonGenericRbClass;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RBClassTest extends RBTestMatcher<RBClass<?>> {

  @Test
  public void variousToStringTests() {
    // We don't normally test the toString output, but this helps us understand what the class represents
    // in the most general case.
    // See toString about the (possibly unexpected) spaces below.
    assertEquals(
        "[CDAG RBMap < String , IidMap < UnitFraction > > CDAG]",
        rbClass(
            RBMap.class,
            nonGenericRbClass(String.class),
            shallowGenericRbClass(
                IidMap.class,
                UnitFraction.class))
            .toString());
  }

  @Test
  public void testEquals() {
    assertEquals(makeTrivialObject(),            makeTrivialObject());
    assertEquals(makeNontrivialObject(),         makeNontrivialObject());
    assertEquals(makeMatchingNontrivialObject(), makeMatchingNontrivialObject());
  }

  @Test
  public void testGetAllClassesAsStream() {
    assertThat(
        newRBSet(rbClass(
            RBMap.class,
            nonGenericRbClass(String.class),
            shallowGenericRbClass(
                IidMap.class,
                UnitFraction.class))
            .getAllClassesAsStream()),
        rbSetEqualsMatcher(
            rbSetOf(RBMap.class, String.class, IidMap.class, UnitFraction.class)));

    // Does not throw if there are duplicate classes, in this case IidMap,
    // as the below represents an IidMap<IidMap<UnitFraction>>.
    assertThat(
        newRBSetFromPossibleDuplicates(rbClass(
            IidMap.class,
            shallowGenericRbClass(
                IidMap.class,
                UnitFraction.class))
            .getAllClassesAsStream()),
        rbSetEqualsMatcher(
            rbSetOf(IidMap.class, UnitFraction.class)));
  }

  @Override
  public RBClass<?> makeTrivialObject() {
    return nonGenericRbClass(UnitFraction.class);
  }

  @Override
  public RBClass<?> makeNontrivialObject() {
    // Represents a RBMap<String, IidMap<UnitFraction>>.
    return rbClass(
        RBMap.class,
        nonGenericRbClass(String.class),
        shallowGenericRbClass(
            IidMap.class,
            UnitFraction.class));
  }

  @Override
  public RBClass<?> makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return rbClass(
        RBMap.class,
        nonGenericRbClass(String.class),
        shallowGenericRbClass(
            IidMap.class,
            UnitFraction.class));
  }

  @Override
  protected boolean willMatch(RBClass<?> expected, RBClass<?> actual) {
    return rbClassMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<RBClass<T>> rbClassMatcher(RBClass<T> expected) {
    return makeMatcher(expected, actual -> 
        rbClassOfAnyTypeMatcher(expected).matches(actual));
  }

  public static TypeSafeMatcher<RBClass<?>> rbClassOfAnyTypeMatcher(RBClass<?> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getOuterClass()),
        matchList(       v -> v.getInnerClassRBClasses(), f -> rbClassOfAnyTypeMatcher(f)));
  }

}
