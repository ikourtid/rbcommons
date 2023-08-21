package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.json.RBGson;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation.Visitor;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;

import static com.rb.nonbiz.json.RBGson.toPrettyJson;

public class HasJsonApiDocumentationTestHelper<T> {

  private final HasJsonApiDocumentation hasJsonApiDocumentation;
  private final Function<JsonObject, T> deserializer;

  private HasJsonApiDocumentationTestHelper(
      HasJsonApiDocumentation hasJsonApiDocumentation,
      Function<JsonObject, T> deserializer) {
    this.hasJsonApiDocumentation = hasJsonApiDocumentation;
    this.deserializer = deserializer;
  }

  public static <T> HasJsonApiDocumentationTestHelper<T> hasJsonApiDocumentationTestHelper(
      HasJsonApiDocumentation hasJsonApiDocumentation,
      Function<JsonObject, T> deserializer) {
    return new HasJsonApiDocumentationTestHelper<>(hasJsonApiDocumentation, deserializer);
  }

  // Check that the "sample" JSON element in the JsonApiDocumentation can be transformed
  // to a valid Java object via #fromJsonObject.
  // We don't want to display any JSON that can't be converted.
  public void testValidSampleJson() {
    // Not all JsonApiClassDocumentation subclasses have sample JSON, so we return Optional<JsonElement>
    // so that classes without can return Optional.empty() and not have the (non-existent) sample JSON checked
    // for validity further on.
    // However, this creates a potential problem. Classes that should have a sample JSON, but do not, will
    // also return Optional.empty(). They would also skip the "valid JSON" check. To solve this, we check
    // that classes that should have a sample JSON object actually do. That way, they won't return Optional.empty(),
    // and therefore will have their sample JSON checked for validity.
    Optional<JsonElement> maybeSampleJson = hasJsonApiDocumentation.getJsonApiDocumentation().visit(
        new Visitor<Optional<JsonElement>>() {
          @Override
          public Optional<JsonElement> visitJsonApiClassDocumentation(
              JsonApiClassDocumentation jsonApiClassDocumentation) {
            checkOptionalSampleJsonIsPresent(jsonApiClassDocumentation.getNontrivialSampleJson());
            return jsonApiClassDocumentation.getNontrivialSampleJson();
          }

          @Override
          public Optional<JsonElement> visitJsonApiEnumDocumentation(
              JsonApiEnumDocumentation<? extends Enum<?>> ignored) {
            return Optional.empty();
          }

          @Override
          public Optional<JsonElement> visitJsonApiClassWithSubclassesDocumentation(
              JsonApiClassWithSubclassesDocumentation jsonApiClassWithSubclassesDocumentation) {
            // make sure that this class has a sample JSON object, and that it's not empty
            checkOptionalSampleJsonIsPresent(jsonApiClassWithSubclassesDocumentation.getNontrivialSampleJson());
            return jsonApiClassWithSubclassesDocumentation.getNontrivialSampleJson();
          }

          @Override
          public Optional<JsonElement> visitJsonApiArrayDocumentation(
              JsonApiArrayDocumentation ignored) {
            // JsonApiArrayDocumentation does have sample JSON, but it is in the form of a JsonArray.
            // We could cast this to a JsonElement, but then that would have to be cast to a JsonObject below,
            // which wouldn't work.
            return Optional.empty();
          }

          @Override
          public Optional<JsonElement> visitJsonApiClassWithNonFixedPropertiesDocumentation(
              JsonApiClassWithNonFixedPropertiesDocumentation jsonApiClassWithNonFixedPropertiesDocumentation) {
            // make sure that this class has a sample JSON object, and that it's not empty
            checkOptionalSampleJsonIsPresent(jsonApiClassWithNonFixedPropertiesDocumentation.getNontrivialSampleJson());
            return jsonApiClassWithNonFixedPropertiesDocumentation.getNontrivialSampleJson();
          }
        });

    // Not all JsonApiClassDocumentation subclasses have sample JSON.
    if (maybeSampleJson.isPresent()) {
      JsonObject sampleJson = maybeSampleJson.get().getAsJsonObject();
      // For those subclasses that do have a sample JSON, check that it can be  converted by fromJsonObject().
      try {
        T doesNotThrow = deserializer.apply(sampleJson);
      } catch (Exception e) {
        throw new RuntimeException(
            Strings.format(
                "deserialization cannot successfully process the following sample JSON object: %s",
                toPrettyJson(sampleJson)),
            e);
      }
    }
  }

  // Check that a class that should have a sample JSON actually has one, and that it is not empty.
  private void checkOptionalSampleJsonIsPresent(Optional<JsonElement> maybeSampleJson) {
    RBPreconditions.checkArgument(
        maybeSampleJson.isPresent() && !RBGson.isEmpty(maybeSampleJson.get().getAsJsonObject()),
        "A non-empty sample JSON must be provided, but found: %s",
        maybeSampleJson);
  }

}
