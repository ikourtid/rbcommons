package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.json.DataClassJsonApiDescriptorTest.dataClassJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.emptyJsonValidationInstructions;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;

public class JsonValidationInstructionsTest extends RBTestMatcher<JsonValidationInstructions> {

  // This does not have to be shared across all tests in this file, but doing so simplifies the tests.
  private static final Class<?> SHARED_CLASS = String.class;

  @Test
  public void mustSpecifyEmptyRequiredOrOptionalSetsExplicitly_orThrows() {
    assertNullPointerException( () -> jsonValidationInstructionsBuilder()
        .setRequiredProperties("requiredPropertyOnly", SHARED_CLASS)
        .build());    // absence of optional properties is implied; throws
    assertNullPointerException( () -> jsonValidationInstructionsBuilder()
        .setOptionalProperties("optionalPropertyOnly", SHARED_CLASS)
        .build());    // absence of required properities is implied; throws

    JsonValidationInstructions doesNotThrow;
    doesNotThrow = jsonValidationInstructionsBuilder()
        .setRequiredProperties("requiredPropertyOnly", SHARED_CLASS)
        .hasNoOptionalProperties()   // explicitly specify 'no optional properties'
        .build();
    doesNotThrow = jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()   // explicitly specify 'no required properties'
        .setOptionalProperties("optionalPropertyOnly", SHARED_CLASS)
        .build();
  }

  @Test
  public void duplicateProperties_throws() {
    BiFunction<String, String, JsonValidationInstructions> builder = (newRequired, newOptional) ->
        jsonValidationInstructionsBuilder()
            .setRequiredProperties(
                "required1", SHARED_CLASS,
                newRequired, SHARED_CLASS)
            .setOptionalProperties(
                "optional1", SHARED_CLASS,
                newOptional, SHARED_CLASS)
            .build();

    JsonValidationInstructions doesNotThrow = builder.apply("required2", "optional2");

    assertIllegalArgumentException( () -> builder.apply("required1", "optional1"));
    assertIllegalArgumentException( () -> builder.apply("required1", "optional2"));
    assertIllegalArgumentException( () -> builder.apply("required2", "optional1"));
  }

  @Test
  public void propertiesBothOptionalAndRequired_throws() {
    BiFunction<String, String, JsonValidationInstructions> builder = (newRequired, newOptional) ->
        jsonValidationInstructionsBuilder()
            .setRequiredProperties(
                "required1", SHARED_CLASS,
                newRequired, SHARED_CLASS)
            .setOptionalProperties(
                "optional1", SHARED_CLASS,
                newOptional, SHARED_CLASS)
            .build();

    JsonValidationInstructions doesNotThrow = builder.apply("required2", "optional2");

    // 'optional1' would be both required and optional:
    assertIllegalArgumentException( () -> builder.apply("optional1", "required2"));
    // 'required1' would be both required and optional:
    assertIllegalArgumentException( () -> builder.apply("optional2", "required1"));
    // add the same property to both the required and optional sets:
    assertIllegalArgumentException( () -> builder.apply("bothRequiredAndOptional", "bothRequiredAndOptional"));
  }

  @Test
  public void specifyHasNoPropertiesMultipleTimes_throws() {
    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .setRequiredProperties("required", SHARED_CLASS)
        .hasNoOptionalProperties()
        .hasNoOptionalProperties());

    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .hasNoRequiredProperties()
        .setOptionalProperties("optional", SHARED_CLASS));
  }

  @Test
  public void specifyProperties_andHasNoProperties_throws() {
    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .setRequiredProperties("required", SHARED_CLASS)
        .hasNoRequiredProperties()    // but already have a required property
        .hasNoOptionalProperties());

    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .setOptionalProperties("optional", SHARED_CLASS)
        .hasNoOptionalProperties());    // but already have an optional property
  }

  @Override
  public JsonValidationInstructions makeTrivialObject() {
    return emptyJsonValidationInstructions();
  }

  @Override
  public JsonValidationInstructions makeNontrivialObject() {
    return jsonValidationInstructionsBuilder()
        .setRequiredProperties(
            "firstRequired", SHARED_CLASS,
            "secondRequired", SHARED_CLASS)
        .setOptionalProperties(
            "firstOptional", SHARED_CLASS,
            "secondOptional", SHARED_CLASS)
        .build();
  }

  @Override
  public JsonValidationInstructions makeMatchingNontrivialObject() {
    return jsonValidationInstructionsBuilder()
        .setRequiredProperties(
            "firstRequired", SHARED_CLASS,
            "secondRequired", SHARED_CLASS)
        .setOptionalProperties(
            "firstOptional", SHARED_CLASS,
            "secondOptional", SHARED_CLASS)
        .build();
  }

  @Override
  protected boolean willMatch(JsonValidationInstructions expected, JsonValidationInstructions actual) {
    return jsonValidationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonValidationInstructions> jsonValidationInstructionsMatcher(
      JsonValidationInstructions expected) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRequiredProperties(), f -> dataClassJsonApiDescriptorMatcher(f)),
        matchRBMap(v -> v.getOptionalProperties(), f -> dataClassJsonApiDescriptorMatcher(f)));
  }

}
