package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;

public class JsonValidationInstructionsTest extends RBTestMatcher<JsonValidationInstructions> {

  public JsonValidationInstructions emptyJsonValidationInstructions() {
    return jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .hasNoOptionalProperties()
        .build();
  }

  @Test
  public void mustSpecifyEmptyRequiredOrOptionalSetsExplicitly_orThrows() {
    assertNullPointerException( () -> jsonValidationInstructionsBuilder()
        .setRequiredProperties("requiredPropertyOnly")
        .build());    // absence of optional properties is implied; throws
    assertNullPointerException( () -> jsonValidationInstructionsBuilder()
        .setOptionalProperties("optionalPropertyOnly")
        .build());    // absence of required properities is implied; throws

    JsonValidationInstructions doesNotThrow;
    doesNotThrow = jsonValidationInstructionsBuilder()
        .setRequiredProperties("requiredPropertyOnly")
        .hasNoOptionalProperties()   // explicitly specify 'no optional properties'
        .build();
    doesNotThrow = jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()   // explicitly specify 'no required properties'
        .setOptionalProperties("optionalPropertyOnly")
        .build();
  }

  @Test
  public void duplicateProperties_throws() {
    BiFunction<String, String, JsonValidationInstructions> builder = (newRequired, newOptional) ->
        jsonValidationInstructionsBuilder()
            .setRequiredProperties("required1", newRequired)
            .setOptionalProperties("optional1", newOptional)
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
            .setRequiredProperties("required1", newRequired)
            .setOptionalProperties("optional1", newOptional)
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
        .setRequiredProperties("required")
        .hasNoOptionalProperties()
        .hasNoOptionalProperties());

    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .hasNoRequiredProperties()
        .setOptionalProperties("optional"));
  }

  @Test
  public void specifyProperties_andHasNoProperties_throws() {
    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .setRequiredProperties("required")
        .hasNoRequiredProperties()    // but already have a required property
        .hasNoOptionalProperties());

    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .setOptionalProperties("optional")
        .hasNoOptionalProperties());    // but already have an optional property
  }

  @Override
  public JsonValidationInstructions makeTrivialObject() {
    return emptyJsonValidationInstructions();
  }

  @Override
  public JsonValidationInstructions makeNontrivialObject() {
    return jsonValidationInstructionsBuilder()
        .setRequiredProperties("firstRequired", "secondRequired")
        .setOptionalProperties("firstOptional", "secondOptional")
        .build();
  }

  @Override
  public JsonValidationInstructions makeMatchingNontrivialObject() {
    return jsonValidationInstructionsBuilder()
        .setRequiredProperties("firstRequired", "secondRequired")
        .setOptionalProperties("firstOptional", "secondOptional")
        .build();
  }

  @Override
  protected boolean willMatch(JsonValidationInstructions expected, JsonValidationInstructions actual) {
    return jsonValidationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonValidationInstructions> jsonValidationInstructionsMatcher(
      JsonValidationInstructions expected) {
    return makeMatcher(expected,
        match(v -> v.getRequiredProperties(), f -> rbSetEqualsMatcher(f)),
        match(v -> v.getOptionalProperties(), f -> rbSetEqualsMatcher(f)));
  }

}
