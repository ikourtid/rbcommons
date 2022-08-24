package com.rb.nonbiz.testutils;

import com.google.common.io.CharStreams;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.jsonapi.HasJsonApiDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import static com.rb.nonbiz.testutils.RBMockeries.imposterizingMockery;

/**
 * Most of your 'verb' classes should extend {@code RBTest<>}.
 * It forces you to create a separate method to generate the object being tested,
 * which enforces a consistent style.
 *
 * For *data* classes that have matchers (almost all of them should),
 * make them derive from {@code RBTestMatcher<T>} instead.
 *
 * There are a bunch of static constants which are useful when we want to use an object of a certain type,
 * but want to make it explicit in the test that we don't care about its value.
 * The alternative is to use one specific value (e.g. 100) and then have the reader of the test wonder
 * whether that specific value matters for making the test pass.
 *
 * Scroll to the end of this file, after all the 'public static final' constant definitions,
 * for a better idea of what RBTest gives you.
 */
public abstract class RBTest<T> extends RBCommonsTestConstants<T> {

  /**
   * 'imposterizing' means that this mockery object allows us to create not just fake objects that
   * implement an interface, but also fake objects that can stand in for a real (prod) java class.
   * The latter is useful because very few of our classes implement an interface.
   */
  protected Mockery mockery = imposterizingMockery();

  /**
   * This is for those rare cases where want to enforce that calls are made in a particular sequence.
   */
  protected Sequence makeSequence(String name) {
    return mockery.sequence(name);
  }

  protected String getFileAsString(String filename) throws IOException {
    return CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream(filename)));
  }

  /**
   * This ensures that the test fails if some expectations weren't correct.
   *
   * It is very, very easy to forget to do this in unit tests. This saves you from having to remember.
   */
  @After
  public void tearDown() {
    mockery.assertIsSatisfied();
  }

  /**
   * This is just a convention so that tests look similar.
   *
   * For unit tests of verb classes that don't have any Guice-injected objects in them,
   * you should just return a new object of that type.
   *
   * <pre>
   * {@code @Override}
   * {@code protected PortfolioValueBreakdownCalculator makeTestObject() {
   *   return new PortfolioValueBreakdownCalculator();
   * }}
   * </pre>
   *
   * For unit tests of verb classes that DO have Guice-injected objects in them,
   * you should name the variable 'testObject', and set its fields (which would normally be injected with values
   * by Guice) ALPHABETICALLY.
   *
   * In the following example, all fields of 'testObject' are themselves real objects. Ideally, we want to use
   * mocked objects, but sometimes it's easier to use a real object than to have to mock it.
   *
   * <pre>
   * {@code @Override}
   * {@code protected PortfolioValueCalculator makeTestObject() {
   *   PortfolioValueCalculator testObject = new PortfolioValueCalculator();
   *   testObject.portfolioValueBreakdownCalculator = new PortfolioValueBreakdownCalculator();
   *   testObject.ungroupedAggregatePositionsCalculator = new UngroupedAggregatePositionsCalculator();
   *   return testObject;
   * }}
   * </pre>
   *
   * The more general case will be when all (or most) Guice-injected fields of the object being tested
   * are mock objects. In the following example, we have 2 mock and 1 real object.
   *
   * Mock object definitions must appear at the top.
   * * Use a new line after the = sign (so each mock definition appears over 2 lines)
   * * Name the mock objects with a variable that matches the name of the class being mocked
   * * Keep the entries sorted alphabetically, so it's easier to track stuff.
   * makeTestObject should be the last method in the file. Example:
   *
   * <pre>
   * {@code
   * AllQuantityLevelTradingRestrictionsMerger quantityLevelTradingRestrictionsMerger =
   *     mockery.mock(AllQuantityLevelTradingRestrictionsMerger.class);
   * WashSaleRestrictionsGenerator washSaleRestrictionsGenerator =
   *     mockery.mock(WashSaleRestrictionsGenerator.class);
   * }
   * </pre>
   *
   * // ... tests go here. Finally...
   *
   * <pre>
   * {@code @Override}
   * {@code protected NonStrategySpecificRestrictionsGenerator makeTestObject() {
   *   NonStrategySpecificRestrictionsGenerator testObject = new NonStrategySpecificRestrictionsGenerator();
   *   testObject.booleanToQuantityRestrictionsConverter = new BooleanToQuantityRestrictionsConverter();
   *   testObject.quantityLevelTradingRestrictionsMerger = quantityLevelTradingRestrictionsMerger;
   *   testObject.washSaleRestrictionsGenerator = washSaleRestrictionsGenerator;
   *   return testObject;
   * }}
   * </pre>
   *
   * Note that one could also use makeRealObject(BooleanToQuantityRestrictionsConverter.class)
   * instead of new BooleanToQuantityRestrictionsConverter();
   * In fact, the latter may fail in cases where the former succeeds, e.g. if the class being created
   * has objects that must be injected by Guice. makeRealObject can handle that, but new XYZ() can't.
   * However, in tests, we often just instantiate with new, unless we *have* to use makeRealObject.
   */
  protected abstract T makeTestObject();

  @Test
  public void testGetJsonApiDocumentation() {
    T testObject;
    try {
      // There are very few tests currently (2 as of June 2022) where this is null, or throws an exception.
      // In normal cases, if makeTestObject is null, then the tests for that verb class will catch that.
      // So we don't have to worry about not catching an error here.
      testObject = makeTestObject();
      if (testObject == null) {
        return;
      }
    } catch (Exception e) {
      return;
    }

    // Only if testObject implements HasJsonApiDocumentation, run this extra check.
    if (HasJsonApiDocumentation.class.isAssignableFrom(testObject.getClass())) {
      // Just asserting that these methods do not throw; there's not much more we can do.
      JsonApiDocumentation doesNotThrow = ((HasJsonApiDocumentation) testObject).getJsonApiDocumentation();
      Optional<RBSet<JsonApiDocumentation>> alsoDoesNotThrow =
          ((HasJsonApiDocumentation) testObject).getAdditionalJsonApiDocumentation();

      // We could additionally check that the various getters don't return null or throw an exception.
      // However, that's the job of the JsonApiDocumentationBuilder.
    }
  }

}
