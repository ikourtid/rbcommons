package com.rb.nonbiz.functional;

import com.rb.nonbiz.text.Strings;

import java.util.Iterator;
import java.util.function.BinaryOperator;

/**
 * An addition function (which, in general, may apply to non-scalars, such as TotalByPortfolioType),
 * a subtraction function, and an identity element.
 *
 * Note that T can be a datatype such that subtraction is not always defined, e.g. Money, where it has to be positive.
 * We'll just have to
 * make sure we only use this functionality in a situation that will not cause an exception - e.g. trying to subtract
 * money(10).subtract(money(11)).
 */
public class AdditionSubtractionAndIdentityElement<T> {

  private final T identityElement;
  private final BinaryOperator<T> adder;
  private final BinaryOperator<T> subtracter;

  private AdditionSubtractionAndIdentityElement(T identityElement, BinaryOperator<T> adder, BinaryOperator<T> subtracter) {
    this.identityElement = identityElement;
    this.adder = adder;
    this.subtracter = subtracter;
  }

  public static <T> AdditionSubtractionAndIdentityElement<T> additionSubtractionAndIdentityElement(
      T identityElement, BinaryOperator<T> adder, BinaryOperator<T> subtracter) {
    T ignored;
    // This isn't a RBPreconditions.checkForSomething, but at least by trying out these operations on the identity
    // element, we can confirm there is no exception.
    // Ideally, we'd check that the return values are the same as the identity element (0 + 0 = 0 and 0 - 0 = 0),
    // but T is not guaranteed to implement #equals, so we can't check for that in the general sense.
    ignored = adder.apply(identityElement, identityElement);
    ignored = subtracter.apply(identityElement, identityElement);
    return new AdditionSubtractionAndIdentityElement<>(identityElement, adder, subtracter);
  }

  // Normally you shouldn't have to use this; Java streams map/reduce should suffice.
  public T aggregate(Iterator<T> iter) {
    T sum = identityElement;
    while (iter.hasNext()) {
      sum = adder.apply(sum, iter.next());
    };
    return sum;
  }

  public T getIdentityElement() {
    return identityElement;
  }

  public BinaryOperator<T> getAdder() {
    return adder;
  }

  public T add(T value1, T value2) {
    return adder.apply(value1, value2);
  }

  public BinaryOperator<T> getSubtracter() {
    return subtracter;
  }

  public T subtract(T value1, T value2) {
    return subtracter.apply(value1, value2);
  }

  @Override
  public String toString() {
    return Strings.format("[ASAIE identityElem= %s ASAIE]", identityElement);
  }

}
