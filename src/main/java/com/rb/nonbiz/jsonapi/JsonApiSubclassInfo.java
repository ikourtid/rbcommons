package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonPropertySpecificDocumentation;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.text.Strings;

import java.util.Optional;

import static com.rb.nonbiz.text.Strings.formatOptional;

/**
 * <p> This is for the special case in the JSON API where we need to represent
 * one of many subclasses of a single class. </p>
 *
 * <p> Unlike the most widely used {@link JsonApiClassDocumentation}, there is no {@link JsonValidationInstructions}
 * here. That normally gets used for attaching type info to a property string key. However, in the case of
 * superclass / subclass, there will not be any additional properties in the representation. Typically, it will be
 * an object such as: {@code { "type" = "SubClass1", "value" = { the JSON object for the subclass instance}}}.
 * That is, there will only be two properties. </p>
 *
 * <p> The corresponding JSON API converter is only stored here so that we can traverse the tree of objects
 * in the input & output classes of our API, which helps us gather a list of all JSON API entities that should
 * make it into the documentation. </p>
 */
public class JsonApiSubclassInfo {

  private final Class<?> classOfSubclass;
  private final String discriminatorPropertyValue;
  private final String propertyWithSubclassContents;
  private final HasJsonApiDocumentation jsonApiConverterForTraversing;
  private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

  private JsonApiSubclassInfo(
      Class<?> classOfSubclass,
      String discriminatorPropertyValue,
      String propertyWithSubclassContents,
      HasJsonApiDocumentation jsonApiConverterForTraversing,
      Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
    this.classOfSubclass = classOfSubclass;
    this.discriminatorPropertyValue = discriminatorPropertyValue;
    this.propertyWithSubclassContents = propertyWithSubclassContents;
    this.jsonApiConverterForTraversing = jsonApiConverterForTraversing;
    this.jsonPropertySpecificDocumentation = jsonPropertySpecificDocumentation;
  }

  public static JsonApiSubclassInfo jsonApiSubclassInfo(
      Class<?> classOfSubclass,
      String discriminatorPropertyValue,
      String propertyWithSubclassContents,
      HasJsonApiDocumentation jsonApiConverterForTraversing) {
    return new JsonApiSubclassInfo(
        classOfSubclass,
        discriminatorPropertyValue,
        propertyWithSubclassContents,
        jsonApiConverterForTraversing,
        Optional.empty());
  }

  public static JsonApiSubclassInfo jsonApiSubclassInfo(
      Class<?> classOfSubclass,
      String discriminatorPropertyValue,
      String propertyWithSubclassContents,
      HasJsonApiDocumentation jsonApiConverterForTraversing,
      JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
    return new JsonApiSubclassInfo(
        classOfSubclass,
        discriminatorPropertyValue,
        propertyWithSubclassContents,
        jsonApiConverterForTraversing,
        Optional.of(jsonPropertySpecificDocumentation));
  }

  public Class<?> getClassOfSubclass() {
    return classOfSubclass;
  }

  public String getDiscriminatorPropertyValue() {
    return discriminatorPropertyValue;
  }

  public String getPropertyWithSubclassContents() {
    return propertyWithSubclassContents;
  }

  public HasJsonApiDocumentation getJsonApiConverterForTraversing() {
    return jsonApiConverterForTraversing;
  }

  public Optional<JsonPropertySpecificDocumentation> getJsonPropertySpecificDocumentation() {
    return jsonPropertySpecificDocumentation;
  }

  @Override
  public String toString() {
    return Strings.format("[JASI %s %s %s %s %s JASI]",
        classOfSubclass,
        discriminatorPropertyValue,
        propertyWithSubclassContents,
        jsonApiConverterForTraversing,
        formatOptional(jsonPropertySpecificDocumentation));
  }

}
