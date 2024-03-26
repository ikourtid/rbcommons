package com.rb.nonbiz.testutils;

import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;

import static org.jmock.AbstractExpectations.returnValue;

/**
 * Various static utility methods related to mocking for purposes of testing.
 */
public class RBMockeries {

  public static Mockery imposterizingMockery() {
    Mockery mockery = new Mockery();
    mockery.setImposteriser(ClassImposteriser.INSTANCE);
    return mockery;
  }

  public static <T> Action returnEmptyList() {
    return returnValue(Collections.<T>emptyList());
  }

  public static <T> Action returnSingletonList(T value) {
    return returnValue(Collections.<T>singletonList(value));
  }

  public static <T> Action returnEmptySet() {
    return returnValue(Collections.<T>emptySet());
  }

  public static <T> Action returnSingletonSet(T value) {
    return returnValue(Collections.<T>singleton(value));
  }

}
