package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSets.noSharedItems;
import static com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions.BehaviorForRest.EXCLUDE;
import static com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions.BehaviorForRest.INCLUDE;
import static com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions.BehaviorForRest.USE_RULES;

/**
 * Tells us if a specific item should be included or excluded as per the SetInclusionExclusionInstructions.
 * Optional.of(true)  {@code =>}  include
 * Optional.of(false) {@code =>}  exclude
 * Optional.empty()   {@code =>}  there isn't enough information to decide.
 */
public class SetInclusionExclusionPredicateEvaluator {

  public <T> Optional<Boolean> mustBeIncluded(T item,
                                              SetInclusionExclusionInstructions<T> instructions) {
    if (instructions.getAlwaysInclude().contains(item)) {
      return Optional.of(true);
    }
    if (instructions.getAlwaysExclude().contains(item)) {
      return Optional.of(false);
    }
    if (instructions.getAlwaysUseRules().contains(item)) {
      return Optional.empty();
    }
    switch (instructions.getBehaviorForRest()) {
      case INCLUDE:
        return Optional.of(true);
      case EXCLUDE:
        return Optional.of(false);
      case USE_RULES:
        return Optional.empty();
      default:
        throw new IllegalArgumentException(
            Strings.format("enum value %s not handled", instructions.getBehaviorForRest()));
    }
  }

  /**
   * Often (e.g. with selecting which instruments will participate in a backtest)
   * we need to have rules such as
   * 'include A, B, C; exclude everything else
   * 'exclude D, E, F; include everything else
   * 'include A, B, C; exclude D, E, F; for everything else, use your own rules' (most general case)
   *
   * This class represents all these cases.
   * It is intentionally not a top-level class. This way, I don't need to expose its data.
   */
  public static class SetInclusionExclusionInstructions<T> {

    enum BehaviorForRest {
      INCLUDE,
      EXCLUDE,
      USE_RULES
    };

    private final RBSet<T> alwaysInclude;
    private final RBSet<T> alwaysExclude;
    private final RBSet<T> alwaysUseRules;
    private final BehaviorForRest behaviorForRest;

    private SetInclusionExclusionInstructions(
        RBSet<T> alwaysInclude, RBSet<T> alwaysExclude, RBSet<T> alwaysUseRules, BehaviorForRest behaviorForRest) {
      this.alwaysInclude = alwaysInclude;
      this.alwaysExclude = alwaysExclude;
      this.alwaysUseRules = alwaysUseRules;
      this.behaviorForRest = behaviorForRest;
    }

    public static <T> SetInclusionExclusionInstructions<T> includeEverything() {
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), emptyRBSet(), emptyRBSet(), INCLUDE);
    }

    public static <T> SetInclusionExclusionInstructions<T> excludeEverything() {
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), emptyRBSet(), emptyRBSet(), EXCLUDE);
    }

    public static <T> SetInclusionExclusionInstructions<T> useRulesForEverything() {
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), emptyRBSet(), emptyRBSet(), USE_RULES);
    }

    public static <T> SetInclusionExclusionInstructions<T> useRulesForTheseExcludeRest(RBSet<T> alwaysUseRules) {
      RBPreconditions.checkArgument(
          !alwaysUseRules.isEmpty(),
          "If there's no 'always use rules' items, use the static constructor #useRulesForEverything");
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), emptyRBSet(), alwaysUseRules, EXCLUDE);
    }

    public static <T> SetInclusionExclusionInstructions<T> useRulesForTheseIncludeRest(RBSet<T> alwaysUseRules) {
      RBPreconditions.checkArgument(
          !alwaysUseRules.isEmpty(),
          "If there's no 'always use rules' items, use the static constructor #useRulesForEverything");
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), emptyRBSet(), alwaysUseRules, INCLUDE);
    }

    public static <T> SetInclusionExclusionInstructions<T> includeTheseExcludeRest(RBSet<T> alwaysInclude) {
      RBPreconditions.checkArgument(
          !alwaysInclude.isEmpty(),
          "If there's no inclusion or exclusion list, use the static constructor #useRulesForEverything");
      return new SetInclusionExclusionInstructions<T>(alwaysInclude, emptyRBSet(), emptyRBSet(), EXCLUDE);
    }

    public static <T> SetInclusionExclusionInstructions<T> includeTheseUseRulesForRest(RBSet<T> alwaysInclude) {
      RBPreconditions.checkArgument(
          !alwaysInclude.isEmpty(),
          "If there's no inclusion or exclusion list, use the static constructor #useRulesForEverything");
      return new SetInclusionExclusionInstructions<T>(alwaysInclude, emptyRBSet(), emptyRBSet(), USE_RULES);
    }

    public static <T> SetInclusionExclusionInstructions<T> excludeTheseIncludeRest(RBSet<T> alwaysExclude) {
      RBPreconditions.checkArgument(
          !alwaysExclude.isEmpty(),
          "If there's no exclusion list, use the static constructor #includeEverything");
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), alwaysExclude, emptyRBSet(), INCLUDE);
    }

    public static <T> SetInclusionExclusionInstructions<T> excludeTheseUseRulesForRest(RBSet<T> alwaysExclude) {
      RBPreconditions.checkArgument(
          !alwaysExclude.isEmpty(),
          "If there's no exclusion list, use the static constructor #useRulesForEverything");
      return new SetInclusionExclusionInstructions<T>(emptyRBSet(), alwaysExclude, emptyRBSet(), USE_RULES);
    }

    public static <T> SetInclusionExclusionInstructions<T> includeTheseExcludeTheseUseRulesForRest(
        RBSet<T> alwaysInclude, RBSet<T> alwaysExclude) {
      RBPreconditions.checkArgument(
          !alwaysInclude.isEmpty() && !alwaysExclude.isEmpty(),
          "If the inclusion or exclusion lists are empty, use a different static constructor");
      RBPreconditions.checkArgument(
          noSharedItems(alwaysInclude, alwaysExclude),
          "Some stocks are both in the alwaysInclude and the alwaysExclude lists; lists are %s and %s",
          alwaysInclude, alwaysExclude);
      return new SetInclusionExclusionInstructions<T>(alwaysInclude, alwaysExclude, emptyRBSet(), USE_RULES);
    }

    /** You should access this data class via a SetInclusionExclusionApplier; this is here for the test matcher. */
    public RBSet<T> getAlwaysInclude() {
      return alwaysInclude;
    }

    /** You should access this data class via a SetInclusionExclusionApplier; this is here for the test matcher. */
    RBSet<T> getAlwaysExclude() {
      return alwaysExclude;
    }

    /** You should access this data class via a SetInclusionExclusionApplier; this is here for the test matcher. */
    RBSet<T> getAlwaysUseRules() {
      return alwaysUseRules;
    }

    /** You should access this data class via a SetInclusionExclusionApplier; this is here for the test matcher. */
    public BehaviorForRest getBehaviorForRest() {
      return behaviorForRest;
    }

  }

}
