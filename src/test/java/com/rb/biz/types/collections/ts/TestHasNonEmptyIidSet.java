package com.rb.biz.types.collections.ts;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.collections.HasIidSet;
import com.rb.nonbiz.collections.HasNonEmptyIidSet;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.util.RBPreconditions;
import org.hamcrest.TypeSafeMatcher;

import java.time.LocalDate;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class TestHasNonEmptyIidSet implements HasNonEmptyIidSet {

  private final IidSet iidSet;
  private final String string;

  /**
   * We intentionally do not create a static constructor like we do with every data class,
   * in order to make it even more noticeable that this is different and is not a prod class.
   */
  public TestHasNonEmptyIidSet(IidSet iidSet, String string) {
    RBPreconditions.checkArgument(
        !iidSet.isEmpty(),
        "IidSet cannot be empty for a HasNonEmptyIidSet");
    this.iidSet = iidSet;
    this.string = string;
  }

  @Override
  public IidSet getIidSet() {
    return iidSet;
  }

  public String getString() {
    return string;
  }

  public static TypeSafeMatcher<TestHasNonEmptyIidSet> testHasNonEmptyIidSetMatcher(TestHasNonEmptyIidSet expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getIidSet()),
        matchUsingEquals(v -> v.string));
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    // Normally, we don't bother implementing toString for test-only classes. However, in this case,
    // this implements HasNonEmptyIidSet, so it has to have an implementation.
    return toString();
  }

}
