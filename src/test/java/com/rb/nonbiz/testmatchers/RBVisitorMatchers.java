package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBVisitorMatchers {

  public static class VisitorMatchInfo<T> {

    private final int caseNumber;
    private final T object;
    private final MatcherGenerator<? extends T> matcherGenerator;

    private VisitorMatchInfo(int caseNumber, T object, MatcherGenerator<? extends T> matcherGenerator) {
      this.caseNumber = caseNumber;
      this.object = object;
      this.matcherGenerator = matcherGenerator;
    }

    public static <X> VisitorMatchInfo<X> visitorMatchInfo(int caseNumber, X object, MatcherGenerator<? extends X> matcherGenerator) {
      return new VisitorMatchInfo<X>(caseNumber, object, matcherGenerator);
    }

    public int getCaseNumber() {
      return caseNumber;
    }

    public T getObject() {
      return object;
    }

    public MatcherGenerator<? extends T> getMatcherGenerator() {
      return matcherGenerator;
    }

  }

  /**
   * This is a relatively general way of creating matchers for the common case where an
   * abstract base class (with N different implementations) has a visitor with N methods
   * (1 for each of its N implementations), and where each of those methods takes as an argument
   * an object of that implementation.
   *
   * The advantage is that we don't have to have N^2 cases to cover in the code for the cases where the
   * visitor covers N different implementations.
   * The number of lines of code is now proportional to N than to N^2.
   *
   * Look at a usage of this for a better understanding, as it's easier to see its usefulness in an example.
   */
  public static <T> TypeSafeMatcher<T> generalVisitorMatcher(T expected,
                                                              Function<T, VisitorMatchInfo<T>> extractor) {
    return makeMatcher(expected, actual -> {
      VisitorMatchInfo<T> expectedTriple = extractor.apply(expected);
      VisitorMatchInfo<T> actualTriple = extractor.apply(actual);
      if (expectedTriple.getCaseNumber() != actualTriple.getCaseNumber()) {
        return false;
      }
      MatcherGenerator<T> matcherGenerator = (MatcherGenerator<T>) expectedTriple.getMatcherGenerator();
      TypeSafeMatcher<T> matcher = matcherGenerator.apply(expectedTriple.getObject());
      return matcher.matches(actualTriple.getObject());
    });

  }

}
