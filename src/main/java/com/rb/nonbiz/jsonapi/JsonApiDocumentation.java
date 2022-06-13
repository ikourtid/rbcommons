package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This is (mostly) human-readable text that explains how a Java object of this type
 * will get converted into JSON. It's useful mostly to 3rd party developers.
 */
public class JsonApiDocumentation<T> {

  private final String documentationHtml;
  private final List<HasJsonApiDocumentation<?>> childNodes;
  private final Optional<JsonObject> trivialSampleJson;
  private final Optional<JsonObject> nontrivialSampleJson;

  private JsonApiDocumentation(
      String documentationHtml,
      List<HasJsonApiDocumentation<?>> childNodes,
      Optional<JsonObject> trivialSampleJson,
      Optional<JsonObject> nontrivialSampleJson) {
    this.documentationHtml = documentationHtml;
    this.childNodes = childNodes;
    this.trivialSampleJson = trivialSampleJson;
    this.nontrivialSampleJson = nontrivialSampleJson;
  }

  /**
   * Returns the human-readable documentation for this JSON API object
   */
  public String getDocumentationHtml() {
    return documentationHtml;
  }

  /**
   * The JSON API often contains {@link JsonObject}s that contain other {@link JsonObject}s etc.
   * in them. This will tell us information about all contained objects.
   */
  public List<HasJsonApiDocumentation<?>> getJsonApiConvertersForContainedObjects() {
    return childNodes;
  }

  /**
   * Returns some trivial (i.e. minimal) sample JSON.
   */
  public Optional<JsonObject> getTrivialSampleJson() {
    return trivialSampleJson;
  }

  /**
   * Returns some non-trivial (i.e. not minimal) sample JSON.
   */
  Optional<JsonObject> getNontrivialSampleJson() {
    return nontrivialSampleJson;
  }

  @Override
  public String toString() {
    return super.toString();
  }


  public static class JsonApiDocumentationBuilder<T> implements RBBuilder<JsonApiDocumentation<T>> {

    private String documentationHtml;
    private List<HasJsonApiDocumentation<?>> childNodes;
    private Optional<JsonObject> trivialSampleJson;
    private Optional<JsonObject> nontrivialSampleJson;

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(documentationHtml);
      RBPreconditions.checkNotNull(childNodes);
      RBPreconditions.checkNotNull(trivialSampleJson);
      RBPreconditions.checkNotNull(nontrivialSampleJson);
    }

    private JsonApiDocumentationBuilder() {}

    public static <T> JsonApiDocumentationBuilder<T> jsonApiDocumentationBuilder() {
      return new JsonApiDocumentationBuilder<>();
    }

    public JsonApiDocumentationBuilder<T> setDocumentationHtml(String documentationHtml) {
      this.documentationHtml = checkNotAlreadySet(this.documentationHtml, documentationHtml);
      return this;
    }

    public JsonApiDocumentationBuilder<T> hasChildNodes(List<HasJsonApiDocumentation<?>> childNodes) {
      this.childNodes = checkNotAlreadySet(this.childNodes, childNodes);
      return this;
    }


    public JsonApiDocumentationBuilder<T> hasChildNodes(
        HasJsonApiDocumentation<?> first,
        HasJsonApiDocumentation<?> second,
        HasJsonApiDocumentation<?> ... rest) {
      return hasChildNodes(concatenateFirstSecondAndRest(first, second, rest));
    }

    public JsonApiDocumentationBuilder<T> hasChildNode(HasJsonApiDocumentation<?> onlyItem) {
      return hasChildNodes(singletonList(onlyItem));
    }

    public JsonApiDocumentationBuilder<T> hasNoChildNodes() {
      return hasChildNodes(emptyList());
    }

    public JsonApiDocumentationBuilder<T> setTrivialSampleJson(JsonObject trivialSampleJson) {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.of(trivialSampleJson));
      return this;
    }

    public JsonApiDocumentationBuilder<T> noTrivialSampleJsonSupplied() {
      this.trivialSampleJson = checkNotAlreadySet(this.trivialSampleJson, Optional.empty());
      return this;
    }

    public JsonApiDocumentationBuilder<T> setNontrivialSampleJson(JsonObject nontrivialSampleJson) {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.of(nontrivialSampleJson));
      return this;
    }

    public JsonApiDocumentationBuilder<T> noNontrivialSampleJsonSupplied() {
      this.nontrivialSampleJson = checkNotAlreadySet(this.nontrivialSampleJson, Optional.empty());
      return this;
    }
    
    @Override
    public JsonApiDocumentation<T> buildWithoutPreconditions() {
      return new JsonApiDocumentation<>(documentationHtml, childNodes, trivialSampleJson, nontrivialSampleJson);
    }

  }

}
