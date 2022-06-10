package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Optional;

// FIXME IAK JSONDOC
public interface HasJsonDocumentation {

  String getDocumentationString();

  /**
   * Returns some trivial (i.e. minimal) sample JSON.
   */
  default Optional<JsonObject> getTrivialSampleJson() {
    return Optional.empty();
  }

  /**
   * Returns some non-trivial (i.e. not minimal) sample JSON.
   */
  default Optional<JsonObject> getNontrivialSampleJson() {
    return Optional.empty();
  }

  List<Class<? extends HasJsonDocumentation>> getContainedObjects();

}
