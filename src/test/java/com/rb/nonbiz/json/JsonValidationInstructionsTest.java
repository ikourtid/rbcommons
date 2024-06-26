package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptorTest.jsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.emptyJsonValidationInstructions;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.stringListMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonValidationInstructionsTest extends RBTestMatcher<JsonValidationInstructions> {

  // This does not have to be shared across all tests in this file, but doing so simplifies the tests.
  private final Class<?> SHARED_CLASS = String.class;
  private final SimpleClassJsonApiPropertyDescriptor SHARED_CLASS_DESCRIPTOR =
      simpleClassJsonApiPropertyDescriptor(SHARED_CLASS);

  @Test
  public void testIsEmpty() {
    assertTrue( makeTrivialObject().isEmpty());
    assertFalse(makeNontrivialObject().isEmpty());
  }

  @Test
  public void testGetters() {
    assertTrue(emptyJsonValidationInstructions().isEmpty());
    assertEquals(0, emptyJsonValidationInstructions().getAllProperties().size());

    JsonValidationInstructions instructions = jsonValidationInstructionsBuilder()
        .setRequiredProperties(
            "required2", SHARED_CLASS,    // out of order on purpose
            "required1", SHARED_CLASS)
        .setOptionalProperties(
            "optional2", SHARED_CLASS,    // out of order on purpose
            "optional1", SHARED_CLASS)
        .build();

    assertFalse(instructions.isEmpty());
    assertEquals(
        rbSetOf("required1", "required2", "optional1", "optional2"),
        instructions.getAllProperties());

    assertThat(
        ImmutableList.of("required1", "required2"),
        stringListMatcher(
            instructions.getRequiredPropertiesAsSortedList()));

    assertThat(
        ImmutableList.of("optional1", "optional2"),
        stringListMatcher(
            instructions.getOptionalPropertiesAsSortedList()));

    assertThat(
        rbMapOf(
            "required1", SHARED_CLASS_DESCRIPTOR,
            "required2", SHARED_CLASS_DESCRIPTOR),
        rbMapMatcher(
            instructions.getRequiredProperties(),
            f -> jsonApiPropertyDescriptorMatcher(f)));

    assertThat(
        rbMapOf(
            "optional1", SHARED_CLASS_DESCRIPTOR,
            "optional2", SHARED_CLASS_DESCRIPTOR),
        rbMapMatcher(
            instructions.getOptionalProperties(),
            f -> jsonApiPropertyDescriptorMatcher(f)));
  }

  @Test
  public void mustSpecifyEmptyRequiredOrOptionalSetsExplicitly_orThrows() {
    assertNullPointerException( () -> jsonValidationInstructionsBuilder()
        .setOnlyRequiredProperty("requiredPropertyOnly", SHARED_CLASS)
        .build());    // absence of optional properties is implied; throws
    assertNullPointerException( () -> jsonValidationInstructionsBuilder()
        .setOnlyOptionalProperty("optionalPropertyOnly", SHARED_CLASS)
        .build());    // absence of required properities is implied; throws

    JsonValidationInstructions doesNotThrow;
    doesNotThrow = jsonValidationInstructionsBuilder()
        .setOnlyRequiredProperty("requiredPropertyOnly", SHARED_CLASS)
        .hasNoOptionalProperties()   // explicitly specify 'no optional properties'
        .build();
    doesNotThrow = jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()   // explicitly specify 'no required properties'
        .setOnlyOptionalProperty("optionalPropertyOnly", SHARED_CLASS)
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
        .setOnlyRequiredProperty("required", SHARED_CLASS)
        .hasNoOptionalProperties()
        .hasNoOptionalProperties());

    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .hasNoRequiredProperties()
        .setOnlyOptionalProperty("optional", SHARED_CLASS));
  }

  @Test
  public void specifyProperties_andHasNoProperties_throws() {
    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .setOnlyRequiredProperty("required", SHARED_CLASS)
        .hasNoRequiredProperties()    // but already have a required property
        .hasNoOptionalProperties());

    assertIllegalArgumentException( () -> jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .setOnlyOptionalProperty("optional", SHARED_CLASS)
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
        matchRBMap(v -> v.getRequiredProperties(), f -> jsonApiPropertyDescriptorMatcher(f)),
        matchRBMap(v -> v.getOptionalProperties(), f -> jsonApiPropertyDescriptorMatcher(f)));
  }

}
