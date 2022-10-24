package com.rb.nonbiz.json;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
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

  // See UNKNOWN_CLASS_OF_JSON_PROPERTY about why we're creating this blank class.
  public static class ContextSpecific {}

  // We could have used Class.class here, but there's a precondition against that in the static constructors of the
  // various JsonApiPropertyDescriptor subclasses. We're using ContextSpecific.class here,
  // because it's conceivable that new code
  // may use Class.class accidentally (not directly, but maybe calling .getClass() on a Class object),
  // and we want to catch that with a precondition. ContextSpecific, on the other hand, isn't something you can get to
  // inadvertently. The reason we created ContextSpecific is that this way the Swagger UI documentation
  // will say 'ContextSpecific' for the type of generic classes such as RBMapWithDefault. It's really meant as
  // human-readable documentation.
  public static final Class<?> UNKNOWN_CLASS_OF_JSON_PROPERTY = ContextSpecific.class;

  /**
   * Some JSON properties, such as the data under YearlyTimeSeries, have a Java object that's generic.
   * If we know ahead of time that the generic type parameter will be e.g. {@link Money}, then we can represent that with
   * javaGenericJsonApiPropertyDescriptor(YearlyTimeSeries.class, Money.class).
   *
   * <p> However, in many cases we do not know at compilation time.
   * For example, many JSON API converters have a private static final {@link JsonValidationInstructions}.
   * Because that's static and gets created once, there's no way to know the type of the property at that time.
   * ContiguousDiscreteRangeMapJsonApiConverter is one example; there are more. </p>
   *
   * <p> Therefore, we will use the below object in those situations. </p>
   */
  public static final JsonApiPropertyDescriptor UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR =
      simpleClassJsonApiPropertyDescriptor(UNKNOWN_CLASS_OF_JSON_PROPERTY);

  private final RBMap<String, JsonApiPropertyDescriptor> requiredProperties;
  private final RBMap<String, JsonApiPropertyDescriptor> optionalProperties;

  private JsonValidationInstructions(
      RBMap<String, JsonApiPropertyDescriptor> requiredProperties,
      RBMap<String, JsonApiPropertyDescriptor> optionalProperties) {
    this.requiredProperties = requiredProperties;
    this.optionalProperties = optionalProperties;
  }

  public static JsonValidationInstructions emptyJsonValidationInstructions() {
    return jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .hasNoOptionalProperties()
        .build();
  }

  public RBMap<String, JsonApiPropertyDescriptor> getRequiredProperties() {
    return requiredProperties;
  }

  public List<String> getRequiredPropertiesAsSortedList() {
    return requiredProperties.sortedKeys(Ordering.natural());
  }

  public RBMap<String, JsonApiPropertyDescriptor> getOptionalProperties() {
    return optionalProperties;
  }

  public List<String> getOptionalPropertiesAsSortedList() {
    return optionalProperties.sortedKeys(Ordering.natural());
  }

  public boolean isEmpty() {
    return requiredProperties.isEmpty() && optionalProperties.isEmpty();
  }

  @Override
  public String toString() {
    return isEmpty()
        ? "[JVI]"
        : Strings.format("[JVI %s required: %s ; %s optional: %s JVI]",
        requiredProperties.size(), requiredProperties,
        optionalProperties.size(), optionalProperties);
  }


  public static class JsonValidationInstructionsBuilder implements RBBuilder<JsonValidationInstructions> {

    private RBMap<String, JsonApiPropertyDescriptor> requiredProperties;
    private RBMap<String, JsonApiPropertyDescriptor> optionalProperties;

    private JsonValidationInstructionsBuilder() {}

    public static JsonValidationInstructionsBuilder jsonValidationInstructionsBuilder() {
      return new JsonValidationInstructionsBuilder();
    }

    public JsonValidationInstructionsBuilder hasNoRequiredProperties() {
      requiredProperties = checkNotAlreadySet(this.requiredProperties, emptyRBMap());
      return this;
    }

    public JsonValidationInstructionsBuilder setOnlyRequiredProperty(
        String property, JsonApiPropertyDescriptor jsonApiPropertyDescriptor) {
      return setRequiredProperties(singletonRBMap(property, jsonApiPropertyDescriptor));
    }

    public JsonValidationInstructionsBuilder setOnlyRequiredProperty(
        String property1, Class<?> class1) {
      return setRequiredProperties(singletonRBMap(property1, simpleClassJsonApiPropertyDescriptor(class1)));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2) {
      return setRequiredProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2)));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3) {
      return setRequiredProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3)));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4) {
      return setRequiredProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3),
          property4, simpleClassJsonApiPropertyDescriptor(class4)));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4,
        String property5, Class<?> class5) {
      return setRequiredProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3),
          property4, simpleClassJsonApiPropertyDescriptor(class4),
          property5, simpleClassJsonApiPropertyDescriptor(class5)));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4,
        String property5, Class<?> class5,
        String property6, Class<?> class6) {
      return setRequiredProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3),
          property4, simpleClassJsonApiPropertyDescriptor(class4),
          property5, simpleClassJsonApiPropertyDescriptor(class5),
          property6, simpleClassJsonApiPropertyDescriptor(class6)));
    }

    public JsonValidationInstructionsBuilder setRequiredProperties(RBMap<String, JsonApiPropertyDescriptor> requiredProperties) {
      this.requiredProperties = checkNotAlreadySet(this.requiredProperties, requiredProperties);
      return this;
    }

    public JsonValidationInstructionsBuilder hasNoOptionalProperties() {
      optionalProperties = checkNotAlreadySet(this.optionalProperties, emptyRBMap());
      return this;
    }

    public JsonValidationInstructionsBuilder setOnlyOptionalProperty(
        String property, JsonApiPropertyDescriptor jsonApiPropertyDescriptor) {
      return setOptionalProperties(singletonRBMap(property, jsonApiPropertyDescriptor));
    }

    public JsonValidationInstructionsBuilder setOnlyOptionalProperty(
        String property1, Class<?> class1) {
      return setOptionalProperties(singletonRBMap(property1, simpleClassJsonApiPropertyDescriptor(class1)));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2) {
      return setOptionalProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2)));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3) {
      return setOptionalProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3)));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4) {
      return setOptionalProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3),
          property4, simpleClassJsonApiPropertyDescriptor(class4)));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4,
        String property5, Class<?> class5) {
      return setOptionalProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3),
          property4, simpleClassJsonApiPropertyDescriptor(class4),
          property5, simpleClassJsonApiPropertyDescriptor(class5)));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(
        String property1, Class<?> class1,
        String property2, Class<?> class2,
        String property3, Class<?> class3,
        String property4, Class<?> class4,
        String property5, Class<?> class5,
        String property6, Class<?> class6) {
      return setOptionalProperties(rbMapOf(
          property1, simpleClassJsonApiPropertyDescriptor(class1),
          property2, simpleClassJsonApiPropertyDescriptor(class2),
          property3, simpleClassJsonApiPropertyDescriptor(class3),
          property4, simpleClassJsonApiPropertyDescriptor(class4),
          property5, simpleClassJsonApiPropertyDescriptor(class5),
          property6, simpleClassJsonApiPropertyDescriptor(class6)));
    }

    public JsonValidationInstructionsBuilder setOptionalProperties(RBMap<String, JsonApiPropertyDescriptor> optionalProperties) {
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
