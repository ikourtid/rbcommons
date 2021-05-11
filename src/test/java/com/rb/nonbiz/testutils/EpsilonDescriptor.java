package com.rb.nonbiz.testutils;

import java.util.Objects;

public abstract class EpsilonDescriptor<T> {

  protected final Class<T> clazz;

  protected EpsilonDescriptor(Class<T> clazz) {
    this.clazz = clazz;
  }

  /**
   * Use this inside:
   *
   * {@code public static TypeSafeMatcher<XYZ> xyzMatcher(XYZ expected) { ... }}
   *
   * ... when you don't care about using different epsilons inside that matcher,
   * which means that specifying the class XYZ only suffices for uniquely identifying which epsilon to use.
   */
  public static class ClassWideEpsilonDescriptor<T> extends EpsilonDescriptor<T> {

    private ClassWideEpsilonDescriptor(Class<T> clazz) {
      super(clazz);
    }

    public static <T> ClassWideEpsilonDescriptor<T> eps(Class<T> clazz) {
      return new ClassWideEpsilonDescriptor<>(clazz);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ClassWideEpsilonDescriptor<?> that = (ClassWideEpsilonDescriptor<?>) o;
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
  public static class GetterSpecificEpsilonDescriptor<T, V> extends EpsilonDescriptor<T> {

    private final Class<V> getterReturnType;

    private GetterSpecificEpsilonDescriptor(Class<T> clazz, Class<V> getterReturnType) {
      super(clazz);
      this.getterReturnType = getterReturnType;
    }

    public static <T, V> GetterSpecificEpsilonDescriptor<T, V> eps(
        Class<T> clazz, Class<V> getterReturnType) {
      return new GetterSpecificEpsilonDescriptor<>(clazz, getterReturnType);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GetterSpecificEpsilonDescriptor<?, ?> that = (GetterSpecificEpsilonDescriptor<?, ?>) o;
      return Objects.equals(clazz, that.clazz) &&
          Objects.equals(getterReturnType, that.getterReturnType);
    }

    @Override
    public int hashCode() {

      return Objects.hash(clazz, getterReturnType);
    }
  }

  public static class GeneralEpsilonDescriptor<T> extends EpsilonDescriptor<T> {

    private final String uniqueIdWithinMatcher;

    private GeneralEpsilonDescriptor(Class<T> clazz, String uniqueIdWithinMatcher) {
      super(clazz);
      this.uniqueIdWithinMatcher = uniqueIdWithinMatcher;
    }

    public static <T> GeneralEpsilonDescriptor<T> eps(Class<T> clazz, String uniqueIdWithinMatcher) {
      return new GeneralEpsilonDescriptor<>(clazz, uniqueIdWithinMatcher);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GeneralEpsilonDescriptor<?> that = (GeneralEpsilonDescriptor<?>) o;
      return Objects.equals(uniqueIdWithinMatcher, that.uniqueIdWithinMatcher);
    }

    @Override
    public int hashCode() {
      return Objects.hash(clazz, uniqueIdWithinMatcher);
    }

  }

}
