package com.rb.nonbiz.types;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Pointer.initializedPointer;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PointerTest extends RBTestMatcher<Pointer<Double>> {

  @Test
  public void happyPath_uninitializedPointer() {
    Pointer<String> pointer = uninitializedPointer();
    assertFalse(pointer.getOptional().isPresent());
    assertIllegalArgumentException( () -> pointer.getOrThrow());
    pointer.set("xyz");
    assertOptionalEquals("xyz", pointer.getOptional());
    assertEquals("xyz", pointer.getOrThrow());
  }

  @Test
  public void testSetAssumingUninitialized() {
    Pointer<String> pointer = uninitializedPointer();
    pointer.setAssumingUninitialized("a");
    assertEquals("a", pointer.getOrThrow());
    assertIllegalArgumentException( () -> pointer.setAssumingUninitialized("a"));
    assertEquals("a", pointer.getOrThrow());
    assertIllegalArgumentException( () -> pointer.setAssumingUninitialized("b"));
    assertEquals("a", pointer.getOrThrow());
  }

  @Test
  public void testSetAssumingInitialized() {
    Pointer<String> pointer = uninitializedPointer();
    assertIllegalArgumentException( () -> pointer.setAssumingInitialized("a"));
    assertFalse(pointer.isInitialized());
    pointer.setAssumingUninitialized("a");
    assertEquals("a", pointer.getOrThrow());
    pointer.setAssumingUninitialized("b");
    assertEquals("b", pointer.getOrThrow());
    pointer.setAssumingUninitialized("c");
    assertEquals("c", pointer.getOrThrow());
  }

  @Test
  public void testInitializeOrModifyExisting() {
    Pointer<Double> pointer = uninitializedPointer();
    assertFalse(pointer.isInitialized());
    pointer.initializeOrModifyExisting(1.1, Double::sum);
    assertTrue(pointer.isInitialized());
    assertEquals(1.1, pointer.getOrThrow(), 1e-8);

    pointer.initializeOrModifyExisting(3.3, Double::sum);
    assertTrue(pointer.isInitialized());
    assertEquals(doubleExplained(4.4, 1.1 + 3.3), pointer.getOrThrow(), 1e-8);

    pointer.initializeOrModifyExisting(7.7, Double::sum);
    assertTrue(pointer.isInitialized());
    assertEquals(doubleExplained(12.1, 4.4 + 7.7), pointer.getOrThrow(), 1e-8);
  }

  @Test
  public void testIfInitialized() {
    Pointer<Double> externalValue = uninitializedPointer();

    Pointer.<Double>uninitializedPointer().ifInitialized(v -> externalValue.set(v));
    assertFalse(externalValue.isInitialized());
    initializedPointer(1.1).ifInitialized(v -> externalValue.set(v));
    assertTrue(externalValue.isInitialized());
    assertEquals(1.1, externalValue.getOrThrow(), 1e-8);
  }

  @Test
  public void testModifyExisting() {
    assertIllegalArgumentException( () -> Pointer.<String>uninitializedPointer().modifyExisting("a", String::concat));
    assertThat(
        initializedPointer("a")
            .modifyExisting("b", String::concat)
            .modifyExisting("c", String::concat),
        pointerMatcher(
            initializedPointer("abc"), v -> typeSafeEqualTo(v)));
  }

  @Override
  public Pointer<Double> makeTrivialObject() {
    return uninitializedPointer();
  }

  @Override
  public Pointer<Double> makeNontrivialObject() {
    return initializedPointer(1.1);
  }

  @Override
  public Pointer<Double> makeMatchingNontrivialObject() {
    double e = 1e-9;
    return initializedPointer(1.1 + e);
  }

  @Override
  protected boolean willMatch(Pointer<Double> expected, Pointer<Double> actual) {
    return pointerMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <T> TypeSafeMatcher<Pointer<T>> pointerMatcher(Pointer<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        matchOptional(v -> v.getOptional(), matcherGenerator));
  }

}
