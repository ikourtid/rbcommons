package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
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
 * This is (mostly) human-readable text that explains how a Java object (non-enum) of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 *
 * <p> This is for the special case where an object can be one of many subclasses.
 * See also {@link JsonApiClassDocumentation} and {@link JsonApiEnumDocumentation}. </p>
 */
public class JsonApiClassWithSubclassesDocumentation extends JsonApiDocumentation {

  private final Class<?> classBeingDocumented;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;
  private final List<JsonApiSubclassInfo> jsonApiSubclassInfoList;
  private final Optional<String> discriminatorProperty;
  private final Optional<JsonElement> nontrivialSampleJson;

  private JsonApiClassWithSubclassesDocumentation(
      Class<?> classBeingDocumented,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation,
      List<JsonApiSubclassInfo> jsonApiSubclassInfoList,
      Optional<String> discriminatorProperty,
      Optional<JsonElement> nontrivialSampleJson) {
    this.classBeingDocumented = classBeingDocumented;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
    this.jsonApiSubclassInfoList = jsonApiSubclassInfoList;
    this.discriminatorProperty = discriminatorProperty;
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
   * Returns the human-readable documentation for this JSON API object.
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
  public Optional<String> getDiscriminatorProperty() {
    return discriminatorProperty;
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
    return Strings.format(
        "[JACWSD %s %s %s ; subClasses: %s ; discriminatorProperty= %s ; nontrivialSampleJson= %s JACWSD]",
        classBeingDocumented.getSimpleName(),
        singleLineSummary,
        longDocumentation,
        formatListInExistingOrder(jsonApiSubclassInfoList, v -> v.getClassOfSubclass().getSimpleName()),
        formatOptional(discriminatorProperty),
        formatOptional(nontrivialSampleJson));
  }


  public static class JsonApiClassWithSubclassesDocumentationBuilder
      implements RBBuilder<JsonApiClassWithSubclassesDocumentation> {

    private Class<?> classBeingDocumented;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private List<JsonApiSubclassInfo> jsonApiSubclassInfoList;
    private Optional<String> discriminatorProperty;
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
      this.discriminatorProperty = checkNotAlreadySet(this.discriminatorProperty, Optional.of(discriminatorProperty));
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder hasNoDiscriminatorProperty() {
      this.discriminatorProperty = checkNotAlreadySet(this.discriminatorProperty, Optional.empty());
      return this;
    }

    public JsonApiClassWithSubclassesDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
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
      RBPreconditions.checkNotNull(discriminatorProperty);
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

      discriminatorProperty.ifPresent(v -> RBPreconditions.checkArgument(
          !v.isEmpty(),
          "If the optional discriminator property is present, it cannot be the empty string."));
    }

    @Override
    public JsonApiClassWithSubclassesDocumentation buildWithoutPreconditions() {
      return new JsonApiClassWithSubclassesDocumentation(
          classBeingDocumented, singleLineSummary, longDocumentation,
          jsonApiSubclassInfoList, discriminatorProperty, nontrivialSampleJson);
    }

  }

}
