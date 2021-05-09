package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.Counter.CounterBuilder;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.CachingSupplierTest.TestCachingSupplierKey;
import org.junit.Test;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.rb.nonbiz.collections.Counter.CounterBuilder.counterBuilder;
import static com.rb.nonbiz.collections.CounterTest.counterFromMap;
import static com.rb.nonbiz.collections.CounterTest.counterMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class CachingSupplierTest extends RBTest<CachingSupplier<TestCachingSupplierKey, String>> {

  /**
   * It is hard to test synchronization, but at least we can test the basic functionality, and also that
   * we don't 'load' items more than once, once they are in the cache.
   *
   * We create our own thin subclass to test CachingSupplier, since it is an abstract class.
   */
  @Test
  public void simpleTestWithoutSynchronization() {
    CachingSupplier<TestCachingSupplierKey, String> cachingSupplier = makeTestObject();

    TestCachingSupplierKey keyA0 = new TestCachingSupplierKey('a', 0);
    TestCachingSupplierKey keyA1 = new TestCachingSupplierKey('a', 1);
    TestCachingSupplierKey keyB0 = new TestCachingSupplierKey('b', 0);
    TestCachingSupplierKey keyB1 = new TestCachingSupplierKey('b', 1);

    CounterBuilder<String> counterBuilder = counterBuilder();
    // In the real world, this 'loader' would actually perform some long-ish operation (e.g. load from disk).
    BiConsumer<String, TestCachingSupplierKey> assertCachingOfValue = (expectedResult, key) -> {
      assertEquals(
          expectedResult,
          cachingSupplier.getFromCache(key, () -> {
            String value = Strings.format("%s_%s", key.charComponent, key.intComponent);
            // We add this side effect so we can make sure things don't get loaded more than once.
            counterBuilder.increment(value);
            return value;
          }));
    };
    Consumer<RBMap<String, Integer>> assertCounter = counterAsMap ->
        assertThat(
            counterBuilder.build(),
            counterMatcher(counterFromMap(counterAsMap)));

    // In the beginning, nothing has been loaded, so the counter should be empty
    assertCounter.accept(emptyRBMap());

    // load one item; side-effect of loading should reflect on the counter
    assertCachingOfValue.accept("a_0", keyA0);
    assertCounter.accept(singletonRBMap(
        "a_0", 1));

    // load another item
    assertCachingOfValue.accept("b_0", keyB0);
    assertCounter.accept(rbMapOf(
        "a_0", 1,
        "b_0", 1));

    // after this, all 4 items will have been loaded, but only once.
    assertCachingOfValue.accept("a_1", keyA1);
    assertCachingOfValue.accept("b_1", keyB1);
    RBMap<String, Integer> everythingLoadedOnce = rbMapOf(
        "a_0", 1,
        "a_1", 1,
        "b_0", 1,
        "b_1", 1);
    assertCounter.accept(everythingLoadedOnce);

    // retrieve all 4 items once more from the cache. We should 'load' them, and hence the counter shouldn't be updated.
    assertCachingOfValue.accept("a_0", keyA0);
    assertCachingOfValue.accept("b_0", keyB0);
    assertCachingOfValue.accept("a_1", keyA1);
    assertCachingOfValue.accept("b_1", keyB1);
    assertCounter.accept(everythingLoadedOnce);

    // once more... why not.
    assertCachingOfValue.accept("a_0", keyA0);
    assertCachingOfValue.accept("b_0", keyB0);
    assertCachingOfValue.accept("a_1", keyA1);
    assertCachingOfValue.accept("b_1", keyB1);
    assertCounter.accept(everythingLoadedOnce);
  }

  @Override
  protected CachingSupplier<TestCachingSupplierKey, String> makeTestObject() {
    int initialCapacity = 1; // smaller than expected, intentionally
    return new CachingSupplier<>(initialCapacity);
  }


  static class TestCachingSupplierKey implements CachingSupplierKey<String> {

    private final char charComponent;
    private final int intComponent;

    public TestCachingSupplierKey(char charComponent, int intComponent) {
      this.charComponent = charComponent;
      this.intComponent = intComponent;
    }

    // IDE-generated
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestCachingSupplierKey that = (TestCachingSupplierKey) o;
      return charComponent == that.charComponent
          && intComponent == that.intComponent;
    }

    // IDE-generated
    @Override
    public int hashCode() {
      return Objects.hash(charComponent, intComponent);
    }

  }

}
