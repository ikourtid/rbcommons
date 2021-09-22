package com.rb.nonbiz.testutils;

import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.PrintsMultilineString;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests for a data class Settings (example) should extend {@code RBTestMatcher<Settings>}.
 *
 * This is a convenience to force you to do a subset of the necessary testing
 * when a data class has a matcher created for it.
 * By 'subset' we mean that this infrastructure can't force you to test your static constructors
 * under various inputs to see if they fail. It's mostly to test the matcher, plus to verify
 * that instantiation of some simple objects won't throw an exception.
 *
 * So: you usually need to add more tests to a data class unit test class!
 *
 * RBTestMatcher creates an inherited unit test that will compare a trivial object
 * (e.g. an empty one - usually there is such a thing with collections etc),
 * against a nontrivial object, and another object that matches that nontrivial object.
 * All checks will happen in matcherMetaTest, so you don't have to remember yourself to do them in your own test class.
 *
 * There should be a public matcher with the following signature and name. The contents of the method
 * should try to match all fields in the object that we care about matching.
 * Static methods can't be part of an interface definition, so it's not possible to force you to create them
 * (i.e. to have the code not compile otherwise). But here is a rough outline, using the examples below:
 *
 * <pre> {@code
 * public static TypeSafeMatcher<Settings> settingsMatcher(Settings expected) {
 *   return makeMatcher(expected,
 *     match(v -> v.getIntSetting(),     f -> typeSafeEqualTo(f)),
 *     match(v -> v.getDoubleSetting1(), f -> doubleAlmostEqualsMatcher(f, 1e-8)),
 *     match(v -> v.getDoubleSetting2(), f -> doubleAlmostEqualsMatcher(f, 1e-8)));
 * }} </pre>
 *
 * This says '2 objects of type Settings are considered equal (for testing purposes)
 * if their intSetting field is exactly the same, and their doubleSetting1 and 2 (respectively) fields
 * are 'almost the same'.
 *
 * Note that there exist some handy shortcuts in special cases where we check for equality and for
 * 'numbers almost equal'. Here is an equivalent version of the matcher above, but shorter:
 *
 * <pre> {@code
 * public static TypeSafeMatcher<Settings> settingsMatcher(Settings expected) {
 *   return makeMatcher(expected,
 *     matchUsingEquals(v -> v.getIntSetting()),
 *     matchUsingAlmostEquals(v -> v.getDoubleSetting1(), 1e-8),
 *     matchUsingAlmostEquals(v -> v.getDoubleSetting2(), 1e-8));
 * }} </pre>
 *
 * Sometimes you will see an older style of this static matcher, which looks like this:
 *
 * <pre> {@code
 * public static TypeSafeMatcher<Settings> settingsMatcher(Settings expected) {
 *   return makeMatcher(expected, actual ->
 *     expected.getIntSetting().equals(actual.getIntSetting()))
 *     && doubleAlmostEqualsMatcher(expected.getDoubleSetting1(), 1e-8).matches(actual.getDoubleSetting1())
 *     && doubleAlmostEqualsMatcher(expected.getDoubleSetting2(), 1e-8).matches(actual.getDoubleSetting2()));
 * }} </pre>
 *
 * This is a bit more error-prone, because the left and right side needs to be repeated, and it's easy to mess up.
 * However, there is a subset of cases where you need to use that.
 * When? Well, you'll notice because the code won't compile.
 *
 * Note that we (intentionally) rarely want to match on HumanReadableLabel. This is the one exception.
 * The whole point of the label is that its values are only for reading, and should never affect logic.
 */
public abstract class RBTestMatcher<T> {

  /**
   * Create the simplest test object you can think of. Examples:
   * * if you are testing a map, this should return an empty map.
   * * if you are testing a data class that stores an int and 2 doubles,
   *   this should return something like (0, 0.0, 0.0) if that is a valid value. Code example:
   *
   * <pre>
   * {@code @Override}
   * {@code public Settings makeTrivialObject() {
   *   return settings(0, 0.0, 0.0);
   * }} </pre>
   */
  public abstract T makeTrivialObject();

  /**
   * Create a 'general' object, i.e. not a trivially simple one.
   * * if you are testing a map, this should return a map of &gt; 1 item.
   * * if you are testing a data class that stores an int and 2 doubles,
   * this should return something like (100, -1.1, 2.2) if it is a valid value. Code example:
   *
   * <pre>
   * {@code @Override}
   * {@code public Settings makeTrivialObject() {
   *   return settings(100, -1.1, 2.2);
   * }} </pre>
   */
  public abstract T makeNontrivialObject();

  /**
   * We often need to create *some* valid object of a class, but its value doesn't matter in the logic.
   * Use this when you want to have explicit semantics about needing a dummy value.
   * This is somewhat less of an issue if you create a local variable, where you can add 'dummy' in its name,
   * but it's useful when you're using such a dummy object in-line, which means you'd otherwise have to have a comment.
   */
  // Issue #927: go through the code and look for "// dummy" and replace with this, when applicable.
  public T makeDummyObject() {
    return makeNontrivialObject();
  }

  /**
   * Like makeNontrivialObject, except that this should generate an object which is slightly different,
   * but which we want to match the output of makeNontrivialObject - i.e. 'not too different'.
   * As per the examples above:
   *
   * <pre>
   * {@code @Override}
   * {@code public Settings makeTrivialObject() {
   *   double e = 1e-9; // epsilon
   *   return settings(100, -1.1 + e, 2.2 + e);
   * }}
   * </pre>
   *
   * In the case above, if we hardcode the static matcher to consider double values within 1e-8 as matching
   * (even when slightly unequal), then perturbing the double values of the original object by 1e-9 &lt; 1e-8
   * should result in matching objects.
   *
   * In some cases, there may not be a meaningful value for this other than makeNontrivialObject.
   * E.g. if settings only took ints, there would not be some well-defined notion
   * of 'different, but not too different'. Same goes for e.g. a string.
   */
  public abstract T makeMatchingNontrivialObject();

  /**
   * Since you should always have a static matcher (in the example above, settingsMatcher),
   * this should just call the static matcher, and do nothing else. Example:
   *
   * <pre>
   * {@code @Override}
   * {@code protected boolean willMatch(Settings expected, Settings actual) {
   *   return settingsMatcher(expected).matches(actual);
   * }}
   * </pre>
   */
  protected abstract boolean willMatch(T expected, T actual);

  /**
   * This is unusual with unit tests, but this test gets inherited and executes whenever each
   * parent class's tests execute.
   *
   * The idea is that the 2 'nontrivial' objects must match with each other, but not with the trivial one.
   * Also, every object must match itself.
   *
   * This set of test cases isn't exhaustive, but it's pretty good.
   */
  @Test
  public void matcherMetaTest() {
    assertTrue(
        "makeTrivialObject() should match itself",
        willMatch(makeTrivialObject(), makeTrivialObject()));
    assertFalse(
        "makeTrivialObject() should not match makeNontrivialObject()",
        willMatch(makeTrivialObject(), makeNontrivialObject()));
    assertFalse(
        "makeNonTrivialObject() should not match makeTrivialObject()",
        willMatch(makeNontrivialObject(), makeTrivialObject()));
    // We create separate objects because if the matcher erroneously does simple pointer equality,
    // the test will pass in cases when it shouldn't.
    assertTrue(
        "makeNontrivialObject() should match itself",
        willMatch(
            makeNontrivialObject(),
            makeNontrivialObject()));
    assertTrue(
        "makeMatchingNontrivialObject() should match itself",
        willMatch(
            makeMatchingNontrivialObject(),
            makeMatchingNontrivialObject()));
    assertTrue(
        "makeNontrivialObject() should match makeMatchingNontrivialObject()",
        willMatch(
            makeNontrivialObject(),
            makeMatchingNontrivialObject()));
    assertTrue(
        "makeMatchingNontrivialObject() should match makeNontrivialObject()",
        willMatch(
            makeMatchingNontrivialObject(),
            makeNontrivialObject()));

    // Most data classes do not support hashCode/equals. In those cases, the following will be pointer comparisons
    // (the default #equals behavior), and since by construction we are dealing with different objects
    // (regardless of their contents), the two objects will be unequal.
    assertNotEquals(makeTrivialObject(), makeNontrivialObject());
    assertNotEquals(makeTrivialObject(), makeMatchingNontrivialObject());
    // However, the following cannot stay in. There are many cases where the Nontrivial and MatchingNontrivial
    // cannot be tweaked to be 'almost same, but not same' versions with each other, AND the data class
    // supports hashCode/equals. In those cases, Nontrivial and MatchingNontrivial would actually be equal.
    // assertNotEquals(makeNontrivialObject(), makeMatchingNontrivialObject());
  }

  @Test
  public void testToStringOnAll3Objects() {
    String mustNotThrow;
    mustNotThrow = makeTrivialObject().toString();
    mustNotThrow = makeNontrivialObject().toString();
    mustNotThrow = makeMatchingNontrivialObject().toString();
  }

  @Test
  public void testToStringOnAll3Objects_ifPrintsInstruments() {
    T trivialObject = makeTrivialObject();
    if (!PrintsInstruments.class.isAssignableFrom(trivialObject.getClass())) {
      return;
    }
    String mustNotThrow;
    mustNotThrow = ((PrintsInstruments) trivialObject)
        .toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
    mustNotThrow = ((PrintsInstruments) makeNontrivialObject())
        .toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
    mustNotThrow = ((PrintsInstruments) makeMatchingNontrivialObject())
        .toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Test
  public void testGetHumanReadableLabelOnAll3Objects_ifHasHumanReadableLabel() {
    T trivialObject = makeTrivialObject();
    if (!HasHumanReadableLabel.class.isAssignableFrom(trivialObject.getClass())) {
      return;
    }
    HumanReadableLabel mustNotThrow;
    mustNotThrow = ((HasHumanReadableLabel) trivialObject)
        .getHumanReadableLabel();
    mustNotThrow = ((HasHumanReadableLabel) makeNontrivialObject())
        .getHumanReadableLabel();
    mustNotThrow = ((HasHumanReadableLabel) makeMatchingNontrivialObject())
        .getHumanReadableLabel();
  }

  @Test
  public void testToMultilineStringOnAll3Objects_ifPrintsMultilineString() {
    T trivialObject = makeTrivialObject();
    if (!PrintsMultilineString.class.isAssignableFrom(trivialObject.getClass())) {
      return;
    }
    String mustNotThrow;
    mustNotThrow = ((PrintsMultilineString) trivialObject)
        .toMultilineString();
    mustNotThrow = ((PrintsMultilineString) makeNontrivialObject())
        .toMultilineString();
    mustNotThrow = ((PrintsMultilineString) makeMatchingNontrivialObject())
        .toMultilineString();
  }

}
