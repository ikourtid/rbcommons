package com.rb.nonbiz.testutils;

import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.Epsilon;

import java.util.Objects;

import static com.rb.nonbiz.text.UniqueId.uniqueId;

/**
 * Support that allows us to use different epsilons that are context-specific.
 *
 * <p> For example, we may want to use 1e-6 epsilon for buy order quantities, but 1e-4 for buy notional. </p>
 *
 * @see MatcherEpsilons
 */
public abstract class MatcherEpsilonDescriptor<T> {

  protected final Class<T> clazz;

  protected MatcherEpsilonDescriptor(Class<T> clazz) {
    this.clazz = clazz;
  }

  /**
   * Use this inside:
   *
   * {@code public static TypeSafeMatcher<XYZ> xyzMatcher(XYZ expected) { ... }}
   *
   * ... when you don't care about using different epsilons for different getters inside that matcher,
   * which means that specifying the class XYZ only suffices for uniquely identifying which epsilon to use.
   */
  public static class ClassWideMatcherEpsilonDescriptor<T> extends MatcherEpsilonDescriptor<T> {

    private ClassWideMatcherEpsilonDescriptor(Class<T> clazz) {
      super(clazz);
    }

    public static <T> ClassWideMatcherEpsilonDescriptor<T> eps(Class<T> clazz) {
      return new ClassWideMatcherEpsilonDescriptor<>(clazz);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ClassWideMatcherEpsilonDescriptor<?> that = (ClassWideMatcherEpsilonDescriptor<?>) o;
      return Objects.equals(clazz, that.clazz);
    }

    @Override
    public int hashCode() {
      return Objects.hash(clazz);
    }

  }

  /**
   * Use this inside:
   *
   * {@code public static TypeSafeMatcher<XYZ> xyzMatcher(XYZ expected) { ... }}
   *
   * ... when more than one epsilon is being used inside that matcher, i.e. there is more than 1 field being matched
   * that needs an epsilon in the match,
   * but each of those instances returns a different type. In that case, specifying the return type of the getter
   * (to which the epsilon will apply) will be unambiguous.
   * Example: BuyOrderTest#buyOrderMatcher
   *
   * One *could* use the GeneralEpsilonDescriptor (with a string key) instead of a GetterSpecificEpsilonDescriptor,
   * but the nice thing about GetterSpecificEpsilonDescriptor is that, by specifying the class object as a 2nd arg,
   * we reduce the risk of a typo. Of course, we could make a mistake and specify a wrong type.
   * But a string is easier to mess up.
   */
  public static class GetterSpecificMatcherEpsilonDescriptor<T, V> extends MatcherEpsilonDescriptor<T> {

    private final Class<V> getterReturnType;

    private GetterSpecificMatcherEpsilonDescriptor(Class<T> clazz, Class<V> getterReturnType) {
      super(clazz);
      this.getterReturnType = getterReturnType;
    }

    public static <T, V> GetterSpecificMatcherEpsilonDescriptor<T, V> eps(
        Class<T> clazz, Class<V> getterReturnType) {
      return new GetterSpecificMatcherEpsilonDescriptor<>(clazz, getterReturnType);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GetterSpecificMatcherEpsilonDescriptor<?, ?> that = (GetterSpecificMatcherEpsilonDescriptor<?, ?>) o;
      return Objects.equals(clazz, that.clazz)
          && Objects.equals(getterReturnType, that.getterReturnType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(clazz, getterReturnType);
    }

  }

  /**
   * Lets you specify an epsilon using some (ideally unique) combination of class plus string 'path'.
   *
   * <p> The high-level idea is that {@link MatcherEpsilons} represents some sort of map of (typically small)
   * numbers to use as epsilon in different calculations, keyed by some unique key. You can think of that as a file
   * path almost. Each user of an epsilon (e.g. the matcher that will compare a BuyOrder to another one)
   * requests that epsilon it cares about, using a key. In general, we can think of that key as a unique string.
   * We can refine that concept a bit and use a combination of a Class and a string key. </p>
   *
   * <p> This is particularly
   * useful in cases where we cannot uniquely identify a use case based on a class and a getter type.
   * For example, assume a class Foo who has two double fields, A and B, and say that we want to use different epsilons
   * in our tests when comparing those. We can't uniquely specify this by 'Foo.class' + 'Double.class', because both
   * A and B are double. And we can't easily refer to the getter method names, despite Java's reflection ability.
   * Therefore, we can do something like this:
   *
   * <pre>
   * epsilons(
   *   GeneralEpsilonDescriptor.eps(Foo.class, "forA"), 1e-6,
   *   GeneralEpsilonDescriptor.eps(Foo.class, "forB"), 1e-7);
   * </pre>
   *
   * Then, the matcher for Foo could look like this (note - this uses generics, so Javadoc won't render it properly):
   *
   * {@code
   * public static TypeSafeMatcher<Foo> fooMatcher() {
   *   return makeMatcher(expected,
   *      matchUsingAlmostEquals(v -> v.getA(), epsilons.get(Foo.class, "forA"),
   *      matchUsingAlmostEquals(v -> v.getB(), epsilons.get(Foo.class, "forB"));
   *      }
   *  }
   */
  public static class ClassPlusStringKeyMatcherEpsilonDescriptor<T> extends MatcherEpsilonDescriptor<T> {

    private final UniqueId<Epsilon> uniqueIdWithinMatcher;

    private ClassPlusStringKeyMatcherEpsilonDescriptor(Class<T> clazz, UniqueId<Epsilon> uniqueIdWithinMatcher) {
      super(clazz);
      this.uniqueIdWithinMatcher = uniqueIdWithinMatcher;
    }

    public static <T> ClassPlusStringKeyMatcherEpsilonDescriptor<T> eps(Class<T> clazz, UniqueId<Epsilon> uniqueIdWithinMatcher) {
      return new ClassPlusStringKeyMatcherEpsilonDescriptor<>(clazz, uniqueIdWithinMatcher);
    }

    public static UniqueId<Epsilon> epsilonId(String rawId) {
      return uniqueId(rawId);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ClassPlusStringKeyMatcherEpsilonDescriptor<?> that = (ClassPlusStringKeyMatcherEpsilonDescriptor<?>) o;
      return Objects.equals(clazz, that.clazz)
          // Note that UniqueId actually implements a non-trivial equals & hashCode.
          && Objects.equals(uniqueIdWithinMatcher, that.uniqueIdWithinMatcher);
    }

    @Override
    public int hashCode() {
      return Objects.hash(clazz, uniqueIdWithinMatcher);
    }

  }

}
