package com.rb.nonbiz.testmatchers;

import com.google.common.collect.ClassToInstanceMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Contains TypeSafeMatcher code for Guava classes.
 */
public class RBClassToInstanceMapMatchers {

  public static <T> TypeSafeMatcher<ClassToInstanceMap<T>> classToInstanceMapEqualityMatcher(ClassToInstanceMap<T> expected) {
    return new TypeSafeMatcher<ClassToInstanceMap<T>>() {
      @Override
      protected boolean matchesSafely(ClassToInstanceMap<T> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (Class<? extends T> key : expected.keySet()) {
          if (!expected.get(key).equals(actual.get(key))) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("ClassToInstanceMap: expected %s", expected));
      }
    };
  }

  public static <T> TypeSafeMatcher<ClassToInstanceMap<T>> classToInstanceMapGeneralMatcher(
      ClassToInstanceMap<T> expected, RBMap<Class<? extends T>, MatcherGenerator<? extends T>> matcherGeneratorsByClass) {
    return new TypeSafeMatcher<ClassToInstanceMap<T>>() {
      @Override
      protected boolean matchesSafely(ClassToInstanceMap<T> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (Class<? extends T> clazz : expected.keySet()) {
          MatcherGenerator<? extends T> appropriateMatcherGenerator = matcherGeneratorsByClass.getOrThrow(clazz);
          T expectedInstance = expected.get(clazz);
          T actualInstance = actual.get(clazz);
          // horrible, I know.
          // This has to do with the fact that 'matcherGeneratorsByClass' does not guarantee that the key and value
          // in each entry match. All it says is that they both have to be subclasses of T. But they can be
          // different subclasses.
          // Since this is in test, the worst case is that we pass in a bad map: e.g.
          // rbMapOf(T1.class, T2instance, T2.class, T1instance), where A1 and A2 both derive from T.
          // But then we'd catch this when a test fails.
          if (!((MatcherGenerator<T>) appropriateMatcherGenerator).apply(expectedInstance).matches(actualInstance)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("ClassToInstanceMap: expected %s", expected));
      }
    };
  }

}
