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
  private final Optional<String> propertyWithSubclassContents;
  private final Optional<HasJsonApiDocumentation> jsonApiConverterForTraversing;
  private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

  private JsonApiSubclassInfo(
      Class<?> classOfSubclass,
      String discriminatorPropertyValue,
      Optional<String> propertyWithSubclassContents,
      Optional<HasJsonApiDocumentation> jsonApiConverterForTraversing,
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

  /**
   * A typical way to represent a class with multiple subclasses in the JSON API is this:
   * { type: 'subclass2', value: { property2A: foo, property2B: bar } }. Here, 'type' is the 'discriminator property',
   * 'subclass2' is {@link #getDiscriminatorPropertyValue()}, and 'value' is
   * {@link #getPropertyWithSubclassContents()}.
   */
  public String getDiscriminatorPropertyValue() {
    return discriminatorPropertyValue;
  }

  /**
   * A typical way to represent a class with multiple subclasses in the JSON API is this:
   * { type: 'subclass2', value: { property2A: foo, property2B: bar } }. Here, 'type' is the 'discriminator property',
   * 'subclass2' is {@link #getDiscriminatorPropertyValue()}, and 'value' is
   * {@link #getPropertyWithSubclassContents()}.
   *
   * It is optional because in certain simple cases, the class is trivial and has no data, in which case there is no
   * property in the JSON object that will point to the subclass's data. One such example is
   * OptimizationWasStaticallyInfeasible (in rbengine).
   */
  public Optional<String> getPropertyWithSubclassContents() {
    return propertyWithSubclassContents;
  }

  /**
   * <p> In practice, this stores the JSON API converter (verb class) that's responsible for converting objects of this
   * type back and forth from/to JSON. We store it here, and this method has the word 'traversing' in its name,
   * because it is convenient (see AllObjectsWithJsonApiDocumentationRawLister in rbengine) to start with the
   * top-level input and output objects, and traverse them to enumerate all the JSON API converters, which all
   * implement {@link HasJsonApiDocumentation}. This is better than having some top-level list of
   * {@link JsonApiDocumentation}, which would have hundreds of items and wouldn't have any logical structure. </p>
   *
   * <p> The reason it is optional is that some JSON API converter verb classes may convert certain objects
   * (usually trivial ones) without delegating to a separate JSON API converter. In those cases, this will be empty. </p>
   */
  public Optional<HasJsonApiDocumentation> getJsonApiConverterForTraversing() {
    return jsonApiConverterForTraversing;
  }

  /**
   * We always have documentation for classes (JSON API entities) in the API. Since every JSON object property
   * will store an object of some class, there's always some documentation. This is for cases where we
   * want additional documentation to a specific property, beyond the documentation in its class.
   */
  public Optional<JsonPropertySpecificDocumentation> getJsonPropertySpecificDocumentation() {
    return jsonPropertySpecificDocumentation;
  }

  @Override
  public String toString() {
    return Strings.format("[JASI %s %s %s %s %s JASI]",
        classOfSubclass,
        discriminatorPropertyValue,
        propertyWithSubclassContents,
        formatOptional(jsonApiConverterForTraversing),
        formatOptional(jsonPropertySpecificDocumentation));
  }


  /**
   * An {@link RBBuilder} which gives you the only way to instantiate a {@link JsonApiSubclassInfo}.
   */
  public static class JsonApiSubclassInfoBuilder implements RBBuilder<JsonApiSubclassInfo> {

    private Class<?> classOfSubclass;
    private String discriminatorPropertyValue;
    private Optional<String> propertyWithSubclassContents;
    private Optional<HasJsonApiDocumentation> jsonApiConverterForTraversing;
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
          this.propertyWithSubclassContents, Optional.of(propertyWithSubclassContents));
      return this;
    }

    public JsonApiSubclassInfoBuilder hasNoPropertyWithSubclassContents() {
      this.propertyWithSubclassContents = checkNotAlreadySet(
          this.propertyWithSubclassContents, Optional.empty());
      return this;
    }

    public JsonApiSubclassInfoBuilder setJsonApiConverterForTraversing(
        HasJsonApiDocumentation jsonApiConverterForTraversing) {
      this.jsonApiConverterForTraversing = checkNotAlreadySet(
          this.jsonApiConverterForTraversing, Optional.of(jsonApiConverterForTraversing));
      return this;
    }

    public JsonApiSubclassInfoBuilder hasNoSeparateJsonApiConverterForTraversing() {
      this.jsonApiConverterForTraversing = checkNotAlreadySet(
          this.jsonApiConverterForTraversing, Optional.empty());
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
