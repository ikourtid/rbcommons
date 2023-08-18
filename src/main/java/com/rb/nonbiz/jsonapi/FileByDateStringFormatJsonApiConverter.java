package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.io.FileByDateStringFormat;
import com.rb.nonbiz.io.FileByDateStringFormat.Visitor;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.json.RBJsonObjectBuilder;
import com.rb.nonbiz.json.RBJsonObjectGetters;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.io.FileByDateStringFormat.fileByDateStringFormat;
import static com.rb.nonbiz.io.FileByDateStringFormat.fixedFilenameIgnoringDate;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonStringOrThrow;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link FileByDateStringFormat} to/from JSON for our public JSON APIs.
 */
public class FileByDateStringFormatJsonApiConverter {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .hasNoRequiredProperties()
      .setOptionalProperties(rbMapOf(
          "fileFormatParameterizedByDate", simpleClassJsonApiPropertyDescriptor(String.class, jsonPropertySpecificDocumentation(
              asSingleLineWithNewlines(
                  "A C-style String format with exactly one '%s' in it, to be parameterized by date, in YYYY-MM-DD format. ",
                  "For example, applying Dec. 25, 2023 to 'foo/bar.%s.csv' for will result in 'foo/bar.2023-12-25.csv'."))),
          "fixedFilenameIgnoringDate", simpleClassJsonApiPropertyDescriptor(String.class, jsonPropertySpecificDocumentation(
              asSingleLineWithNewlines(
                  "Unlike 'rawFormat', this is a constant filename that does not get parametrized by date.")))))
      .build();

  @Inject JsonValidator jsonValidator;

  public <T> JsonObject toJsonObject(FileByDateStringFormat<T> fileByDateStringFormat) {
    return jsonValidator.validate(
        fileByDateStringFormat.visit(new Visitor<JsonObject>() {
          @Override
          public JsonObject visitFileFormatParameterizedByDate(String fileFormatParametrizedByDate) {
            return rbJsonObjectBuilder()
                .setString("fileFormatParameterizedByDate", fileFormatParametrizedByDate)
                .build();
          }

          @Override
          public JsonObject visitFixedFilenameIgnoringDate(String fixedFilenameIgnoringDate) {
            return rbJsonObjectBuilder()
                .setString("fixedFilenameIgnoringDate", fixedFilenameIgnoringDate)
                .build();
          }
        }),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> FileByDateStringFormat<T> fromJsonObject(JsonObject jsonObject) {
    // The JSON_VALIDATION_INSTRUCTIONS does not have the ability to capture the fact that exactly one of these
    // two JSON properties must exist. Therefore,
    boolean isFormat = jsonObject.has("fileFormatParameterizedByDate");
    boolean isFixed = jsonObject.has("fixedFilenameIgnoringDate");
    RBPreconditions.checkArgument(
        isFormat ^ isFixed,
        "exactly 1 (not 0 or 2) of 'fileFormatParameterizedByDate' or 'fixedFilenameIgnoringDate' must be specified");
    return isFormat
        ? fileByDateStringFormat(getJsonStringOrThrow(jsonObject, "fileFormatParameterizedByDate"))
        : fixedFilenameIgnoringDate(getJsonStringOrThrow(jsonObject, "fixedFilenameIgnoringDate"));
  }

}
