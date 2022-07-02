package com.rb.nonbiz.json;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;

/**
 * Hold information about required and optional top-level properties for a JSON object.
 *
 * <p> The purpose is to check JSON objects being converted via our JsonApiConverters.
 * In particular, we want to know if an optional property is misspelled, which could allow
 * a silent failure to propagate. </p>
 *
 * <p> E.g. if a JSON object contained optional entries: </p>
 * <pre>
 * "orderNotionalLimits" = {
 *   "min": 100,
 *   "mxa": 1000     // TYPO: "mxa" instead of "max"
 * }
 * </pre>
 *
 * <p> Because the max order notional size is optional, a typo would result in no maximum,
 * e.g. unlimited order size. </p>
 *
 * <p> We could extend validation to checking valid data ranges (e.g. UnitFractions must be between 0.0 and 1.0).
 * However, this wouldn't add much value since our constructors already enforce these limits. </p>
 */
public class JsonValidationInstructions {

  /**
   * Some JSON properties, such as the data under YearlyTimeSeries, do not have a fixed class.
   * We will use class to denote that. This is convenient because many JSON API converters have a private static final
   * JsonValidationInstructions. Because that's static and gets created once, there's no way to know the type of
   * the property at that time.
   */
  public static final Class<?> UNKNOWN_CLASS_OF_JSON_PROPERTY = Class.class;

  private final RBMap<String, Class<?>> requiredProperties;
  private final RBMap<String, Class<?>> optionalProperties;

  private JsonValidationInstructions(
      RBMap<String, Class<?>> requiredProperties,
      RBMap<String, Class<?>> optionalProperties) {
    this.requiredProperties = requiredProperties;
    this.optionalProperties = optionalProperties;
  }

  public static JsonValidationInstructions emptyJsonValidationInstructions() {
    return jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .hasNoOptionalProperties()
        .build();
  }

  public RBMap<String, Class<?>> getRequiredProperties() {
    return requiredProperties;
  }

  public List<String> getRequiredPropertiesAsSortedList() {
    return requiredProperties.sortedKeys(Ordering.natural());
  }

  public RBMap<String, Class<?>> getOptionalProperties() {
    return optionalProperties;
  }

  public List<String> getOptionalPropertiesAsSortedList() {
    return optionalProperties.sortedKeys(Ordering.natural());
  }

  @Override
  public String toString() {
    return Strings.format("[JVI %s required: %s ; %s optional: %s JVI]",
        requiredProperties.size(), requiredProperties,
        optionalProperties.size(), optionalProperties);
  }


  public static class JsonValidationInstructionsBuilder implements RBBuilder<JsonValidationInstructions> {

    private RBMap<String, Class<?>> requiredProperties;
    private RBMap<String, Class<?>> optionalProperties;

    private JsonValidationInstructionsBuilder() {}

    public static JsonValidationInstructionsBuilder jsonValidationInstructionsBuilder() {
      return new JsonValidationInstructionsBuilder();
    }

    public JsonValidationInstructionsBuilder hasNoRequiredProperties() {
      requiredProperties = checkNotAlreadySet(this.requiredProperties, emptyRBMap());
      return this;
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1) {
      return setRequiredProperties(singletonRBMap(property1, class1));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2) {
      return setRequiredProperties(rbMapOf(
          property1, class1,
          property2, class2));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3) {
      return setRequiredProperties(rbMapOf(
          property1, class1,
          property2, class2,
          property3, class3));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4) {
      return setRequiredProperties(rbMapOf(
          property1, class1,
          property2, class2,
          property3, class3,
          property4, class4));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(RBMap<String, Class<?>> requiredProperties) {
      this.requiredProperties = checkNotAlreadySet(this.requiredProperties, requiredProperties);
      return this;
    }

    public JsonValidationInstructionsBuilder hasNoOptionalProperties() {
      optionalProperties = checkNotAlreadySet(this.optionalProperties, emptyRBMap());
      return this;
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1) {
      return setOptionalProperties(singletonRBMap(property1, class1));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2) {
      return setOptionalProperties(rbMapOf(
          property1, class1,
          property2, class2));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3) {
      return setOptionalProperties(rbMapOf(
          property1, class1,
          property2, class2,
          property3, class3));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4) {
      return setOptionalProperties(rbMapOf(
          property1, class1,
          property2, class2,
          property3, class3,
          property4, class4));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(RBMap<String, Class<?>> optionalProperties) {
      this.optionalProperties = checkNotAlreadySet(this.optionalProperties, optionalProperties);
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
      RBSet<String> intersection = newRBSet(Sets.intersection(requiredProperties.keySet(), optionalProperties.keySet()));
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
