package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.jsonapi.JsonApiDocumentation.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.jsonapi.JsonApiArrayDocumentationTest.jsonApiArrayDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentationTest.jsonApiClassDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentationTest.jsonApiClassWithSubclassesDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiEnumDocumentationTest.jsonApiEnumDocumentationMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class JsonApiDocumentationTest extends RBTestMatcher<JsonApiDocumentation> {

  @Override
  public JsonApiDocumentation makeTrivialObject() {
    return new JsonApiClassDocumentationTest().makeTrivialObject();
  }

  @Override
  public JsonApiDocumentation makeNontrivialObject() {
    return new JsonApiClassDocumentationTest().makeNontrivialObject();
  }

  @Override
  public JsonApiDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return new JsonApiClassDocumentationTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(JsonApiDocumentation expected, JsonApiDocumentation actual) {
    return jsonApiDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiDocumentation> jsonApiDocumentationMatcher(JsonApiDocumentation expected) {
    return generalVisitorMatcher(expected, v -> v.visit(new Visitor<VisitorMatchInfo<JsonApiDocumentation>>() {
      @Override
      public VisitorMatchInfo<JsonApiDocumentation> visitJsonApiClassDocumentation(
          JsonApiClassDocumentation jsonApiClassDocumentation) {
        return visitorMatchInfo(1, jsonApiClassDocumentation,
            (MatcherGenerator<JsonApiClassDocumentation>) f -> jsonApiClassDocumentationMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiDocumentation> visitJsonApiEnumDocumentation(
          JsonApiEnumDocumentation<? extends Enum<?>> jsonApiEnumDocumentation) {
        return visitorMatchInfo(2, jsonApiEnumDocumentation,
            (MatcherGenerator<JsonApiEnumDocumentation<? extends Enum<?>>>) f -> jsonApiEnumDocumentationMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiDocumentation> visitJsonApiClassWithSubclassesDocumentation(
          JsonApiClassWithSubclassesDocumentation jsonApiClassWithSubclassesDocumentation) {
        return visitorMatchInfo(3, jsonApiClassWithSubclassesDocumentation,
            (MatcherGenerator<JsonApiClassWithSubclassesDocumentation>)
                f -> jsonApiClassWithSubclassesDocumentationMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiDocumentation> visitJsonApiArrayDocumentation(
          JsonApiArrayDocumentation jsonApiArrayDocumentation) {
        return visitorMatchInfo(4, jsonApiArrayDocumentation,
            (MatcherGenerator<JsonApiArrayDocumentation>)
                f -> jsonApiArrayDocumentationMatcher(f));
      }

    }));
  }

}
