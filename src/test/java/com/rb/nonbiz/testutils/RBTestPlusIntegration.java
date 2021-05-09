package com.rb.nonbiz.testutils;

import java.time.LocalDateTime;

/**
 * Use this instead of RBTest when you additionally want to perform an integration test
 * using a 'real' (i.e. non-mock) object.
 *
 * Upsides are:
 * - more testing coverage
 *
 * Downsides are:
 * - it forces you to make the test data realistic - whereas, when you mock your injected classes, you can pretend
 *   that the injected class produces some particular input based on some particular output, even if the real class
 *   wouldn't produce that input.
 * - it slows things down, as injector.injectMembers is not instant
 *
 * In general, this is most useful in 'happy path' types of test methods, which uses most/all of your injected
 * objects. Otherwise, if you are just checking that e.g. some simple combination of inputs is invalid and causes
 * your tested method to throw, you don't really need to use real objects for that - mocks will do.
 *
 * Note that, for performance reasons, each {@code @Test} method in your test will be receiving the same copy
 * of the {@code <T>} object each time it calls makeRealObject() - which should be OK as long as your 'verb' classes are stateless,
 * and you're supposed to be conforming to that anyway. Of course, different {@code @Test} methods will each get a copy,
 * because JUnit creates a new instance of the unit test class every time it executes a single {@code @Test} method.
 */
public abstract class RBTestPlusIntegration<T> extends RBTest<T> {

  private T realObject;

  protected abstract Class<T> getClassBeingTested();

  protected T makeRealObject() {
    return makeRealObject(DUMMY_TIME);
  }

  protected T makeRealObject(LocalDateTime now) {
    if (realObject == null) {
      realObject = RBIntegrationTest.makeRealObject(getClassBeingTested(), now);
    }
    return realObject;
  }

}