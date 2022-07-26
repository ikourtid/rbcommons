package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.json.JsonValidationInstructions.emptyJsonValidationInstructions;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.formatListInExistingOrder;
import static com.rb.nonbiz.text.Strings.formatOptional;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This is (mostly) human-readable text that explains how a Java object of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 */
public class JsonApiDocumentation {

  private final Class<?> clazz;
  private final HumanReadableLabel singleLineSummary;
  private final String documentationHtml;
  private final JsonValidationInstructions jsonValidationInstructions;
  private final List<HasJsonApiDocumentation> childNodes;
  private final Optional<JsonElement> trivialSampleJson;
  private final Optional<JsonElement> nontrivialSampleJson;

  private JsonApiDocumentation(
      Class<?> clazz,
      HumanReadableLabel singleLineSummary,
      String documentationHtml,
      List<HasJsonApiDocumentation> childNodes,
      JsonValidationInstructions jsonValidationInstructions,
      Optional<JsonElement> trivialSampleJson,
      Optional<JsonElement> nontrivialSampleJson) {
    this.clazz = clazz;
    this.singleLineSummary = singleLineSummary;
    this.documentationHtml = documentationHtml;
    this.jsonValidationInstructions = jsonValidationInstructions;
    this.childNodes = childNodes;
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
  public HumanReadableLabel getSingleLineSummary() {
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
  public List<HasJsonApiDocumentation> getChildNodes() {
    return childNodes;
  }

  /**
   * Returns the human-readable documentation for this JSON API object
   */
  public String getDocumentationHtml() {
    return documentationHtml;
  }

  /**
   * The JSON API often contains {@link JsonElement}s that contain other {@link JsonElement}s etc.
   * in them. This will tell us information about all contained objects.
   */
  public List<HasJsonApiDocumentation> getJsonApiConvertersForContainedObjects() {
    return childNodes;
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
  public String toString() {
    return Strings.format("[JAD %s %s %s %s %s %s %s JAD]",
        clazz,
        singleLineSummary,
        documentationHtml,
        jsonValidationInstructions,
        formatListInExistingOrder(childNodes),
        formatOptional(trivialSampleJson),
        formatOptional(nontrivialSampleJson));
  }


  public static class JsonApiDocumentationBuilder implements RBBuilder<JsonApiDocumentation> {

    private Class<?> clazz;
    private HumanReadableLabel singleLineSummary;
    private String documentationHtml;
    private JsonValidationInstructions jsonValidationInstructions;
    private List<HasJsonApiDocumentation> childNodes;
    private Optional<JsonElement> trivialSampleJson;
    private Optional<JsonElement> nontrivialSampleJson;

    private JsonApiDocumentationBuilder() {}

    public static JsonApiDocumentationBuilder jsonApiDocumentationBuilder() {
      return new JsonApiDocumentationBuilder();
    }

    // FIXME IAK / FIXME SWA JSONDOC: once all JSON API classes get documented, we should remove this.
    public static JsonApiDocumentationBuilder intermediateJsonApiDocumentationBuilder() {
      return new JsonApiDocumentationBuilder()
          .setDocumentationHtml("FIXME IAK / FIXME SWA JSONDOC")
          .noTrivialSampleJsonSupplied()
          .noNontrivialSampleJsonSupplied();
    }

    // FIXME IAK / FIXME SWA JSONDOC: once all JSON API classes get documented, we should remove this.
    // Same as above, but doesn't call .setDocumentationHtml()
    public static JsonApiDocumentationBuilder intermediate2JsonApiDocumentationBuilder() {
      return new JsonApiDocumentationBuilder()
          .noTrivialSampleJsonSupplied()
          .noNontrivialSampleJsonSupplied();
    }

    // FIXME IAK / FIXME SWA JSONDOC: once all JSON API classes get documented, we should remove this.
    public static JsonApiDocumentation intermediateJsonApiDocumentationWithFixme(
        Class<?> clazz,
        HasJsonApiDocumentation ... items) {
      return jsonApiDocumentationBuilder()
          .setClass(clazz)
          .setSingleLineSummary(label("FIXME IAK / FIXME SWA JSONDOC"))
          .setDocumentationHtml("FIXME IAK / FIXME SWA JSONDOC")
          .hasNoJsonValidationInstructions()
          .hasChildNodes(Arrays.asList(items))
          .noTrivialSampleJsonSupplied()
          .noNontrivialSampleJsonSupplied()
          .build();
    }

    public JsonApiDocumentationBuilder setClass(Class<?> clazz) {
      this.clazz = checkNotAlreadySet(this.clazz, clazz);
      return this;
    }

    public JsonApiDocumentationBuilder setSingleLineSummary(HumanReadableLabel singleLineSummary) {
      this.singleLineSummary = checkNotAlreadySet(this.singleLineSummary, singleLineSummary);
      return this;
    }

    public JsonApiDocumentationBuilder setJsonValidationInstructions(
        JsonValidationInstructions jsonValidationInstructions) {
      this.jsonValidationInstructions = checkNotAlreadySet(
          this.jsonValidationInstructions,
          jsonValidationInstructions);
      return this;
    }

    public JsonApiDocumentationBuilder hasNoJsonValidationInstructions() {
      return setJsonValidationInstructions(emptyJsonValidationInstructions());
    }

    public JsonApiDocumentationBuilder setDocumentationHtml(String documentationHtml) {
      this.documentationHtml = checkNotAlreadySet(this.documentationHtml, documentationHtml);
      return this;
    }

    public JsonApiDocumentationBuilder hasChildNodes(List<HasJsonApiDocumentation> childNodes) {
      this.childNodes = checkNotAlreadySet(this.childNodes, childNodes);
      return this;
    }

    public JsonApiDocumentationBuilder hasChildNodes(
        HasJsonApiDocumentation first,
        HasJsonApiDocumentation second,
        HasJsonApiDocumentation ... rest) {
      return hasChildNodes(concatenateFirstSecondAndRest(first, second, rest));
    }

    public JsonApiDocumentationBuilder hasChildNode(HasJsonApiDocumentation onlyItem) {
      return hasChildNodes(singletonList(onlyItem));
    }

    public JsonApiDocumentationBuilder hasNoChildNodes() {
      return hasChildNodes(emptyList());
    }

    public JsonApiDocumentationBuilder setTrivialSampleJson(JsonElement trivialSampleJson) {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.of(trivialSampleJson));
      return this;
    }

    public JsonApiDocumentationBuilder noTrivialSampleJsonSupplied() {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.empty());
      return this;
    }

    public JsonApiDocumentationBuilder setNontrivialSampleJson(JsonElement nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.of(nontrivialSampleJson));
      return this;
    }

    public JsonApiDocumentationBuilder noNontrivialSampleJsonSupplied() {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(clazz);
      RBPreconditions.checkNotNull(singleLineSummary);
      RBPreconditions.checkNotNull(documentationHtml);
      RBPreconditions.checkNotNull(jsonValidationInstructions);
      RBPreconditions.checkNotNull(childNodes);
      RBPreconditions.checkNotNull(trivialSampleJson);
      RBPreconditions.checkNotNull(nontrivialSampleJson);

      // Since the child nodes are 'verb classes', which never implement equals/hashCode (we rarely even do this with
      // data classes), this will check using simple pointer equality. We have it here to prevent mistakes where a
      // JSON API converter Class<?> specifies the same 'child JSON API converter' more than once.
      RBPreconditions.checkUnique(childNodes);
    }

    @Override
    public JsonApiDocumentation buildWithoutPreconditions() {
      return new JsonApiDocumentation(
          clazz, singleLineSummary, documentationHtml, childNodes, jsonValidationInstructions,
          trivialSampleJson, nontrivialSampleJson);
    }

  }

}
