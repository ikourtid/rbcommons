package com.rb.nonbiz.json;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * Hold information about required and optional top-level properties for a JSON object.
 * The purpose is to check JSON objects being converted via our JsonApiConverters.
 * In particular, we want to know if an optional property is misspelled, which could allow
 * a silent failure to propagate.
 *
 * E.g. if a JSON object contained optional entries:
 * "orderNotionalLimits" = {
 *   "min": 100,
 *   "mxa": 1000     // TYPO: "mxa" instead of "max"
 * }
 * Because the max order notional size is optional, a typo would result in no maximum,
 * e.g. unlimited order size.
 *
 * We could extend validation to checking valid data ranges (e.g. UnitFractions must be between 0.0 and 1.0).
 * However, this wouldn't add much value since our constructors already enforce these limits.
 */
public class JsonValidationInstructions {

  private final RBSet<String> requiredProperties;
  private final RBSet<String> optionalProperties;

  private JsonValidationInstructions(
      RBSet<String> requiredProperties, RBSet<String> optionalProperties) {
    this.requiredProperties = requiredProperties;
    this.optionalProperties = optionalProperties;
  }

  public RBSet<String> getRequiredProperties() {
    return requiredProperties;
  }

  public RBSet<String> getOptionalProperties() {
    return optionalProperties;
  }

  @Override
  public String toString() {
    return Strings.format("[JVI %s required: %s ; %s optional: %s JVI]",
        requiredProperties.size(), requiredProperties,
        optionalProperties.size(), optionalProperties);
  }


  public static class JsonValidationInstructionsBuilder implements RBBuilder<JsonValidationInstructions> {

    private RBSet<String> requiredProperties;
    private RBSet<String> optionalProperties;

    private JsonValidationInstructionsBuilder() {}

    public static JsonValidationInstructionsBuilder jsonValidationInstructionsBuilder() {
      return new JsonValidationInstructionsBuilder();
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(String firstProperty, String... rest) {
      List<String> firstAndRest = concatenateFirstAndRest(firstProperty, rest);
      RBPreconditions.checkArgument(
          firstAndRest.size() == newRBSet(firstAndRest).size(),
          "setting required properties with duplicate entries: %s",
          firstAndRest);
      requiredProperties = checkNotAlreadySet(this.requiredProperties, newRBSet(firstAndRest));
      return this;
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(String firstProperty, String... rest) {
      List<String> firstAndRest = concatenateFirstAndRest(firstProperty, rest);
      RBPreconditions.checkArgument(
          firstAndRest.size() == newRBSet(firstAndRest).size(),
          "setting optional properties with duplicate entries: %s",
          firstAndRest);
      optionalProperties = checkNotAlreadySet(this.optionalProperties, newRBSet(firstAndRest));
      return this;
    }

    public JsonValidationInstructionsBuilder hasNoRequiredProperties() {
      requiredProperties = checkNotAlreadySet(this.requiredProperties, emptyRBSet());
      return this;
    }

    public JsonValidationInstructionsBuilder hasNoOptionalProperties() {
      optionalProperties = checkNotAlreadySet(this.optionalProperties, emptyRBSet());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(
          requiredProperties,
          "must either call 'hasNoRequiredProperties()' or add at least one required property");
      RBPreconditions.checkNotNull(
          optionalProperties,
          "must either call 'hasNoOptionalProperties() or add at least one optional property");
      RBSet<String> intersection = RBSets.intersection(requiredProperties, optionalProperties);
      RBPreconditions.checkArgument(
          intersection.isEmpty(),
          "required properties cannot also be optional; found overlap = %s",
          intersection);
    }

    @Override
    public JsonValidationInstructions buildWithoutPreconditions() {
      return new JsonValidationInstructions(requiredProperties, optionalProperties);
    }

  }

}
