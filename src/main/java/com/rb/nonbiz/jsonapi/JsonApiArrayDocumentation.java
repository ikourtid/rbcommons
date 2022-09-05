package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Optional;

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
 * However, that gets validated by the (separate) JSON API converter that converts the items inside the array. </p>
 *
 * <p> Also, those other implementers of {@link JsonApiDocumentation} have sample JSON in them. However,
 * since this is an array, there's no need for sample JSON for the entire array; some sample JSON for the array items
 * would suffice. That JSON would live in that array item's JSON API converter, however. </p>
 */
public class JsonApiArrayDocumentation extends JsonApiDocumentation {

  private final Class<?> topLevelClass;
  private final Class<?> classOfArrayItems;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;
  private final Optional<HasJsonApiDocumentation> childNode;

  private JsonApiArrayDocumentation(
      Class<?> topLevelClass,
      Class<?> classOfArrayItems,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation,
      Optional<HasJsonApiDocumentation> childNode) {
    this.topLevelClass = topLevelClass;
    this.classOfArrayItems = classOfArrayItems;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
    this.childNode = childNode;
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
  public Class<?> getTopLevelClass() {
    return topLevelClass;
  }

  public Class<?> getClassOfArrayItems() {
    return classOfArrayItems;
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
   * <p> Other implementers of {@link JsonApiDocumentation} have a list of {@link HasJsonApiDocumentation}
   * (essentially JSON API converters), which can have multiple JSON API converters in it. Here, however, we can only
   * have 1 (if there exists a separate JSON API converter for the items in the array) or 0 (if those items are
   * converted by the 'whole array' JSON API converter and don't have a separate converter). </p>
   */
  public Optional<HasJsonApiDocumentation> getChildNode() {
    return childNode;
  }

  /**
   * Returns the human-readable documentation for this JSON API object.
   */
  @Override
  public HumanReadableDocumentation getLongDocumentation() {
    return longDocumentation;
  }


  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitJsonApiArrayDocumentation(this);
  }

  @Override
  public Class<?> getClassBeingDocumented() {
    return getTopLevelClass();
  }

  @Override
  public String toString() {
    return Strings.format("[JAAD %s %s %s %s %s JACD]",
        topLevelClass,
        classOfArrayItems,
        singleLineSummary,
        longDocumentation,
        formatOptional(childNode));
  }


  public static class JsonApiArrayDocumentationBuilder implements RBBuilder<JsonApiArrayDocumentation> {

    private Class<?> topLevelClass;
    private Class<?> classOfArrayItems;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private Optional<HasJsonApiDocumentation> childNode;

    private JsonApiArrayDocumentationBuilder() {}

    public static JsonApiArrayDocumentationBuilder jsonApiArrayDocumentationBuilder() {
      return new JsonApiArrayDocumentationBuilder();
    }

    public JsonApiArrayDocumentationBuilder setTopLevelClass(Class<?> topLevelClass) {
      this.topLevelClass = checkNotAlreadySet(this.topLevelClass, topLevelClass);
      return this;
    }

    public JsonApiArrayDocumentationBuilder setClassOfArrayItems(Class<?> classOfArrayItems) {
      this.classOfArrayItems = checkNotAlreadySet(this.classOfArrayItems, classOfArrayItems);
      return this;
    }

    public JsonApiArrayDocumentationBuilder setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiArrayDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    public JsonApiArrayDocumentationBuilder hasChildNode(HasJsonApiDocumentation childNode) {
      this.childNode = checkNotAlreadySet(this.childNode, Optional.of(childNode));
      return this;
    }

    public JsonApiArrayDocumentationBuilder hasNoChildNode() {
      this.childNode = checkNotAlreadySet(this.childNode, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(topLevelClass);
      RBPreconditions.checkNotNull(classOfArrayItems);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
      RBPreconditions.checkNotNull(childNode);

      RBPreconditions.checkArgument(
          !topLevelClass.isEnum(),
          "Class %s cannot be an enum! Use JsonApiEnumDocumentation for that case",
          topLevelClass);

      RBPreconditions.checkArgument(
          !topLevelClass.equals(classOfArrayItems),
          "Both the top-level class and the class of the array items are equal: %s",
          topLevelClass);
    }

    @Override
    public JsonApiArrayDocumentation buildWithoutPreconditions() {
      return new JsonApiArrayDocumentation(
          topLevelClass, classOfArrayItems, singleLineSummary, longDocumentation, childNode);
    }

  }

}
