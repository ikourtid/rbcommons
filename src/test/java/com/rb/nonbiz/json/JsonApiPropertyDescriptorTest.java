package com.rb.nonbiz.json;

import com.rb.nonbiz.json.JsonApiPropertyDescriptor.CollectionJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.IidMapJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaGenericJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.PseudoEnumJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.RBMapJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.CollectionJsonApiPropertyDescriptorTest.collectionJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.IidMapJsonApiPropertyDescriptorTest.iidMapJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.JavaEnumJsonApiPropertyDescriptorTest.javaEnumJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.JavaGenericJsonApiPropertyDescriptorTest.javaGenericJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.PseudoEnumJsonApiPropertyDescriptorTest.pseudoEnumJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.RBMapJsonApiPropertyDescriptorTest.rbMapJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.SimpleClassJsonApiPropertyDescriptorTest.simpleClassJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class JsonApiPropertyDescriptorTest extends RBTestMatcher<JsonApiPropertyDescriptor> {

  @Override
  public JsonApiPropertyDescriptor makeTrivialObject() {
    return new SimpleClassJsonApiPropertyDescriptorTest().makeTrivialObject();
  }

  @Override
  public JsonApiPropertyDescriptor makeNontrivialObject() {
    return new RBMapJsonApiPropertyDescriptorTest().makeNontrivialObject();
  }

  @Override
  public JsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    return new RBMapJsonApiPropertyDescriptorTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(JsonApiPropertyDescriptor expected, JsonApiPropertyDescriptor actual) {
    return dataClassJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiPropertyDescriptor> dataClassJsonApiPropertyDescriptorMatcher(
      JsonApiPropertyDescriptor expected) {
    return generalVisitorMatcher(expected, v -> v.visit(new Visitor<VisitorMatchInfo<JsonApiPropertyDescriptor>>() {
      @Override
      public VisitorMatchInfo<JsonApiPropertyDescriptor> visitSimpleClassJsonApiPropertyDescriptor(
          SimpleClassJsonApiPropertyDescriptor simpleClassJsonApiPropertyDescriptor) {
        return visitorMatchInfo(1, simpleClassJsonApiPropertyDescriptor,
            (MatcherGenerator<SimpleClassJsonApiPropertyDescriptor>) f -> simpleClassJsonApiPropertyDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiPropertyDescriptor> visitIidMapJsonApiPropertyDescriptor(
          IidMapJsonApiPropertyDescriptor iidMapJsonApiPropertyDescriptor) {
        return visitorMatchInfo(2, iidMapJsonApiPropertyDescriptor,
            (MatcherGenerator<IidMapJsonApiPropertyDescriptor>) f -> iidMapJsonApiPropertyDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiPropertyDescriptor> visitRBMapJsonApiPropertyDescriptor(
          RBMapJsonApiPropertyDescriptor rbMapJsonApiPropertyDescriptor) {
        return visitorMatchInfo(3, rbMapJsonApiPropertyDescriptor,
            (MatcherGenerator<RBMapJsonApiPropertyDescriptor>) f -> rbMapJsonApiPropertyDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiPropertyDescriptor> visitCollectionJsonApiPropertyDescriptor(
          CollectionJsonApiPropertyDescriptor collectionJsonApiPropertyDescriptor) {
        return visitorMatchInfo(4, collectionJsonApiPropertyDescriptor,
            (MatcherGenerator<CollectionJsonApiPropertyDescriptor>) f -> collectionJsonApiPropertyDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiPropertyDescriptor> visitJavaGenericJsonApiPropertyDescriptor(
          JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor) {
        return visitorMatchInfo(5, javaGenericJsonApiPropertyDescriptor,
            (MatcherGenerator<JavaGenericJsonApiPropertyDescriptor>) f -> javaGenericJsonApiPropertyDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<JsonApiPropertyDescriptor> visitPseudoEnumJsonApiPropertyDescriptor(
          PseudoEnumJsonApiPropertyDescriptor pseudoEnumJsonApiPropertyDescriptor) {
        return visitorMatchInfo(6, pseudoEnumJsonApiPropertyDescriptor,
            (MatcherGenerator<PseudoEnumJsonApiPropertyDescriptor>) f -> pseudoEnumJsonApiPropertyDescriptorMatcher(f));
      }
    }));
  }

}
