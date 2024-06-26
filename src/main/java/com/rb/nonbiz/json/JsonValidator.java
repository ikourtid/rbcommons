package com.rb.nonbiz.json;

import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSet;

/**
 * Performs some very basic schema validation checks on a {@link JsonObject}.
 *
 * <p> Checks the top-level properties only, and makes sure that the required
 * ones are present, and that no properties are present that are
 * neither required nor optional. </p>
 *
 * <p> If all checks pass, returns the original jsonObject unchanged. </p>
 *
 * <p> Here, we do something non-standard: the main code is in a static method. This is because there are a few rare cases
 * where we need to call this validator from another static method, where we cannot Inject a JsonValidator.
 * However, in almost all cases, we can (and should) inject a {@link JsonValidator}.
 * Hopefully the clunky name 'staticValidate' should prevent you from accidentally using it instead of plain 'validate'. </p>
 */
public class JsonValidator {

  /**
   * Validate a JsonObject according to supplied {@link JsonValidationInstructions}.
   */
  public JsonObject validate(
      JsonObject jsonObject,
      JsonValidationInstructions jsonValidationInstructions) {
    return staticValidate(jsonObject, jsonValidationInstructions);
  }

  /**
   * The static version of validating a JsonObject according to supplied {@link JsonValidationInstructions}.
   */
  public static JsonObject staticValidate(
      JsonObject jsonObject,
      JsonValidationInstructions jsonValidationInstructions) {
    RBSet<String> requiredProperties = newRBSet(jsonValidationInstructions.getRequiredProperties().keySet());
    RBSet<String> optionalProperties = newRBSet(jsonValidationInstructions.getOptionalProperties().keySet());
    RBSet<String> jsonKeys = rbSet(jsonObject.keySet());

    // Check each (top-level) JSON property to make sure it is found in either
    // the required or optional properties.
    for (String key : jsonKeys) {
      RBPreconditions.checkArgument (
          requiredProperties.contains(key) || optionalProperties.contains(key),
          "property '%s' not in %s required properties (%s) or in %s optional properties (%s)",
          key, requiredProperties.size(), requiredProperties,
          optionalProperties.size(), optionalProperties);
    }

    // Check that every required property is present.
    for (String requiredProperty : requiredProperties) {
      RBPreconditions.checkArgument(
          jsonKeys.contains(requiredProperty),
          "required property %s is not found in %s json properties: %s",
          requiredProperty, jsonKeys.size(), jsonKeys);
    }

    // no failures; return the original JSON object
    return jsonObject;
  }

}
