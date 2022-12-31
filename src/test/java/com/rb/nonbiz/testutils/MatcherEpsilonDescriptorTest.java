package com.rb.nonbiz.testutils;

import org.junit.Test;

import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassWideMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassPlusStringKeyMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GetterSpecificMatcherEpsilonDescriptor.eps;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatcherEpsilonDescriptorTest {

  @Test
  public void testEquals() {
    class Class1 {};
    class Class1A {};
    class Class1B {};
    class Class2 {};

    assertEquals(
        "ClassWideEpsilonDescriptor objects must be equal",
        eps(Class1.class),
        eps(Class1.class));
    assertEquals(
        "GetterSpecificEpsilonDescriptor objects must be equal",
        eps(Class1.class, Class1A.class),
        eps(Class1.class, Class1A.class));
    assertEquals(
        "Case of GeneralEpsilonDescriptor objects must be equal",
        eps(Class1.class, "foo"),
        eps(Class1.class, "foo"));

    assertNotEquals(
        "ClassWideEpsilonDescriptor objects must not be equal",
        eps(Class1.class),
        eps(Class2.class));
    assertNotEquals(
        "GetterSpecificEpsilonDescriptor objects must not be equal",
        eps(Class1.class, Class1A.class),
        eps(Class1.class, Class1B.class));
    assertNotEquals(
        "GetterSpecificEpsilonDescriptor objects must not be equal",
        eps(Class1.class, Class1A.class),
        eps(Class2.class, Class1A.class));
    assertNotEquals(
        "GeneralEpsilonDescriptor objects must not be equal",
        eps(Class1.class, "foo"),
        eps(Class1.class, "bar"));
    assertNotEquals(
        "GeneralEpsilonDescriptor objects must not be equal",
        eps(Class1.class, "foo"),
        eps(Class2.class, "foo"));

    assertNotEquals(
        "Different types of EpsilonDescriptor must not be equal",
        eps(Class1.class),
        eps(Class1.class, Class1A.class));
    assertNotEquals(
        "Different types of EpsilonDescriptor must not be equal",
        eps(Class1.class),
        eps(Class1.class, "foo"));
  }

}
