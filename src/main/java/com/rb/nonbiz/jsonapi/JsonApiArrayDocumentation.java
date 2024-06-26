package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.reflection.RBClass;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.reflection.RBClass.nonGenericRbClass;
import static com.rb.nonbiz.text.Strings.formatOptional;

/**
 * This is (mostly) human-readable text that explains how a Java object (non-enum) of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 *
 * <p> For the case of enums, see {@link JsonApiEnumDocumentation}. </p>
 *
 * <p> Note that the other implementers of {@link JsonApiDocumentation} store a {@link JsonValidationInstructions}
 * to allow us to validate that a JSON object has correct properties. An array, however, does not have any properties.
 * So we can't validate anything, other than the fact that the items inside the array are valid.
 * However, the validation of a single array item is performed by the (separate) JSON API converter
 * that converts the items inside the array. </p>
 *
 * <p> Also, those other implementers of {@link JsonApiDocumentation} have sample JSON in them. Since this is an array,
 * you could argue that there's no need for sample JSON for the entire array, and that some sample JSON for the array
 * <em> items </em> would suffice. That JSON would live in that array item's JSON API converter.
 * However, sometimes it's clearer to have sample JSON even for an entire array, in addition to sample JSON just
 * for the array items. So we will still keep a sample JSON property here. </p>
 *
 * @see JsonApiClassDocumentation
 */
public class JsonApiArrayDocumentation extends JsonApiDocumentation {

  private final Class<?> classBeingDocumented;
  private final RBClass<?> rbClassOfArrayItems;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;
  private final Optional<HasJsonApiDocumentation> childJsonApiConverter;
  private final Optional<JsonArray> nontrivialSampleJson;

  private JsonApiArrayDocumentation(
      Class<?> classBeingDocumented,
      RBClass<?> rbClassOfArrayItems,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation,
      Optional<HasJsonApiDocumentation> childJsonApiConverter,
      Optional<JsonArray> nontrivialSampleJson) {
    this.classBeingDocumented = classBeingDocumented;
    this.rbClassOfArrayItems = rbClassOfArrayItems;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
    this.childJsonApiConverter = childJsonApiConverter;
    this.nontrivialSampleJson = nontrivialSampleJson;
  }

  /**
   * Returns the Java class of the data class (NOT the JSON API converter verb class)
   * that this documentation refers to.
   *
   * <p> Since the JSON representation of this class will be a JSON array, this stores the class containing the
   * array, not the class of the items in the array. </p>
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

  public RBClass<?> getRbClassOfArrayItems() {
    return rbClassOfArrayItems;
  }

  /**
   * A single-line summary for the class being converted to/from JSON by this JSON API converter.
   */
  @Override
  public HumanReadableDocumentation getSingleLineSummary() {
    return singleLineSummary;
  }

  /**
   * All the "sub"-JSON API converters that are being used by this JSON API converter.
   * This will help us generate pages that link to the JSON subobjects.
   *
   * <p> For example, we want the page that describes MarketInfo to also have links to
   * CurrentMarketInfo and DailyMarketInfo. </p>
   *
   * <p> Other implementers of {@link JsonApiDocumentation} each have a list of {@link HasJsonApiDocumentation}
   * (essentially JSON API converters), which can have multiple JSON API converters in it. Here, however, we can only
   * have 1 (if there exists a separate JSON API converter for the items in the array) or 0 (if those items are
   * converted by the 'whole array' JSON API converter and don't have a separate converter). </p>
   *
   * <p> There is no strict concept of a JSON API converter. These are verb classes that are similar in terms of
   * what they do (convert a Java object back and forth to JSON), but they don't all implement any shared interface
   * other than {@link HasJsonApiDocumentation}. Using 'JSON API converter' in the name of this is a bit less
   * correct, but it is much clearer. </p>
   */
  public Optional<HasJsonApiDocumentation> getChildJsonApiConverter() {
    return childJsonApiConverter;
  }

  /**
   * Returns the human-readable documentation for this JSON API object.
   */
  @Override
  public HumanReadableDocumentation getLongDocumentation() {
    return longDocumentation;
  }

  /**
   * Returns {@link JsonArray} that can be used as an example inside the documentation.
   */
  public Optional<JsonArray> getNontrivialSampleJson() {
    return nontrivialSampleJson;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitJsonApiArrayDocumentation(this);
  }

  @Override
  public String toString() {
    return Strings.format(
        "[JAAD %s ; arrayClass: %s ; summary: %s ; longDoc: %s ; childConverter: %s ; nontrivialJson: %s JAAD]",
        classBeingDocumented.getSimpleName(),
        rbClassOfArrayItems,
        singleLineSummary,
        longDocumentation,
        formatOptional(childJsonApiConverter, v -> v.getClass().getSimpleName()),
        formatOptional(nontrivialSampleJson));
  }


  /**
   * An {@link RBBuilder} that lets you construct a {@link JsonApiArrayDocumentation} object.
   */
  public static class JsonApiArrayDocumentationBuilder implements RBBuilder<JsonApiArrayDocumentation> {

    private Class<?> classBeingDocumented;
    private RBClass<?> rbClassOfArrayItems;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private Optional<HasJsonApiDocumentation> childJsonApiConverter;
    private Optional<JsonArray> nontrivialSampleJson;

    private JsonApiArrayDocumentationBuilder() {}

    public static JsonApiArrayDocumentationBuilder jsonApiArrayDocumentationBuilder() {
      return new JsonApiArrayDocumentationBuilder();
    }

    public JsonApiArrayDocumentationBuilder setClassBeingDocumented(Class<?> classBeingDocumented) {
      this.classBeingDocumented = checkNotAlreadySet(this.classBeingDocumented, classBeingDocumented);
      return this;
    }

    public JsonApiArrayDocumentationBuilder setRBClassOfArrayItems(RBClass<?> rbClassOfArrayItems) {
      this.rbClassOfArrayItems = checkNotAlreadySet(this.rbClassOfArrayItems, rbClassOfArrayItems);
      return this;
    }

    public JsonApiArrayDocumentationBuilder setClassOfArrayItems(Class<?> classOfArrayItems) {
      return setRBClassOfArrayItems(nonGenericRbClass(classOfArrayItems));
    }

    public JsonApiArrayDocumentationBuilder setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiArrayDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    public JsonApiArrayDocumentationBuilder hasJsonApiConverter(HasJsonApiDocumentation childJsonApiConverter) {
      this.childJsonApiConverter = checkNotAlreadySet(this.childJsonApiConverter, Optional.of(childJsonApiConverter));
      return this;
    }

    public JsonApiArrayDocumentationBuilder hasNoJsonApiConverter() {
      this.childJsonApiConverter = checkNotAlreadySet(this.childJsonApiConverter, Optional.empty());
      return this;
    }

    public JsonApiArrayDocumentationBuilder setNontrivialSampleJson(JsonArray nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.of(nontrivialSampleJson));
      return this;
    }

    public JsonApiArrayDocumentationBuilder hasNoNontrivialSampleJson() {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(classBeingDocumented);
      RBPreconditions.checkNotNull(rbClassOfArrayItems);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
      RBPreconditions.checkNotNull(childJsonApiConverter);
      RBPreconditions.checkNotNull(nontrivialSampleJson);

      RBPreconditions.checkArgument(
          !classBeingDocumented.isEnum(),
          "Class %s cannot be an enum! Use JsonApiEnumDocumentation for that case",
          classBeingDocumented);

      RBPreconditions.checkArgument(
          !classBeingDocumented.equals(rbClassOfArrayItems.getOuterClass()),
          "Both the class being documented and the class of its array items are equal: %s",
          classBeingDocumented);

      nontrivialSampleJson.ifPresent(v -> RBPreconditions.checkArgument(
          v.size() > 0,
          "The sample JSON array is empty: %s",
          v));
    }

    @Override
    public JsonApiArrayDocumentation buildWithoutPreconditions() {
      return new JsonApiArrayDocumentation(
          classBeingDocumented,
          rbClassOfArrayItems,
          singleLineSummary,
          longDocumentation,
          childJsonApiConverter,
          nontrivialSampleJson);
    }

  }

}
