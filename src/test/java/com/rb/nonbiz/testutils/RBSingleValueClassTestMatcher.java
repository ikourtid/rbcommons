package com.rb.nonbiz.testutils;

import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

/**
 * This is a version of {@link RBTestMatcher} for the special cases where all instances of a class match each other.
 *
 * <p> The class does not necessarily have to have a single instance, from a pure pointer point of view;
 * there could be many, in the sense that there can be multiple objects in the runtime.
 * </p>
 *
 * @see RBTestMatcher
 */
public abstract class RBSingleValueClassTestMatcher<T> {

  public abstract T makeOnlyObject();

  /**
   * Since you should always have a static matcher,
   * this should just call the static matcher, and do nothing else. Example:
   *
   * <pre>
   * {@code @Override}
   * {@code protected boolean willMatch(Settings expected, Settings actual) {
   *     return settingsMatcher(expected).matches(actual);
   *   }
   * } </pre>
   *
   * This isn't really necessary since it will always return true. However, it makes it easier to migrate
   * test classes that extend RBSingleValueClassTestMatcher to instead extend RBTestMatcher, if the prod class has
   * fields added to it.
   * Plus, we need some way to assert that the static matcher says that the 'only object' matches itself.
   * However, because the matcher is static, it can't be some abstract method that subclasses are forced to implement.
   * So we might as well use the RBTestMatcher convention of getting access to the static matcher
   * in tests via the getMatcher method, which calls willMatch.
   */
  protected abstract boolean willMatch(T expected, T actual);

  protected TypeSafeMatcher<T> getMatcher(T expected) {
    return new TypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        return willMatch(expected, actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected %s", expected));
      }
    };
  }

  /**
   * This is unusual with unit tests, but this test gets inherited and executes whenever each
   * parent class's tests execute.
   *
   * You may wonder why we even have a matcher for this, if we know we only have an only valid instance.
   * The reason is that, if we ever add another field to this object, it will be easier to change
   * the test class from inheriting RBSingleValueClassTestMatcher to inheriting RBTestMatcher.
   */
  @Test
  public void matcherMetaTest() {
    assertThat(
        makeOnlyObject(),
        getMatcher(makeOnlyObject()));
  }

  @Test
  public void testToStringOnOnlyObject() {
    String mustNotThrow;
    mustNotThrow = makeOnlyObject().toString();
  }

  @Test
  public void doesNotImplementPrintsInstruments() {
    assertFalse(
        "If an object implements PrintsInstruments, it must have some state, so it has more than 1 valid instances "
        + "and therefore cannot extend RBSingleValueClassTestMatcher",
        PrintsInstruments.class.isAssignableFrom(makeOnlyObject().getClass()));
  }

}
