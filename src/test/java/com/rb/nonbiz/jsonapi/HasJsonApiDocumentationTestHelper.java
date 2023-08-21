package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.RBGson;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation.Visitor;
import com.rb.nonbiz.jsonapi.doc.AllPropertiesMentionedInSingleDocumentationStringLister;
import com.rb.nonbiz.jsonapi.doc.RecursiveJsonApiDocumentationLister;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSets.difference;
import static com.rb.nonbiz.collections.RBSets.union;
import static com.rb.nonbiz.collections.RBSets.unionOfRBSets;
import static com.rb.nonbiz.json.JsonValidationInstructions.emptyJsonValidationInstructions;
import static com.rb.nonbiz.json.RBGson.toPrettyJson;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;

public class HasJsonApiDocumentationTestHelper<T> {

  private HasJsonApiDocumentationTestHelper() {}

  public static <T> HasJsonApiDocumentationTestHelper<T> hasJsonApiDocumentationTestHelper() {
    return new HasJsonApiDocumentationTestHelper<>();
  }

  public void runAllTests(
      HasJsonApiDocumentation hasJsonApiDocumentation,
      Function<JsonObject, T> deserializer) {
    testValidSampleJson(hasJsonApiDocumentation, deserializer);
    testAllPropertiesMentionedInDoc(hasJsonApiDocumentation);
    testPropertiesMentionedAreValid(hasJsonApiDocumentation);
  }

  public void runAllTestsExceptForValidSampleJson(HasJsonApiDocumentation hasJsonApiDocumentation) {
    testAllPropertiesMentionedInDoc(hasJsonApiDocumentation);
    testPropertiesMentionedAreValid(hasJsonApiDocumentation);
  }

  // Check that the "sample" JSON element in the JsonApiDocumentation can be transformed
  // to a valid Java object via #fromJsonObject.
  // We don't want to display any JSON that can't be converted.
  public void testValidSampleJson(
      HasJsonApiDocumentation hasJsonApiDocumentation,
      Function<JsonObject, T> deserializer) {
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

  // Get the set of all properties listed in the JsonApiDocumentation.
  // Properties are labelled as <b>property</b> in the documentation lines.
  // We call this 'topLevel' documentation, in contrast with the recursive list of documentation used to
  // search for valid properties.
  // Make this method 'public static' so that test classes that don't extend JsonRoundTripConverter can access it.
  public void testPropertiesMentionedAreValid(HasJsonApiDocumentation hasJsonApiDocumentation) {
    JsonApiDocumentation topLevelJsonApiDocumentation = hasJsonApiDocumentation.getJsonApiDocumentation();

    AllPropertiesMentionedInSingleDocumentationStringLister allPropertiesMentionedInSingleDocumentationStringLister
        = makeRealObject(AllPropertiesMentionedInSingleDocumentationStringLister.class);
    RBSet<String> topLevelPropertiesInDocumentation = union(
        allPropertiesMentionedInSingleDocumentationStringLister.listAll(
            topLevelJsonApiDocumentation.getSingleLineSummary()),
        allPropertiesMentionedInSingleDocumentationStringLister.listAll(
            topLevelJsonApiDocumentation.getLongDocumentation()));

    // Get the (recursive) set of all JsonValidationInstructions, which together hold all the properties
    // that could reasonably be mentioned in the documentation for this class.
    List<JsonValidationInstructions> allRecursiveJsonValidationInstructions =
        makeRealObject(RecursiveJsonApiDocumentationLister.class).list(topLevelJsonApiDocumentation)
            .stream()
            .map(jsonApiDocumentation -> getOptionalJsonValidationInstructions(jsonApiDocumentation))
            .filter(v -> v.isPresent())
            .map(   v -> v.get())
            .collect(Collectors.toList());

    // Extract the required and optional properties from all the (recursive) JsonValidationInstructions.
    RBSet<String> allRecursiveProperties = union(
        allRecursiveJsonValidationInstructions
            .stream()
            .map(v -> v.getAllProperties())
            .iterator());

    // Look for "top-level" properties that don't exist in any of the recursive properties.
    // Note: difference(set1, set2) finds the elements in set1 that are not in set2.
    RBSet<String> invalidPropertiesFound = difference(topLevelPropertiesInDocumentation, allRecursiveProperties);
    RBPreconditions.checkArgument(
        invalidPropertiesFound.isEmpty(),
        "In JSON documentation for %s, %s properties are invalid: %s (valid: %s)",
        topLevelJsonApiDocumentation.getClassBeingDocumented().getSimpleName(),
        invalidPropertiesFound.size(), invalidPropertiesFound, allRecursiveProperties);
  }

  // Check that all the top-level (not recursive) properties were in fact mentioned
  // in the documentation at least once each.
  public void testAllPropertiesMentionedInDoc(HasJsonApiDocumentation hasJsonApiDocumentation) {
    JsonApiDocumentation jsonApiDocumentation = hasJsonApiDocumentation.getJsonApiDocumentation();

    // Find all the required and optional properties for this converter, as found in the
    // (optional) JsonValidationInstructions. Of course, if there is no JVI, this check won't work.
    JsonValidationInstructions jsonValidationInstructions =
        getOptionalJsonValidationInstructions(jsonApiDocumentation).orElse(
            emptyJsonValidationInstructions());
    // Make sure that both required and optional properties are mentioned. Alternatively, we could just
    // check for required properties.
    RBSet<String> propertiesThatShouldBeMentioned = jsonValidationInstructions.getAllProperties();

    // Find all the properties mentioned in the class documentation, in 3 places:
    // 1) the 'SingleSummaryLine'
    // 2) the 'LongDocumentation'
    // 3) the 'property' descriptions
    AllPropertiesMentionedInSingleDocumentationStringLister allPropertiesMentionedInSingleDocumentationStringLister =
        makeRealObject(AllPropertiesMentionedInSingleDocumentationStringLister.class);
    RBSet<String> propertiesMentionedInDocumentation = unionOfRBSets(
        allPropertiesMentionedInSingleDocumentationStringLister.listAll(
            jsonApiDocumentation.getSingleLineSummary()),
        allPropertiesMentionedInSingleDocumentationStringLister.listAll(
            jsonApiDocumentation.getLongDocumentation()),
        // properties in the JVI can also have documentation
        getDocumentedProperties(jsonValidationInstructions.getRequiredProperties()),
        getDocumentedProperties(jsonValidationInstructions.getOptionalProperties()));

    // Look for any JVI properties that weren't mentioned in the documentation.
    RBSet<String> unmentionedProperties = difference(
        propertiesThatShouldBeMentioned,
        propertiesMentionedInDocumentation);
    RBPreconditions.checkArgument(
        unmentionedProperties.isEmpty(),
        "In the JSON API documentation for %s, %s of %s top-level properties were not mentioned: %s",
        jsonApiDocumentation.getClassBeingDocumented().getSimpleName(),
        unmentionedProperties.size(), propertiesThatShouldBeMentioned.size(),
        unmentionedProperties);
  }

  // Return the names of any properties that have documentation.
  private RBSet<String> getDocumentedProperties(
      RBMap<String, JsonApiPropertyDescriptor> jsonApiPropertyDescriptorRBMap) {
    return rbSet(jsonApiPropertyDescriptorRBMap.entrySet()
        .stream()
        .filter(entry -> entry.getValue().getPropertySpecificDocumentation().isPresent())
        .map(entry -> entry.getKey())
        .collect(Collectors.toSet()));
  }

  // Get the (optional) JsonValidationInstructions for a JsonApiDocumentation. There is no need to look recursively;
  // we are calling this method with a recursive list of JsonApiDocumentations.
  private Optional<JsonValidationInstructions> getOptionalJsonValidationInstructions(
      JsonApiDocumentation jsonApiDocumentation) {
    return jsonApiDocumentation.visit(new Visitor<Optional<JsonValidationInstructions>>() {

      @Override
      public Optional<JsonValidationInstructions> visitJsonApiClassDocumentation(
          JsonApiClassDocumentation jsonApiClassDocumentation) {
        return Optional.of(jsonApiClassDocumentation.getJsonValidationInstructions());
      }

      @Override
      public Optional<JsonValidationInstructions> visitJsonApiEnumDocumentation(
          JsonApiEnumDocumentation<? extends Enum<?>> ignored) {
        return Optional.empty();
      }

      @Override
      public Optional<JsonValidationInstructions> visitJsonApiClassWithSubclassesDocumentation(
          JsonApiClassWithSubclassesDocumentation ignored) {
        return Optional.empty();
      }

      @Override
      public Optional<JsonValidationInstructions> visitJsonApiArrayDocumentation(
          JsonApiArrayDocumentation ignored) {
        return Optional.empty();
      }

      @Override
      public Optional<JsonValidationInstructions> visitJsonApiClassWithNonFixedPropertiesDocumentation(
          JsonApiClassWithNonFixedPropertiesDocumentation ignored) {
        return Optional.empty();
      }
    });

  }

}
