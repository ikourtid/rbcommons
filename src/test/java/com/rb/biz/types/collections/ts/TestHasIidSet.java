package com.rb.biz.types.collections.ts;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.collections.HasIidSet;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;

import java.time.LocalDate;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class TestHasIidSet implements HasIidSet {

  private final IidSet iidSet;
  private final String string;

  /**
   * We intentionally do not create a static constructor like we do with every data class,
   * in order to make it even more noticeable that this is different and is not a prod class.
   */
  public TestHasIidSet(IidSet iidSet, String string) {
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

  public static TypeSafeMatcher<TestHasIidSet> testHasIidSetMatcher(TestHasIidSet expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getIidSet()),
        matchUsingEquals(v -> v.string));
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    // Normally, we don't bother implementing toString for test-only classes. However, in this case,
    // this implements HasIidSet, so it has to have an implementation.
    return toString();
  }

}
