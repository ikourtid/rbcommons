package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.biz.types.Money;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.types.MoneyFraction;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonDoubleOrThrow;
import static com.rb.nonbiz.types.MoneyFraction.moneyFraction;

/**
 * Converts a {@link MoneyFraction} back and forth to JSON for our public API.
 */
public class MoneyFractionJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "numerator",   simpleClassJsonApiPropertyDescriptor(
              Money.class,
              jsonPropertySpecificDocumentation("The fraction numerator, in dollars.")),
          "denominator", simpleClassJsonApiPropertyDescriptor(
              Money.class,
              jsonPropertySpecificDocumentation("The fraction denominator, in dollars."))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public JsonObject toJsonObject(
      MoneyFraction moneyFraction) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setDouble("numerator",   moneyFraction.getNumerator().doubleValue())
            .setDouble("denominator", moneyFraction.getDenominator().doubleValue())
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public MoneyFraction fromJsonObject(
      JsonObject jsonObject) {
    return moneyFraction(
        money(getJsonDoubleOrThrow(jsonObject, "numerator")),
        money(getJsonDoubleOrThrow(jsonObject, "denominator")));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return null;
  }

}
