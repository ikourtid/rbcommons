package com.rb.nonbiz.jsonapi.doc;

import com.google.common.collect.ImmutableList;
import com.rb.biz.jsonapi.JsonTicker;
import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.FlatSignedLinearCombination;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.JsonValidationInstructionsTest;
import com.rb.nonbiz.jsonapi.HasJsonApiDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiArrayDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiClassDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiClassWithNonFixedPropertiesDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentation;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.jsonapi.JsonApiArrayDocumentation.JsonApiArrayDocumentationBuilder.jsonApiArrayDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentationTest.testJsonApiClassDocumentationWithSeed;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithNonFixedPropertiesDocumentation.JsonApiClassWithNonFixedPropertiesDocumentationBuilder.jsonApiClassWithNonFixedPropertiesDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentation.JsonApiClassWithSubclassesDocumentationBuilder.jsonApiClassWithSubclassesDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentationTest.jsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiSubclassInfo.JsonApiSubclassInfoBuilder.jsonApiSubclassInfoBuilder;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.unorderedListMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecursiveJsonApiDocumentationListerTest extends RBTest<RecursiveJsonApiDocumentationLister> {

  @Test
  public void recursivelyEnumeratesAllJsonApiDocumentation_simpleClass() {
    JsonApiClassDocumentation leafNode =
        makeJsonApiClassDocumentation(AssetId.class, emptyList());
    JsonApiClassDocumentation additionalDocumentationForIntermediateNode =
        makeJsonApiClassDocumentation(Double.class, emptyList());
    JsonApiClassDocumentation intermediateNode =
        makeJsonApiClassDocumentation(FlatSignedLinearCombination.class, singletonList(
            new HasJsonApiDocumentation() {
              @Override
              public JsonApiDocumentation getJsonApiDocumentation() {
                return leafNode;
              }

              @Override
              public Optional<RBSet<JsonApiDocumentation>> getAdditionalJsonApiDocumentation() {
                return Optional.of(singletonRBSet(additionalDocumentationForIntermediateNode));
              }
            }));
    JsonApiClassDocumentation rootNode =
        // lambda shorthand for a HasJsonApiDocumentation
        makeJsonApiClassDocumentation(ClosedRange.class, singletonList( () -> intermediateNode));

    assertThat(
        makeTestObject().list(rootNode),
        orderedListMatcher(
            ImmutableList.of(rootNode, intermediateNode, additionalDocumentationForIntermediateNode, leafNode),
            f -> jsonApiDocumentationMatcher(f)));
  }

  @Test
  public void recursivelyEnumeratesAllJsonApiDocumentation_classWithSubclasses() {
    // Trying to make this test somewhat realistic, using the fact that CashId and InstrumentId both derive from AssetId.
    JsonApiClassDocumentation cashIdNode =
        testJsonApiClassDocumentationWithSeed(CashId.class, DUMMY_STRING);
    JsonApiClassDocumentation instrumentIdNode =
        testJsonApiClassDocumentationWithSeed(InstrumentId.class, DUMMY_STRING);

    JsonApiClassWithSubclassesDocumentation assetIdNode = jsonApiClassWithSubclassesDocumentationBuilder()
        .setClassBeingDocumented(AssetId.class)
        .setSingleLineSummary(documentation(DUMMY_STRING))
        .setLongDocumentation(documentation(DUMMY_STRING))
        .setJsonApiInfoOnMultipleSubclasses(
            jsonApiSubclassInfoBuilder()
                .setClassOfSubclass(CashId.class)
                .setDiscriminatorPropertyValue("cashId")
                .setJsonApiConverterForTraversing( () -> cashIdNode)
                .build(),
            jsonApiSubclassInfoBuilder()
                .setClassOfSubclass(InstrumentId.class)
                .setDiscriminatorPropertyValue("instrumentId")
                .setJsonApiConverterForTraversing( () -> instrumentIdNode)
                .build())
        .setDiscriminatorProperty(DUMMY_STRING)
        .noNontrivialSampleJsonSupplied()
        .build();
    assertThat(
        makeTestObject().list(assetIdNode),
        unorderedListMatcher(
            ImmutableList.of(assetIdNode, cashIdNode, instrumentIdNode),
            f -> jsonApiDocumentationMatcher(f),
            comparing(v -> v.getClassBeingDocumented().getSimpleName()))); // any fixed comparison will do here
  }

  @Test
  public void recursivelyEnumeratesAllJsonApiDocumentation_jsonArray() {
    JsonApiClassDocumentation leafNode =
        makeJsonApiClassDocumentation(AssetId.class, emptyList());
    JsonApiClassDocumentation additionalDocumentationForIntermediateNode =
        makeJsonApiClassDocumentation(Double.class, emptyList());
    JsonApiClassDocumentation intermediateNode =
        makeJsonApiClassDocumentation(FlatSignedLinearCombination.class, singletonList(
            new HasJsonApiDocumentation() {
              @Override
              public JsonApiDocumentation getJsonApiDocumentation() {
                return leafNode;
              }

              @Override
              public Optional<RBSet<JsonApiDocumentation>> getAdditionalJsonApiDocumentation() {
                return Optional.of(singletonRBSet(additionalDocumentationForIntermediateNode));
              }
            }));
    JsonApiArrayDocumentation rootNode = jsonApiArrayDocumentationBuilder()
        .setClassBeingDocumented(ClosedRange.class)
        // We'll keep it the same as the JSON API converter 'intermediateNode' to make the test more realistic,
        // although it doesn't matter in this test.
        .setClassOfArrayItems(FlatSignedLinearCombination.class)
        .setSingleLineSummary(documentation("s"))
        .setLongDocumentation(documentation("l"))
        .hasJsonApiConverter( () -> intermediateNode)
        .hasNoNontrivialSampleJson()
        .build();

    assertThat(
        makeTestObject().list(rootNode),
        orderedListMatcher(
            ImmutableList.of(rootNode, intermediateNode, additionalDocumentationForIntermediateNode, leafNode),
            f -> jsonApiDocumentationMatcher(f)));
  }

  @Test
  public void recursivelyEnumeratesAllJsonApiDocumentation_classWithNonFixedProperties() {
    JsonApiClassWithNonFixedPropertiesDocumentation leafNode =
        makeJsonApiClassWithNonFixedPropertiesDocumentation(AssetId.class, emptyList());
    JsonApiClassWithNonFixedPropertiesDocumentation additionalDocumentationForIntermediateNode =
        makeJsonApiClassWithNonFixedPropertiesDocumentation(Double.class, emptyList());
    JsonApiClassWithNonFixedPropertiesDocumentation intermediateNode =
        makeJsonApiClassWithNonFixedPropertiesDocumentation(FlatSignedLinearCombination.class, singletonList(
            new HasJsonApiDocumentation() {
              @Override
              public JsonApiDocumentation getJsonApiDocumentation() {
                return leafNode;
              }

              @Override
              public Optional<RBSet<JsonApiDocumentation>> getAdditionalJsonApiDocumentation() {
                return Optional.of(singletonRBSet(additionalDocumentationForIntermediateNode));
              }
            }));
    JsonApiClassWithNonFixedPropertiesDocumentation rootNode =
        // lambda shorthand for a HasJsonApiDocumentation
        makeJsonApiClassWithNonFixedPropertiesDocumentation(ClosedRange.class, singletonList( () -> intermediateNode));

    assertThat(
        makeTestObject().list(rootNode),
        orderedListMatcher(
            ImmutableList.of(rootNode, intermediateNode, additionalDocumentationForIntermediateNode, leafNode),
            f -> jsonApiDocumentationMatcher(f)));
  }

  private JsonApiClassDocumentation makeJsonApiClassDocumentation(
      Class<?> clazz, List<HasJsonApiDocumentation> childJsonApiConverters) {
    return jsonApiClassDocumentationBuilder()
        .setClass(clazz)
        .setSingleLineSummary(documentation(DUMMY_STRING))
        .setLongDocumentation(documentation(DUMMY_STRING))
        .setJsonValidationInstructions(new JsonValidationInstructionsTest().makeDummyObject())
        .hasChildJsonApiConverters(childJsonApiConverters)
        .noNontrivialSampleJsonSupplied()
        .build();
  }

  private JsonApiClassWithNonFixedPropertiesDocumentation makeJsonApiClassWithNonFixedPropertiesDocumentation(
      Class<?> classBeingDocumented, List<HasJsonApiDocumentation> childJsonApiConverters) {
    return jsonApiClassWithNonFixedPropertiesDocumentationBuilder()
        .setClassBeingDocumented(classBeingDocumented)
        .setKeyClass(JsonTicker.class) // dummy
        .setValueClass(UnitFraction.class) // dummy
        .setSingleLineSummary(documentation(DUMMY_STRING))
        .setLongDocumentation(documentation(DUMMY_STRING))
        .hasChildJsonApiConverters(childJsonApiConverters)
        .noNontrivialSampleJsonSupplied()
        .build();
  }

  @Override
  protected RecursiveJsonApiDocumentationLister makeTestObject() {
    return new RecursiveJsonApiDocumentationLister();
  }

}
