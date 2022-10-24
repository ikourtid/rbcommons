package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.rb.biz.jsonapi.JsonTicker;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.Partition;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.IidMapJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.RBMapJsonApiPropertyDescriptor;
import com.rb.nonbiz.reflection.RBClass;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.reflection.RBClass.nonGenericRbClass;
import static com.rb.nonbiz.text.Strings.formatListInExistingOrder;
import static com.rb.nonbiz.text.Strings.formatOptional;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This is (mostly) human-readable text that explains how a Java object (non-enum) of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 *
 * <p> The most common type of JSON object has fixed properties that correspond to members of the corresponding
 * Java class. For example, the JSON representation for {@link ClosedRange} has a 'min' and a 'max' property.
 * However, sometimes a JSON object is the representation of a single map, where they keys are not
 * fixed. Examples are maps keyed by {@link JsonTicker}. This lets us expressed in a structured manner
 * what they types are for the keys and values, as opposed to writing it out in free-form text in the documentation. </p>
 *
 * <p> At first, this may look similar to {@link IidMapJsonApiPropertyDescriptor} and
 * {@link RBMapJsonApiPropertyDescriptor}. However, those are for cases where a JSON object has a (fixed) property whose
 * type is a map. Those apply to the property of a JSON object, not to the entire JSON object.
 * Instead, {@link JsonApiClassWithNonFixedPropertiesDocumentation} applies to the entire JSON object. </p>
 *
 * <p> We currently (Sep 2022) never have any JSON objects in our JSON API representations that have both fixed and
 * non-fixed properties. This is because we may not be able to guarantee that a non-fixed property will not collide
 * with a fixed property. </p>
 *
 * @see JsonApiDocumentation
 */
public class JsonApiClassWithNonFixedPropertiesDocumentation extends JsonApiDocumentation {

  private final Class<?> classBeingDocumented;
  private final RBClass<?> keyClass;
  private final RBClass<?> valueClass;
  private final HumanReadableDocumentation singleLineSummary;
  private final HumanReadableDocumentation longDocumentation;
  private final List<HasJsonApiDocumentation> childJsonApiConverters;
  private final Optional<JsonElement> nontrivialSampleJson;

  private JsonApiClassWithNonFixedPropertiesDocumentation(
      Class<?> classBeingDocumented,
      RBClass<?> keyClass,
      RBClass<?> valueClass,
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentation,
      List<HasJsonApiDocumentation> childJsonApiConverters,
      Optional<JsonElement> nontrivialSampleJson) {
    this.classBeingDocumented = classBeingDocumented;
    this.keyClass = keyClass;
    this.valueClass = valueClass;
    this.singleLineSummary = singleLineSummary;
    this.longDocumentation = longDocumentation;
    this.childJsonApiConverters = childJsonApiConverters;
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
   *
   * <p> Note that, unlike {@link #getKeyClass()} and {@link #getValueClass()}, this is a plain {@link Class},
   * not an {@link RBClass}. That's because the JSON API documentation will have a single documentation entry for e.g.
   * {@link Partition}, but not for a {@link Partition} of {@link InstrumentId} or any other specific generic flavor. </p>
   */
  @Override
  public Class<?> getClassBeingDocumented() {
    return classBeingDocumented;
  }

  public RBClass<?> getKeyClass() {
    return keyClass;
  }

  public RBClass<?> getValueClass() {
    return valueClass;
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
   * Returns some non-trivial (i.e. not minimal) sample JSON.
   */
  public Optional<JsonElement> getNontrivialSampleJson() {
    return nontrivialSampleJson;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitJsonApiClassWithNonFixedPropertiesDocumentation(this);
  }

  @Override
  public String toString() {
    return Strings.format("[JACWNFPD %s ( %s -> %s ) %s %s %s ; childConverters: %s ; nonTrivialJson: %s JACWNFPD]",
        classBeingDocumented.getSimpleName(),
        keyClass.toStringWithoutTags(),
        valueClass.toStringWithoutTags(),
        singleLineSummary,
        longDocumentation,
        formatListInExistingOrder(childJsonApiConverters, v -> v.getClass().getSimpleName()),
        formatOptional(nontrivialSampleJson));
  }


  public static class JsonApiClassWithNonFixedPropertiesDocumentationBuilder
      implements RBBuilder<JsonApiClassWithNonFixedPropertiesDocumentation> {

    private Class<?> classBeingDocumented;
    private RBClass<?> keyClass;
    private RBClass<?> valueClass;
    private HumanReadableDocumentation singleLineSummary;
    private HumanReadableDocumentation longDocumentation;
    private List<HasJsonApiDocumentation> childJsonApiConverters;
    private Optional<JsonElement> nontrivialSampleJson;

    private JsonApiClassWithNonFixedPropertiesDocumentationBuilder() {}

    public static JsonApiClassWithNonFixedPropertiesDocumentationBuilder jsonApiClassWithNonFixedPropertiesDocumentationBuilder() {
      return new JsonApiClassWithNonFixedPropertiesDocumentationBuilder();
    }

    /**
     * A convenience constructor for the common case where the 'keyClass' is {@link JsonTicker}, which
     * is used when converting {@link IidMap}s to JSON.
     */
    public static JsonApiClassWithNonFixedPropertiesDocumentationBuilder iidJsonApiClassDocumentationBuilder() {
      return jsonApiClassWithNonFixedPropertiesDocumentationBuilder()
          .setKeyClass(JsonTicker.class);
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setClassBeingDocumented(Class<?> classBeingDocumented) {
      this.classBeingDocumented = checkNotAlreadySet(this.classBeingDocumented, classBeingDocumented);
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setKeyClass(RBClass<?> keyClass) {
      this.keyClass = checkNotAlreadySet(this.keyClass, keyClass);
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setKeyClass(Class<?> keyClass) {
      return setKeyClass(nonGenericRbClass(keyClass));
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setValueClass(RBClass<?> valueClass) {
      this.valueClass = checkNotAlreadySet(this.valueClass, valueClass);
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setValueClass(Class<?> valueClass) {
      return setValueClass(nonGenericRbClass(valueClass));
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setSingleLineSummary(HumanReadableDocumentation singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setLongDocumentation(HumanReadableDocumentation longDocumentation) {
      this.longDocumentation = checkNotAlreadySet(this.longDocumentation, longDocumentation);
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder hasChildJsonApiConverters(
        List<HasJsonApiDocumentation> childJsonApiConverters) {
      this.childJsonApiConverters = checkNotAlreadySet(this.childJsonApiConverters, childJsonApiConverters);
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder hasChildJsonApiConverters(
        HasJsonApiDocumentation first,
        HasJsonApiDocumentation second,
        HasJsonApiDocumentation ... rest) {
      return hasChildJsonApiConverters(concatenateFirstSecondAndRest(first, second, rest));
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder hasSingleChildJsonApiConverter(HasJsonApiDocumentation onlyItem) {
      return hasChildJsonApiConverters(singletonList(onlyItem));
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder hasNoChildJsonApiConverters() {
      return hasChildJsonApiConverters(emptyList());
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder setNontrivialSampleJson(JsonElement nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.of(nontrivialSampleJson));
      return this;
    }

    public JsonApiClassWithNonFixedPropertiesDocumentationBuilder noNontrivialSampleJsonSupplied() {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(classBeingDocumented);
      RBPreconditions.checkNotNull(keyClass);
      RBPreconditions.checkNotNull(valueClass);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(longDocumentation);
      RBPreconditions.checkNotNull(childJsonApiConverters);
      RBPreconditions.checkNotNull(nontrivialSampleJson);

      RBPreconditions.checkArgument(
          !classBeingDocumented.isEnum(),
          "Class %s cannot be an enum! Use JsonApiEnumDocumentation for that case",
          classBeingDocumented);

      // Since the child nodes are 'verb classes', which never implement equals/hashCode (we rarely even do this with
      // data classes), this will check using simple pointer equality. We have it here to prevent mistakes where a
      // JSON API converter Class<?> specifies the same 'child JSON API converter' more than once.
      RBPreconditions.checkUnique(childJsonApiConverters);
    }

    @Override
    public JsonApiClassWithNonFixedPropertiesDocumentation buildWithoutPreconditions() {
      return new JsonApiClassWithNonFixedPropertiesDocumentation(
          classBeingDocumented, keyClass, valueClass,
          singleLineSummary, longDocumentation, childJsonApiConverters, nontrivialSampleJson);
    }

  }

}
