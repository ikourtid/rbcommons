package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.CollectionJsonApiPropertyDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.CollectionJsonApiPropertyDescriptor.collectionJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaGenericJsonApiPropertyDescriptor.javaGenericJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptorTest.dataClassJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class CollectionJsonApiPropertyDescriptorTest extends RBTestMatcher<CollectionJsonApiPropertyDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        UniqueId.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> collectionJsonApiPropertyDescriptor(
                simpleClassJsonApiPropertyDescriptor(clazz))));
  }

  @Override
  public CollectionJsonApiPropertyDescriptor makeTrivialObject() {
    return collectionJsonApiPropertyDescriptor(simpleClassJsonApiPropertyDescriptor(Money.class));
  }

  @Override
  public CollectionJsonApiPropertyDescriptor makeNontrivialObject() {
    return collectionJsonApiPropertyDescriptor(javaGenericJsonApiPropertyDescriptor(
        ClosedRange.class, simpleClassJsonApiPropertyDescriptor(Double.class)));
  }

  @Override
  public CollectionJsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    return collectionJsonApiPropertyDescriptor(javaGenericJsonApiPropertyDescriptor(
        ClosedRange.class, simpleClassJsonApiPropertyDescriptor(Double.class)));
  }

  @Override
  protected boolean willMatch(CollectionJsonApiPropertyDescriptor expected, CollectionJsonApiPropertyDescriptor actual) {
    return collectionJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<CollectionJsonApiPropertyDescriptor> collectionJsonApiPropertyDescriptorMatcher(
      CollectionJsonApiPropertyDescriptor expected) {
    return makeMatcher(expected,
        match(v -> v.getCollectionValueClassDescriptor(), f -> dataClassJsonApiPropertyDescriptorMatcher(f)));
  }

}
