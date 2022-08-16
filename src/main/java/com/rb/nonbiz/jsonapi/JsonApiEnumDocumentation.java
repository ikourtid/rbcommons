package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.RBLog;
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

  private final Class<E> enumClass;
  private final HumanReadableLabel singleLineSummary;
  private final String longDocumentation;

  private JsonApiEnumDocumentation(
      Class<E> enumClass,
      HumanReadableLabel singleLineSummary,
      String longDocumentation) {
    this.enumClass = enumClass;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
  }

  /**
   * Returns the Java class of the enum that this documentation refers to.
   *
   * <p> Design-wise, this does not have to be a Java class - it could have been just a string, or some other unique ID.
   * The reader of the JSON API should not care what the names of the Java classes are. But we need some sort of
   * unique way of referring to this JSON "class" (in the general object-oriented sense, not in the Java sense),
   * so let's just use the {@link Class#getSimpleName()} for that purpose. </p>
   *
   * <p> This is similar to what we do for instantiating {@link RBLog}. </p>
   */
  public Class<E> getEnumClass() {
    return enumClass;
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
    return getEnumClass();
  }

  @Override
  public String toString() {
    return Strings.format("[JACD %s %s %s %s %s %s %s JACD]",
        enumClass,
        singleLineSummary,
        longDocumentation);
  }


  public static class JsonApiEnumDocumentationBuilder<E extends Enum<E>>
      implements RBBuilder<JsonApiEnumDocumentation<E>> {

    private Class<E> enumClass;
    private HumanReadableLabel singleLineSummary;
    private String longDocumentation;

    private JsonApiEnumDocumentationBuilder() {}

    public static <E extends Enum<E>> JsonApiEnumDocumentationBuilder<E> jsonApiEnumDocumentationBuilder() {
      return new JsonApiEnumDocumentationBuilder<>();
    }

    public JsonApiEnumDocumentationBuilder<E> setEnumClass(Class<E> enumClass) {
      this.enumClass = checkNotAlreadySet(this.enumClass, enumClass);
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
      RBPreconditions.checkNotNull(enumClass);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);

      RBPreconditions.checkArgument(
          enumClass.isEnum(),
          // 'internal error' because this should never happen due to the builder having <E extends Enum<E>>.
          // But let's check it, just in case.
          "Internal error: class %s must be an enum!",
          enumClass);
    }

    @Override
    public JsonApiEnumDocumentation<E> buildWithoutPreconditions() {
      return new JsonApiEnumDocumentation<E>(
          enumClass, singleLineSummary, longDocumentation);
    }

  }

}
