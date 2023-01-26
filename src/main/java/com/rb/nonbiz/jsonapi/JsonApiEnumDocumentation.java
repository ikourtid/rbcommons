package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiEnumDescriptor;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RoundTripStringConvertibleEnum;

/**
 * This is (mostly) human-readable text that explains how a Java enum of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 *
 * <p> For non-enums, see {@link JsonApiClassDocumentation}. </p>
 *
 * @see JsonApiDocumentation
 */
public class JsonApiEnumDocumentation<E extends Enum<E> & RoundTripStringConvertibleEnum<E>> extends JsonApiDocumentation {

  private final JsonApiEnumDescriptor<E> jsonApiEnumDescriptor;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;

  private JsonApiEnumDocumentation(
      JsonApiEnumDescriptor<E> jsonApiEnumDescriptor,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation) {
    this.jsonApiEnumDescriptor = jsonApiEnumDescriptor;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
  }

  public JsonApiEnumDescriptor<E> getJsonApiEnumDescriptor() {
    return jsonApiEnumDescriptor;
  }

  /**
   * A single-line summary for this enum.
   */
  @Override
  public HumanReadableDocumentation getSingleLineSummary() {
    return singleLineSummary;
  }
  
  /**
   * The human-readable documentation for this enum.
   */
  @Override
  public HumanReadableDocumentation getLongDocumentation() {
    return longDocumentation;
  }


  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitJsonApiEnumDocumentation(this);
  }

  @Override
  public Class<?> getClassBeingDocumented() {
    return jsonApiEnumDescriptor.getEnumClass();
  }

  @Override
  public String toString() {
    return Strings.format("[JACDoc %s %s %s JACDoc]",
        jsonApiEnumDescriptor,
        singleLineSummary,
        longDocumentation);
  }


  public static class JsonApiEnumDocumentationBuilder<E extends Enum<E> & RoundTripStringConvertibleEnum<E>>
      implements RBBuilder<JsonApiEnumDocumentation<E>> {

    private JsonApiEnumDescriptor<E> jsonApiEnumDescriptor;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;

    private JsonApiEnumDocumentationBuilder() {}

    public static <E extends Enum<E> & RoundTripStringConvertibleEnum<E>>
    JsonApiEnumDocumentationBuilder<E> jsonApiEnumDocumentationBuilder() {
      return new JsonApiEnumDocumentationBuilder<>();
    }

    public JsonApiEnumDocumentationBuilder<E> setJsonApiEnumDescriptor(JsonApiEnumDescriptor<E> jsonApiEnumDescriptor) {
      this.jsonApiEnumDescriptor = checkNotAlreadySet(this.jsonApiEnumDescriptor, jsonApiEnumDescriptor);
      return this;
    }

    public JsonApiEnumDocumentationBuilder<E> setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiEnumDocumentationBuilder<E> setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(jsonApiEnumDescriptor);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
    }

    @Override
    public JsonApiEnumDocumentation<E> buildWithoutPreconditions() {
      return new JsonApiEnumDocumentation<E>(
          jsonApiEnumDescriptor, singleLineSummary, longDocumentation);
    }

  }

}
