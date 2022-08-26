package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.RBSets.setUnionOfFirstAndRest;

/**
 * Helps us check whether a string is one of a set of valid values, but we don't care about capitalization.
 *
 * Internally, we do a trick where we actually only store uppercase strings, and when asked whether a string matches
 * the filter, we check whether the uppercase string matches. We could have chosen lowercase just as well; we just
 * need to be consistent.
 *
 * This is the reason why, unlike RBSet, we do not expose the contents of this class as a set. That's also why we call
 * this a 'filter' instead of a 'set' - to imply that we don't care about its contents, but rather only about whether
 * a given string matches the filter or not.
 */
public class CaseInsensitiveStringFilter {

  private final RBSet<String> capitalizedStrings;

  private CaseInsensitiveStringFilter(RBSet<String> capitalizedStrings) {
    this.capitalizedStrings = capitalizedStrings;
  }

  public static CaseInsensitiveStringFilter caseInsensitiveStringFilter(RBSet<String> strings) {
    // The following is simpler, but it will not catch cases where a string appears with multiple capitalizations
    // within the set - e.g. "xyz" and "xYz".
    // return new CaseInsensitiveStringFilter(strings.transform(v -> v.toUpperCase()));
    return new CaseInsensitiveStringFilter(strings.transform(v -> v.toUpperCase()));
  }

  public static CaseInsensitiveStringFilter emptyCaseInsensitiveStringFilter() {
    return caseInsensitiveStringFilter(emptyRBSet());
  }

  public static CaseInsensitiveStringFilter singletonCaseInsensitiveStringFilter(String singleValue) {
    return caseInsensitiveStringFilter(singletonRBSet(singleValue));
  }

  public static CaseInsensitiveStringFilter caseInsensitiveStringFilter(String first, String ... rest) {
    return caseInsensitiveStringFilter(setUnionOfFirstAndRest(first, rest));
  }

  public boolean containsIgnoringCase(String string) {
    return capitalizedStrings.contains(string.toUpperCase());
  }

  // Do not use this; it is here for the test matcher and for the JSON converter
  public RBSet<String> getCapitalizedStrings() {
    return capitalizedStrings;
  }

  @Override
  public String toString() {
    return Strings.format("[CISF %s CISF]", capitalizedStrings);
  }

}
