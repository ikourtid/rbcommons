package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.CollectionJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.CollectionJsonApiDescriptor.collectionJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class CollectionJsonApiDescriptorTest extends RBTestMatcher<CollectionJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class,
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        UniqueId.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> collectionJsonApiDescriptor(clazz)));
  }

  @Override
  public CollectionJsonApiDescriptor makeTrivialObject() {
    return collectionJsonApiDescriptor(Money.class);
  }

  @Override
  public CollectionJsonApiDescriptor makeNontrivialObject() {
    return collectionJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  public CollectionJsonApiDescriptor makeMatchingNontrivialObject() {
    return collectionJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  protected boolean willMatch(CollectionJsonApiDescriptor expected, CollectionJsonApiDescriptor actual) {
    return collectionJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<CollectionJsonApiDescriptor> collectionJsonApiDescriptorMatcher(
      CollectionJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getCollectionValueClass()));
  }

}
