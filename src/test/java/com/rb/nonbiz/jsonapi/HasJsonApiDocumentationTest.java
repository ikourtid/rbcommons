package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.RBSet;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentation.getAllJsonApiDocumentation;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentationTest.jsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentationTest.testJsonApiDocumentationWithSeed;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static org.junit.Assert.assertEquals;

public class HasJsonApiDocumentationTest {

  private final JsonApiDocumentation docA = testJsonApiDocumentationWithSeed(AssetId.class,     "a");
  private final JsonApiDocumentation docB = testJsonApiDocumentationWithSeed(BigDecimal.class,  "b");
  private final JsonApiDocumentation docC = testJsonApiDocumentationWithSeed(ClosedRange.class, "c");

  @Test
  public void testDefaultMethods_noAdditionalDocumentation() {
    rbSetOf(
        new HasJsonApiDocumentation() {
          @Override
          public JsonApiDocumentation getJsonApiDocumentation() {
            return docA;
          }
        },
        new HasJsonApiDocumentation() {
          @Override
          public JsonApiDocumentation getJsonApiDocumentation() {
            return docA;
          }

          @Override
          public Optional<RBSet<JsonApiDocumentation>> getAdditionalJsonApiDocumentation() {
            return Optional.empty();
          }
        })
        .forEach(hasJsonApiDocumentation -> {
          assertOptionalEmpty(hasJsonApiDocumentation.getAdditionalJsonApiDocumentation());
          assertEquals(singletonRBSet(docA), getAllJsonApiDocumentation(hasJsonApiDocumentation));
        });
  }

  @Test
  public void testDefaultMethods_noAdditionalDocumentation_usesOverrideThatReturnsNoAdditionalDocumentation() {
    HasJsonApiDocumentation hasJsonApiDocumentation = new HasJsonApiDocumentation() {
      @Override
      public JsonApiDocumentation getJsonApiDocumentation() {
        return docA;
      }

      @Override
      public Optional<RBSet<JsonApiDocumentation>> getAdditionalJsonApiDocumentation() {
        return Optional.of(rbSetOf(docB, docC));
      }
    };

    assertOptionalEquals(
        rbSetOf(docB, docC),
        hasJsonApiDocumentation.getAdditionalJsonApiDocumentation());
    assertEquals(
        rbSetOf(docA, docB, docC),
        getAllJsonApiDocumentation(hasJsonApiDocumentation));
  }

  public static TypeSafeMatcher<HasJsonApiDocumentation> hasJsonApiDocumentationMatcher(
      HasJsonApiDocumentation expected) {
    return makeMatcher(expected,
        match(v -> v.getJsonApiDocumentation(), f -> jsonApiDocumentationMatcher(f)));
  }

}
