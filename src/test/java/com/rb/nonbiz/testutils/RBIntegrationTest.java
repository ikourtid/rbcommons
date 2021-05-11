package com.rb.nonbiz.testutils;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.rb.biz.guice.RBClockModifier;

import java.time.LocalDateTime;

import static com.rb.biz.guice.modules.RBCommonsTestModule.rbCommonsIntegrationTestModule;

/**
 * Integration tests (i.e. higher level than unit tests) of verb class FooBarBazer
 * should inherit from {@code RBIntegrationTest<FooBarBazer>}
 *
 * All this does is enforce convention by having you override getClassBeingTested; it should look like this:
 *
 * <pre>
 * {@code @Override}
 * {@code
 * protected Class<FooBarBazer> getClassBeingTested() {
 *   return FooBarBazer.class;
 * }
 * }</pre>
 *
 * By the way, if you are wondering why we even need this method, keep in mind that Java does something called
 * 'type erasure', which basically means that type information for generics is NOT available at runtime,
 * i.e. once the code has been compiled.
 *
 * Then you can just use makeRealObject() in your test code to get a usable object (with all Guice injections done
 * for you) of the type you are doing an integration test for.
 *
 * If you want to instantiate a real object that has 0 (or some manageable number of) verb sub-objects in it,
 * OR the number of (recursive transitive closure of) injections,
 * you could use RBTest instead of RBIntegrationTest, and just return a real object.
 * That alternative will be slightly faster, since RBIntegrationTest uses the Guice injector.
 * Of course, this will only matter once we have tons and tons of tests.
 *
 * An integration test for verb class FooBarBazer should be named FooBarBazerIntegrationTest.java,
 * in order to distinguish it from the unit test FooBarBazerTest.java.
 *
 * @see RBTest
 * @see RBTestPlusIntegration
 */
public abstract class RBIntegrationTest<T> extends RBCommonsTestConstants<T> {

  private T realObject;

  protected abstract Class<T> getClassBeingTested();

  protected T makeRealObject() {
    return makeRealObject(DUMMY_TIME);
  }

  protected T makeRealObject(LocalDateTime now) {
    if (realObject == null) {
      realObject = makeRealObject(getClassBeingTested(), now);
    }
    return realObject;
  }

  /**
   * Sometimes an {@code RBIntegrationTest<T1>} will also need real objects of some other type T2.
   */
  public static <T> T makeRealObject(Class<T> toInstantiate, LocalDateTime now, com.google.inject.Module module) {
    try {
      Injector injector = Guice.createInjector(Stage.DEVELOPMENT, module);
      T realObject = toInstantiate.getConstructor().newInstance();
      injector.getProvider(RBClockModifier.class).get().overwriteCurrentTime(now);
      injector.injectMembers(realObject);
      return realObject;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T makeRealObject(Class<T> toInstantiate, LocalDateTime now) {
    return makeRealObject(toInstantiate, now, rbCommonsIntegrationTestModule());
  }

  public static <T> T makeRealObject(Class<T> toInstantiate) {
    return makeRealObject(toInstantiate, DUMMY_TIME);
  }

}
