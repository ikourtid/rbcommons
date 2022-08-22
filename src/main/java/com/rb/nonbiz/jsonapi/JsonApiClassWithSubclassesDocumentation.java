package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.rb.nonbiz.collections.RBLists;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.text.Strings.formatListInExistingOrder;
import static com.rb.nonbiz.text.Strings.formatOptional;
import static java.util.Collections.singletonList;

/**
 * <p> This is (mostly) human-readable text that explains how a Java object (non-enum) of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers. </p>
 *
 * <p> This is for the special case where an object can be one of many subclasses.
 * See also {@link JsonApiClassDocumentation} and {@link JsonApiEnumDocumentation}. </p>
 */
public class JsonApiClassWithSubclassesDocumentation extends JsonApiDocumentation {

  private final Class<?> classBeingDocumented;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;
  private final List<JsonApiSubclassInfo> jsonApiSubclassInfoList;
  private final String discriminatorProperty;
  private final Optional<JsonElement> trivialSampleJson;
  private final Optional<JsonElement> nontrivialSampleJson;

  private JsonApiClassWithSubclassesDocumentation(
      Class<?> classBeingDocumented,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation,
      List<JsonApiSubclassInfo> jsonApiSubclassInfoList,
      String discriminatorProperty,
      Optional<JsonElement> trivialSampleJson,
      Optional<JsonElement> nontrivialSampleJson) {
    this.classBeingDocumented = classBeingDocumented;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
    this.jsonApiSubclassInfoList = jsonApiSubclassInfoList;
    this.discriminatorProperty = discriminatorProperty;
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
  @Override
  public Class<?> getClassBeingDocumented() {
    return classBeingDocumented;
  }

  /**
   * A single-line summary for the class being converted to/from JSON by this JSON API converter.
   */
  @Override
  public HumanReadableDocumentation getSingleLineSummary() {
    return singleLineSummary;
  }

  /**
   * Returns the human-readable documentation for this JSON API object
   */
  @Override
  public HumanReadableDocumentation getLongDocumentation() {
    return longDocumentation;
  }

  /**
   * Information about all the subclasses that can be serialized in this position of the JSON.
   */
  public List<JsonApiSubclassInfo> getJsonApiSubclassInfoList() {
    return jsonApiSubclassInfoList;
  }

  /**
   * This lets us determine what exact subclass is being shown here. See OpenAPI specification
   * <a href="https://swagger.io/docs/specification/data-models/oneof-anyof-allof-not/">
   *   here.
   *   </a>
   */
  public String getDiscriminatorProperty() {
    return discriminatorProperty;
  }

  public Optional<JsonElement> getTrivialSampleJson() {
    return trivialSampleJson;
  }

  public Optional<JsonElement> getNontrivialSampleJson() {
    return nontrivialSampleJson;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitJsonApiClassWithSubclassesDocumentation(this);
  }

  @Override
  public String toString() {
    return Strings.format("[JACWSD %s %s %s %s %s %s %s JACWSD]",
        classBeingDocumented,
        singleLineSummary,
        longDocumentation,
        formatListInExistingOrder(jsonApiSubclassInfoList),
        discriminatorProperty,
        formatOptional(trivialSampleJson),
        formatOptional(nontrivialSampleJson));
  }


  public static class JsonApiClassWithSubclassesDocumentationBuilder 
      implements RBBuilder<JsonApiClassWithSubclassesDocumentation> {

    private Class<?> classBeingDocumented;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private List<JsonApiSubclassInfo> jsonApiSubclassInfoList;
    private String discriminatorProperty;
    private Optional<JsonElement> trivialSampleJson;
    private Optional<JsonElement> nontrivialSampleJson;

    private JsonApiClassWithSubclassesDocumentationBuilder() {}

    public static JsonApiClassWithSubclassesDocumentationBuilder jsonApiClassWithSubclassesDocumentationBuilder() {
      return new JsonApiClassWithSubclassesDocumentationBuilder();
    }
    public JsonApiClassWithSubclassesDocumentationBuilder setClassBeingDocumented(Class<?> classBeingDocumented) {
      this.classBeingDocumented = checkNotAlreadySet(this.classBeingDocumented, classBeingDocumented);
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setJsonApiInfoOnMultipleSubclasses(
        JsonApiSubclassInfo first,
        JsonApiSubclassInfo second,
        JsonApiSubclassInfo ... rest) {
      this.jsonApiSubclassInfoList = checkNotAlreadySet(this.jsonApiSubclassInfoList,
          concatenateFirstSecondAndRest(first, second, rest));
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setJsonApiInfoOnOnlySubclass(
        JsonApiSubclassInfo onlyItem) {
      this.jsonApiSubclassInfoList = checkNotAlreadySet(this.jsonApiSubclassInfoList, singletonList(onlyItem));
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setDiscriminatorProperty(String discriminatorProperty) {
      this.discriminatorProperty = checkNotAlreadySet(this.discriminatorProperty, discriminatorProperty);
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setTrivialSampleJson(JsonElement trivialSampleJson) {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.of(trivialSampleJson));
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder noTrivialSampleJsonSupplied() {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.empty());
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setNontrivialSampleJson(JsonElement nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.of(nontrivialSampleJson));
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder noNontrivialSampleJsonSupplied() {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(classBeingDocumented);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
      RBPreconditions.checkNotNull(jsonApiSubclassInfoList);
      RBPreconditions.checkNotNull(trivialSampleJson);
      RBPreconditions.checkNotNull(nontrivialSampleJson);

      RBPreconditions.checkArgument(
          !classBeingDocumented.isEnum(),
          "Class %s cannot be an enum! Use JsonApiEnumDocumentation for that case",
          classBeingDocumented);

      RBPreconditions.checkArgument(
          !jsonApiSubclassInfoList.isEmpty(),
          "We must have at least one subclass here");

      RBPreconditions.checkUnique(
          jsonApiSubclassInfoList.stream().map(v -> v.getClassOfSubclass()),
          "We cannot repeat a Class object here: %s",
          jsonApiSubclassInfoList);

      RBPreconditions.checkArgument(
          !discriminatorProperty.isEmpty(),
          "The discriminator property cannot be empty");
    }

    @Override
    public JsonApiClassWithSubclassesDocumentation buildWithoutPreconditions() {
      return new JsonApiClassWithSubclassesDocumentation(
          classBeingDocumented, singleLineSummary, longDocumentation,
          jsonApiSubclassInfoList, discriminatorProperty, trivialSampleJson, nontrivialSampleJson);
    }

  }

}
