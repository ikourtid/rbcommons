package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiEnumDescriptor;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * <p> This is (mostly) human-readable text that explains how a Java enum of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers. </p>
 *
 * <p> For non-enums, see {@link JsonApiClassDocumentation}. </p>
 */
public class JsonApiEnumDocumentation<E extends Enum<E>> extends JsonApiDocumentation {

  private final JsonApiEnumDescriptor<E> jsonApiEnumDescriptor;
  private final HumanReadableLabel singleLineSummary;
  private final String longDocumentation;

  private JsonApiEnumDocumentation(
      JsonApiEnumDescriptor<E> jsonApiEnumDescriptor,
      HumanReadableLabel singleLineSummary,
      String longDocumentation) {
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
  public HumanReadableLabel getSingleLineSummary() {
    return singleLineSummary;
  }
  
  /**
   * The human-readable documentation for this enum.
   */
  @Override
  public String getLongDocumentation() {
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
    return Strings.format("[JACD %s %s %s JACD]",
        jsonApiEnumDescriptor,
        singleLineSummary,
        longDocumentation);
  }


  public static class JsonApiEnumDocumentationBuilder<E extends Enum<E>>
      implements RBBuilder<JsonApiEnumDocumentation<E>> {

    private JsonApiEnumDescriptor<E> jsonApiEnumDescriptor;
    private HumanReadableLabel singleLineSummary;
    private String longDocumentation;

    private JsonApiEnumDocumentationBuilder() {}

    public static <E extends Enum<E>> JsonApiEnumDocumentationBuilder<E> jsonApiEnumDocumentationBuilder() {
      return new JsonApiEnumDocumentationBuilder<>();
    }

    public JsonApiEnumDocumentationBuilder<E> setJsonApiEnumDescriptor(JsonApiEnumDescriptor<E> jsonApiEnumDescriptor) {
      this.jsonApiEnumDescriptor = checkNotAlreadySet(this.jsonApiEnumDescriptor, jsonApiEnumDescriptor);
      return this;
    }

    public JsonApiEnumDocumentationBuilder<E> setSingleLineSummary(HumanReadableLabel singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiEnumDocumentationBuilder<E> setLongDocumentation(String longDocumentation) {
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
