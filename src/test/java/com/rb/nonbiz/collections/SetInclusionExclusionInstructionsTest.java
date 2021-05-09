package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions.*;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

// This test class is not generic, but the publicly exposed matcher is.
public class SetInclusionExclusionInstructionsTest extends RBTestMatcher<SetInclusionExclusionInstructions<Character>> {

  @Test
  public void testConstructionSanityChecks() {
    SetInclusionExclusionInstructions<Character> doesNotThrow;
    doesNotThrow = includeEverything();
    doesNotThrow = excludeEverything();
    doesNotThrow = useRulesForEverything();

    doesNotThrow = useRulesForTheseExcludeRest(singletonRBSet('a'));
    assertIllegalArgumentException( () -> useRulesForTheseExcludeRest(emptyRBSet()));

    doesNotThrow = useRulesForTheseIncludeRest(singletonRBSet('a'));
    assertIllegalArgumentException( () -> useRulesForTheseIncludeRest(emptyRBSet()));

    doesNotThrow = includeTheseExcludeRest(singletonRBSet('a'));
    assertIllegalArgumentException( () -> includeTheseExcludeRest(emptyRBSet()));

    doesNotThrow = includeTheseUseRulesForRest(singletonRBSet('a'));
    assertIllegalArgumentException( () -> includeTheseUseRulesForRest(emptyRBSet()));

    doesNotThrow = excludeTheseIncludeRest(singletonRBSet('a'));
    assertIllegalArgumentException( () -> excludeTheseIncludeRest(emptyRBSet()));

    doesNotThrow = excludeTheseUseRulesForRest(singletonRBSet('a'));
    assertIllegalArgumentException( () -> excludeTheseUseRulesForRest(emptyRBSet()));

    doesNotThrow = includeTheseExcludeTheseUseRulesForRest(singletonRBSet('a'), singletonRBSet('b'));
    doesNotThrow = includeTheseExcludeTheseUseRulesForRest(rbSetOf('a', 'b'), rbSetOf('c', 'd'));
    assertIllegalArgumentException( () -> includeTheseExcludeTheseUseRulesForRest(singletonRBSet('a'), singletonRBSet('a')));
    assertIllegalArgumentException( () -> includeTheseExcludeTheseUseRulesForRest(rbSetOf('a', 'b'), rbSetOf('a', 'c')));
    assertIllegalArgumentException( () -> includeTheseExcludeTheseUseRulesForRest(emptyRBSet(), singletonRBSet('a')));
    assertIllegalArgumentException( () -> includeTheseExcludeTheseUseRulesForRest(singletonRBSet('a'), emptyRBSet()));
    assertIllegalArgumentException( () -> includeTheseExcludeTheseUseRulesForRest(emptyRBSet(), emptyRBSet()));
  }

  @Override
  public SetInclusionExclusionInstructions<Character> makeTrivialObject() {
    return includeEverything();
  }

  @Override
  public SetInclusionExclusionInstructions<Character> makeNontrivialObject() {
    return includeTheseExcludeTheseUseRulesForRest(rbSetOf('a', 'b'), rbSetOf('c', 'd'));
  }

  @Override
  public SetInclusionExclusionInstructions<Character> makeMatchingNontrivialObject() {
    return includeTheseExcludeTheseUseRulesForRest(rbSetOf('a', 'b'), rbSetOf('c', 'd'));
  }

  @Override
  protected boolean willMatch(SetInclusionExclusionInstructions<Character> expected,
                              SetInclusionExclusionInstructions<Character> actual) {
    return setInclusionExclusionInstructionsMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<SetInclusionExclusionInstructions<T>> setInclusionExclusionInstructionsMatcher(
      SetInclusionExclusionInstructions<T> expected) {
    return makeMatcher(expected, actual ->
        expected.getAlwaysExclude().equals(actual.getAlwaysExclude())
            && expected.getAlwaysInclude().equals(actual.getAlwaysInclude())
            && expected.getAlwaysUseRules().equals(actual.getAlwaysUseRules())
            && expected.getBehaviorForRest() == actual.getBehaviorForRest());
  }

}
