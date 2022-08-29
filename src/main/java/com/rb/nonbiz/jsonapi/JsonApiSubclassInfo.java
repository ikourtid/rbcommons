package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonPropertySpecificDocumentation;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.text.Strings.formatOptional;

/**
 * This is for the special case in the JSON API where we need to represent one of many subclasses of a single class.
 *
 * <p> Unlike the most widely used {@link JsonApiClassDocumentation}, there is no {@link JsonValidationInstructions}
 * here. That normally gets used for attaching type info to a property string key. However, in the case of
 * superclass / subclass, there will not be any additional properties in the representation. Typically, it will be
 * an object such as: {@code { "type" = "SubClass1", "value" = { the JSON object for the subclass instance }}}.
 * That is, there will only be two properties. Using the terminology below, {@link #getDiscriminatorPropertyValue()}
 * would be "SubClass1", and {@link #getPropertyWithSubclassContents()} would be "value" (the property name").
 * The discriminator property name is "type", but that's not stored here. </p>
 *
 * <p> The corresponding JSON API converter is only stored here so that we can traverse the tree of objects
 * in the input and output classes of our API, which helps us gather a list of all JSON API entities that should
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


  /**
   * An {@link RBBuilder} which gives you the only way to instantiate a {@link JsonApiSubclassInfo}.
   */
  public static class JsonApiSubclassInfoBuilder implements RBBuilder<JsonApiSubclassInfo> {

    private Class<?> classOfSubclass;
    private String discriminatorPropertyValue;
    private String propertyWithSubclassContents;
    private HasJsonApiDocumentation jsonApiConverterForTraversing;
    private Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

    private JsonApiSubclassInfoBuilder() {}
    
    public static JsonApiSubclassInfoBuilder jsonApiSubclassInfoBuilder() {
      return new JsonApiSubclassInfoBuilder();
    }

    public JsonApiSubclassInfoBuilder setClassOfSubclass(Class<?> classOfSubclass) {
      this.classOfSubclass = checkNotAlreadySet(this.classOfSubclass, classOfSubclass);
      return this;
    }

    public JsonApiSubclassInfoBuilder setDiscriminatorPropertyValue(String discriminatorPropertyValue) {
      this.discriminatorPropertyValue = checkNotAlreadySet(
          this.discriminatorPropertyValue, discriminatorPropertyValue);
      return this;
    }

    public JsonApiSubclassInfoBuilder setPropertyWithSubclassContents(String propertyWithSubclassContents) {
      this.propertyWithSubclassContents = checkNotAlreadySet(
          this.propertyWithSubclassContents, propertyWithSubclassContents);
      return this;
    }

    public JsonApiSubclassInfoBuilder setJsonApiConverterForTraversing(
        HasJsonApiDocumentation jsonApiConverterForTraversing) {
      this.jsonApiConverterForTraversing = checkNotAlreadySet(
          this.jsonApiConverterForTraversing, jsonApiConverterForTraversing);
      return this;
    }

    public JsonApiSubclassInfoBuilder setJsonPropertySpecificDocumentation(
        JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
      this.jsonPropertySpecificDocumentation = checkNotAlreadySet(
          this.jsonPropertySpecificDocumentation, Optional.of(jsonPropertySpecificDocumentation));
      return this;
    }

    public JsonApiSubclassInfoBuilder hasNoJsonPropertySpecificDocumentation() {
      this.jsonPropertySpecificDocumentation = checkNotAlreadySet(
          this.jsonPropertySpecificDocumentation, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(classOfSubclass);
      RBPreconditions.checkNotNull(discriminatorPropertyValue);
      RBPreconditions.checkNotNull(propertyWithSubclassContents);
      RBPreconditions.checkNotNull(jsonApiConverterForTraversing);
      RBPreconditions.checkNotNull(jsonPropertySpecificDocumentation);

      RBPreconditions.checkNotNull(discriminatorPropertyValue);
      RBPreconditions.checkNotNull(propertyWithSubclassContents);
    }

    @Override
    public JsonApiSubclassInfo buildWithoutPreconditions() {
      return new JsonApiSubclassInfo(
          classOfSubclass,
          discriminatorPropertyValue,
          propertyWithSubclassContents,
          jsonApiConverterForTraversing,
          jsonPropertySpecificDocumentation);
    }

  }
  

}
