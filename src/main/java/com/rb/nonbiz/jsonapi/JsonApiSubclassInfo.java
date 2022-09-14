package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;

/**
 * This is for the special case in the JSON API where we need to represent one of many subclasses of a single class.
 *
 * <p> Unlike the most widely used {@link JsonApiClassDocumentation}, there is no {@link JsonValidationInstructions}
 * here. That normally gets used for attaching type info to a property string key. However, in the case of
 * superclass / subclass, there will not be any additional properties in the representation. Typically, it will be
 * an object such as: {@code { "type" = "SubClass1", "subclassProperty1" = "foo", "subclassProperty2" = "bar" } }.
 * Using the terminology below, {@link #getDiscriminatorPropertyValue()} would be "SubClass1".
 * The discriminator property name is "type", but that's not stored here. </p>
 *
 * <p> The corresponding JSON API converter is only stored here so that we can traverse the tree of objects
 * in the input and output classes of our API, which helps us gather a list of all JSON API entities that should
 * make it into the documentation. This is goes against our convention of never using 'verb classes' as objects
 * to pass around, but this exception is necessary here. </p>
 */
public class JsonApiSubclassInfo {

  private final Class<?> classOfSubclass;
  private final Optional<String> discriminatorPropertyValue;
  private final Optional<HasJsonApiDocumentation> jsonApiConverterForTraversing;

  private JsonApiSubclassInfo(
      Class<?> classOfSubclass,
      Optional<String> discriminatorPropertyValue,
      Optional<HasJsonApiDocumentation> jsonApiConverterForTraversing) {
    this.classOfSubclass = classOfSubclass;
    this.discriminatorPropertyValue = discriminatorPropertyValue;
    this.jsonApiConverterForTraversing = jsonApiConverterForTraversing;
  }

  public Class<?> getClassOfSubclass() {
    return classOfSubclass;
  }

  /**
   * A typical way to represent a class with multiple subclasses in the JSON API is this:
   * {@literal  type: 'subclass2', property2A: foo, property2B: bar }. Here, 'type' is the 'discriminator property',
   * and 'subclass2' is {@link #getDiscriminatorPropertyValue()}.
   */
  public Optional<String> getDiscriminatorPropertyValue() {
    return discriminatorPropertyValue;
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

  @Override
  public String toString() {
    return Strings.format("[JASI class= %s ; discrValue= %s ; jsonApiConverterForTraversing= %s JASI]",
        classOfSubclass,
        discriminatorPropertyValue,
        transformOptional(
            jsonApiConverterForTraversing,
            v -> v.getClass().getSimpleName())
            .orElse("<no HasJsonApiDocumentation>"));
  }


  /**
   * An {@link RBBuilder} which gives you the only way to instantiate a {@link JsonApiSubclassInfo}.
   */
  public static class JsonApiSubclassInfoBuilder implements RBBuilder<JsonApiSubclassInfo> {

    private Class<?> classOfSubclass;
    private Optional<String> discriminatorPropertyValue;
    private Optional<HasJsonApiDocumentation> jsonApiConverterForTraversing;

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
          this.discriminatorPropertyValue, Optional.of(discriminatorPropertyValue));
      return this;
    }

    public JsonApiSubclassInfoBuilder hasNoDiscriminatorPropertyAndThereforeNoValue() {
      this.discriminatorPropertyValue = checkNotAlreadySet(
          this.discriminatorPropertyValue, Optional.empty());
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

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(classOfSubclass);
      RBPreconditions.checkNotNull(discriminatorPropertyValue);
      RBPreconditions.checkNotNull(jsonApiConverterForTraversing);
    }

    @Override
    public JsonApiSubclassInfo buildWithoutPreconditions() {
      return new JsonApiSubclassInfo(
          classOfSubclass,
          discriminatorPropertyValue,
          jsonApiConverterForTraversing);
    }

  }

}
