package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.json.JsonValidationInstructions.emptyJsonValidationInstructions;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.formatListInExistingOrder;
import static com.rb.nonbiz.text.Strings.formatOptional;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This is (mostly) human-readable text that explains how a Java object (non-enum) of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 *
 * <p> For the case of enums, see {@link JsonApiEnumDocumentation}. </p>
 */
public class JsonApiClassDocumentation extends JsonApiDocumentation {

  private final Class<?> clazz;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;
  private final JsonValidationInstructions jsonValidationInstructions;
  private final List<HasJsonApiDocumentation> childJsonApiConverters;
  private final Optional<JsonElement> trivialSampleJson;
  private final Optional<JsonElement> nontrivialSampleJson;

  private JsonApiClassDocumentation(
      Class<?> clazz,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation,
      List<HasJsonApiDocumentation> childJsonApiConverters,
      JsonValidationInstructions jsonValidationInstructions,
      Optional<JsonElement> trivialSampleJson,
      Optional<JsonElement> nontrivialSampleJson) {
    this.clazz = clazz;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
    this.jsonValidationInstructions = jsonValidationInstructions;
    this.childJsonApiConverters = childJsonApiConverters;
    this.trivialSampleJson = trivialSampleJson;
    this.nontrivialSampleJson = nontrivialSampleJson;
  }

  /**
   * Returns the Java class of the data class (NOT the JSON API converter verb class)
   * that this documentation refers to.
   *
   * <p> Design-wise, this does not have to be a Java class - it could have been just a string, or some other unique ID.
   * The reader of the JSON API should not care what the names of the Java classes are. But we need some sort of
   * unique way of referring to this JSON "class" (in the general object-oriented sense, not in the Java sense),
   * so let's just use the {@link Class#getSimpleName()} for that purpose. </p>
   *
   * <p> This is similar to what we do for instantiating {@link RBLog}. </p>
   */
  public Class<?> getClazz() {
    return clazz;
  }

  /**
   * A single-line summary for the class being converted to/from JSON by this JSON API converter.
   */
  @Override
  public HumanReadableDocumentation getSingleLineSummary() {
    return singleLineSummary;
  }

  /**
   * The JSON validation instructions.
   */
  public JsonValidationInstructions getJsonValidationInstructions() {
    return jsonValidationInstructions;
  }

  /**
   * A list of the required JSON properties.
   */
  public List<String> getRequiredProperties() {
    return jsonValidationInstructions.getRequiredPropertiesAsSortedList();
  }

  /**
   * A list of the optional JSON properties.
   */
  public List<String> getOptionalProperties() {
    return jsonValidationInstructions.getOptionalPropertiesAsSortedList();
  }

  /**
   * All the "sub"-JSON API converters that are being used by this JSON API converter.
   * This will help us generate pages that link to the JSON subobjects.
   *
   * <p> For example, we want the page that describes MarketInfo to also have links to
   * CurrentMarketInfo and DailyMarketInfo. </p>
   */
  public List<HasJsonApiDocumentation> getChildJsonApiConverters() {
    return childJsonApiConverters;
  }

  /**
   * Returns the human-readable documentation for this JSON API object.
   */
  @Override
  public HumanReadableDocumentation getLongDocumentation() {
    return longDocumentation;
  }

  /**
   * The JSON API often contains {@link JsonElement}s that contain other {@link JsonElement}s etc.
   * in them. This will tell us information about all contained objects.
   */
  public List<HasJsonApiDocumentation> getJsonApiConvertersForContainedObjects() {
    return childJsonApiConverters;
  }

  /**
   * Returns some trivial (i.e. minimal) sample JSON.
   */
  public Optional<JsonElement> getTrivialSampleJson() {
    return trivialSampleJson;
  }

  /**
   * Returns some non-trivial (i.e. not minimal) sample JSON.
   */
  public Optional<JsonElement> getNontrivialSampleJson() {
    return nontrivialSampleJson;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitJsonApiClassDocumentation(this);
  }

  @Override
  public Class<?> getClassBeingDocumented() {
    return getClazz();
  }

  @Override
  public String toString() {
    return Strings.format("[JACD %s %s %s %s ; childConverters: %s ; trivialJson: %s ; nonTrivialJson: %s JACD]",
        clazz.getSimpleName(),
        singleLineSummary,
        longDocumentation,
        jsonValidationInstructions,
        formatListInExistingOrder(childJsonApiConverters, v -> v.getClass().getSimpleName()),
        formatOptional(trivialSampleJson),
        formatOptional(nontrivialSampleJson));
  }


  public static class JsonApiClassDocumentationBuilder implements RBBuilder<JsonApiClassDocumentation> {

    private Class<?> clazz;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private JsonValidationInstructions jsonValidationInstructions;
    private List<HasJsonApiDocumentation> childJsonApiConverters;
    private Optional<JsonElement> trivialSampleJson;
    private Optional<JsonElement> nontrivialSampleJson;

    private JsonApiClassDocumentationBuilder() {}

    public static JsonApiClassDocumentationBuilder jsonApiClassDocumentationBuilder() {
      return new JsonApiClassDocumentationBuilder();
    }

    // FIXME IAK / FIXME SWA JSONDOC: once all JSON API classes get documented, we should remove this,
    // and also update SingleObjectJsonApiDocumentationRawGenerator and SingleStringDocumentationFuser.
    public static JsonApiClassDocumentationBuilder intermediateJsonApiClassDocumentationBuilder() {
      return jsonApiClassDocumentationBuilder()
          .setLongDocumentation(documentation("FIXME IAK / FIXME SWA JSONDOC"))
          .noTrivialSampleJsonSupplied()
          .noNontrivialSampleJsonSupplied();
    }

    // FIXME IAK / FIXME SWA JSONDOC: once all JSON API classes get documented, we should remove this,
    // and also update SingleObjectJsonApiDocumentationRawGenerator and SingleStringDocumentationFuser.
    // Same as above, but doesn't call .setLongDocumentation()
    public static JsonApiClassDocumentationBuilder intermediate2JsonApiClassDocumentationBuilder() {
      return jsonApiClassDocumentationBuilder()
          .noTrivialSampleJsonSupplied()
          .noNontrivialSampleJsonSupplied();
    }

    // FIXME IAK / FIXME SWA JSONDOC: once all JSON API classes get documented, we should remove this,
    // and also update SingleObjectJsonApiDocumentationRawGenerator and SingleStringDocumentationFuser.
    public static JsonApiClassDocumentation intermediateJsonApiClassDocumentationWithFixme(
        Class<?> clazz,
        HasJsonApiDocumentation ... items) {
      return jsonApiClassDocumentationBuilder()
          .setClass(clazz)
          .setSingleLineSummary(documentation("FIXME IAK / FIXME SWA JSONDOC"))
          .setLongDocumentation(documentation("FIXME IAK / FIXME SWA JSONDOC"))
          .hasNoJsonValidationInstructions()
          .hasChildJsonApiConverters(Arrays.asList(items))
          .noTrivialSampleJsonSupplied()
          .noNontrivialSampleJsonSupplied()
          .build();
    }

    public JsonApiClassDocumentationBuilder setClass(Class<?> clazz) {
      this.clazz = checkNotAlreadySet(this.clazz, clazz);
      return this;
    }

    public JsonApiClassDocumentationBuilder setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiClassDocumentationBuilder setJsonValidationInstructions(
        JsonValidationInstructions jsonValidationInstructions) {
      this.jsonValidationInstructions = checkNotAlreadySet(
          this.jsonValidationInstructions,
          jsonValidationInstructions);
      return this;
    }

    public JsonApiClassDocumentationBuilder hasNoJsonValidationInstructions() {
      return setJsonValidationInstructions(emptyJsonValidationInstructions());
    }

    public JsonApiClassDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    public JsonApiClassDocumentationBuilder hasChildJsonApiConverters(
        List<HasJsonApiDocumentation> childJsonApiConverters) {
      this.childJsonApiConverters = checkNotAlreadySet(this.childJsonApiConverters, childJsonApiConverters);
      return this;
    }

    public JsonApiClassDocumentationBuilder hasChildJsonApiConverters(
        HasJsonApiDocumentation first,
        HasJsonApiDocumentation second,
        HasJsonApiDocumentation ... rest) {
      return hasChildJsonApiConverters(concatenateFirstSecondAndRest(first, second, rest));
    }

    public JsonApiClassDocumentationBuilder hasSingleChildJsonApiConverter(HasJsonApiDocumentation onlyItem) {
      return hasChildJsonApiConverters(singletonList(onlyItem));
    }

    public JsonApiClassDocumentationBuilder hasNoChildJsonApiConverters() {
      return hasChildJsonApiConverters(emptyList());
    }

    public JsonApiClassDocumentationBuilder setTrivialSampleJson(JsonElement trivialSampleJson) {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.of(trivialSampleJson));
      return this;
    }

    public JsonApiClassDocumentationBuilder noTrivialSampleJsonSupplied() {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.empty());
      return this;
    }

    public JsonApiClassDocumentationBuilder setNontrivialSampleJson(JsonElement nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.of(nontrivialSampleJson));
      return this;
    }

    public JsonApiClassDocumentationBuilder noNontrivialSampleJsonSupplied() {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(clazz);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
      RBPreconditions.checkNotNull(jsonValidationInstructions);
      RBPreconditions.checkNotNull(childJsonApiConverters);
      RBPreconditions.checkNotNull(trivialSampleJson);
      RBPreconditions.checkNotNull(nontrivialSampleJson);

      RBPreconditions.checkArgument(
          !clazz.isEnum(),
          "Class %s cannot be an enum! Use JsonApiEnumDocumentation for that case",
          clazz);

      // Since the child nodes are 'verb classes', which never implement equals/hashCode (we rarely even do this with
      // data classes), this will check using simple pointer equality. We have it here to prevent mistakes where a
      // JSON API converter Class<?> specifies the same 'child JSON API converter' more than once.
      RBPreconditions.checkUnique(childJsonApiConverters);
    }

    @Override
    public JsonApiClassDocumentation buildWithoutPreconditions() {
      return new JsonApiClassDocumentation(
          clazz, singleLineSummary, longDocumentation, childJsonApiConverters, jsonValidationInstructions,
          trivialSampleJson, nontrivialSampleJson);
    }

  }


  /**
   * Use this for creating {@link JsonApiDocumentation} for simple primitives that are simple wrappers
   * around {@link RBNumeric}. The #nontrivialSampleJson is not optional here; it's small enough in the case of
   * numeric wrappers that it should always be specified.
   *
   * Numeric wrapper does not necessarily mean that it extends {@link RBNumeric}. It could be that it's a class
   * that only includes an {@link RBNumeric} inside it (i.e. object composition instead of inheritance.
   */
  public static class JsonApiRbNumericWrapperDocumentationBuilder implements RBBuilder<JsonApiClassDocumentation> {

    private Class<?> classBeingDocumented;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private JsonElement nontrivialSampleJson;

    private JsonApiRbNumericWrapperDocumentationBuilder() {}

    public static JsonApiRbNumericWrapperDocumentationBuilder jsonApiRbNumericWrapperDocumentationBuilder() {
      return new JsonApiRbNumericWrapperDocumentationBuilder();
    }

    // This cannot be <T extends RBNumeric<? extends T>>, because there are classes that are simple wrappers
    // around a RbNumericWrapper, but use object composition instead of inheritance. See this builder's class javadoc.
    public JsonApiRbNumericWrapperDocumentationBuilder
    setClassBeingDocumented(Class<?> classBeingDocumented) {
      this.classBeingDocumented = checkNotAlreadySet(this.classBeingDocumented, classBeingDocumented);
      return this;
    }

    public JsonApiRbNumericWrapperDocumentationBuilder setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiRbNumericWrapperDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    public JsonApiRbNumericWrapperDocumentationBuilder setNontrivialSampleJson(JsonElement nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, nontrivialSampleJson);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(classBeingDocumented);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
      RBPreconditions.checkNotNull(nontrivialSampleJson);
    }

    @Override
    public JsonApiClassDocumentation buildWithoutPreconditions() {
      return jsonApiClassDocumentationBuilder()
          .setClass(classBeingDocumented)
          .setSingleLineSummary(singleLineSummary)
          .setLongDocumentation(longDocumentation)
          // JsonValidationInstructions is for cases where there are properties, but this is n/a for a primitive.
          .hasNoJsonValidationInstructions()
          // primitives do not mention other entities under them that get serialized.
          .hasNoChildJsonApiConverters()
          .noTrivialSampleJsonSupplied()
          // Currently (Aug 2022), there's only room for one of the two sample JSON values in the resulting Swagger
          // documentation, and it's the 'nontrivial' that gets chosen to be shown. So let's use that one here.
          .setNontrivialSampleJson(nontrivialSampleJson)
          .build();
    }

  }

}
