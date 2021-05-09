package com.rb.nonbiz.json;

import com.google.gson.JsonObject;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.Consumer;

import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class JsonValidatorTest extends RBTest<JsonValidator> {

  @Test
  public void missingRequiredProperty_throws() {
    Consumer<JsonObject> validator = jsonObject -> {
      JsonValidationInstructions instructions = jsonValidationInstructionsBuilder()
          .setRequiredProperties("required1", "required2")
          .setOptionalProperties("optional1")
          .build();
      makeTestObject().validate(jsonObject, instructions);
    };

    // has neither required property
    assertIllegalArgumentException( () -> validator.accept(emptyJsonObject()));

    // has only the optional property
    assertIllegalArgumentException( () -> validator.accept(singletonJsonObject("optional1", "optionalValue1")));

    // has one required property and the optional property
    assertIllegalArgumentException( () -> validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "optional1", jsonString("optionaValue1"))));

    // only one required property
    assertIllegalArgumentException( () -> validator.accept(singletonJsonObject("required1", jsonString("value1"))));
    assertIllegalArgumentException( () -> validator.accept(singletonJsonObject("required2", jsonString("value2"))));

    // has both required properties; no exception
    validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2")));

    // has both required properties and the optional property; no exception
    validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2"),
        "optional1", jsonString("optionalValue1")));
  }

  @Test
  public void typo_throws() {
    Consumer<JsonObject> validator = jsonObject -> {
      JsonValidationInstructions instructions = jsonValidationInstructionsBuilder()
          .setRequiredProperties("required1", "required2")
          .setOptionalProperties("optional1")
          .build();
      makeTestObject().validate(jsonObject, instructions);
    };

    assertIllegalArgumentException( () -> validator.accept(jsonObject(
        "rEquired1", jsonString("value1"),   // TYPO
        "required2", jsonString("value2"),
        "optional1", jsonString("optionalValue1"))));

    assertIllegalArgumentException( () -> validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2"),
        "oPtional1", jsonString("optionalValue1"))));   // TYPO

    // no typos; no exception
    validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2"),
        "optional1", jsonString("optionalValue1")));
  }

  @Test
  public void extraProperty_throws() {
    Consumer<JsonObject> validator = jsonObject -> {
      JsonValidationInstructions instructions = jsonValidationInstructionsBuilder()
          .setRequiredProperties("required1", "required2")
          .setOptionalProperties("optional1")
          .build();
      makeTestObject().validate(jsonObject, instructions);
    };

    // no extra properties; does not throw
    validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2"),
        "optional1", jsonString("optionalValue1")));

    // having an extraneous property throws
    assertIllegalArgumentException( () -> validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2"),
        "optional1", jsonString("optionalValue1"),
        "extraneousProperty", jsonString("extraneousValue"))));

    // throws even if the number of properties is correct, but one property is unexpected
    assertIllegalArgumentException( () -> validator.accept(jsonObject(
        "required1", jsonString("value1"),
        "required2", jsonString("value2"),
        // replace optional property with an extraneous one
        "extraneousProperty", jsonString("extraneousValue"))));
  }

  @Override
  protected JsonValidator makeTestObject() {
    return new JsonValidator();
  }

}
