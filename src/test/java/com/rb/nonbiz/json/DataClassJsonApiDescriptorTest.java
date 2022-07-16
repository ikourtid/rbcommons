package com.rb.nonbiz.json;

import com.rb.nonbiz.json.DataClassJsonApiDescriptor.CollectionJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.IidMapJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaEnumJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.PseudoEnumJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.RBMapJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaGenericJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.CollectionJsonApiDescriptorTest.collectionJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.IidMapJsonApiDescriptorTest.iidMapJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.JavaEnumJsonApiDescriptorTest.javaEnumJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.PseudoEnumJsonApiDescriptorTest.pseudoEnumJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.RBMapJsonApiDescriptorTest.rbMapJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.SimpleClassJsonApiDescriptorTest.simpleClassJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.JavaGenericJsonApiDescriptorTest.javaGenericJsonApiDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;
import static org.junit.Assert.fail;

public class DataClassJsonApiDescriptorTest extends RBTestMatcher<DataClassJsonApiDescriptor> {

  @Test
  public void noGenericArguments_throws() {
    fail("");
  }

  @Override
  public DataClassJsonApiDescriptor makeTrivialObject() {
    return new SimpleClassJsonApiDescriptorTest().makeTrivialObject();
  }

  @Override
  public DataClassJsonApiDescriptor makeNontrivialObject() {
    return new RBMapJsonApiDescriptorTest().makeNontrivialObject();
  }

  @Override
  public DataClassJsonApiDescriptor makeMatchingNontrivialObject() {
    return new RBMapJsonApiDescriptorTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(DataClassJsonApiDescriptor expected, DataClassJsonApiDescriptor actual) {
    return dataClassJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<DataClassJsonApiDescriptor> dataClassJsonApiDescriptorMatcher(
      DataClassJsonApiDescriptor expected) {
    return generalVisitorMatcher(expected, v -> v.visit(new Visitor<VisitorMatchInfo<DataClassJsonApiDescriptor>>() {
      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitSimpleClassJsonApiDescriptor(
          SimpleClassJsonApiDescriptor simpleClassJsonApiDescriptor) {
        return visitorMatchInfo(1, simpleClassJsonApiDescriptor,
            (MatcherGenerator<SimpleClassJsonApiDescriptor>) f -> simpleClassJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitIidMapJsonApiDescriptor(
          IidMapJsonApiDescriptor iidMapJsonApiDescriptor) {
        return visitorMatchInfo(2, iidMapJsonApiDescriptor,
            (MatcherGenerator<IidMapJsonApiDescriptor>) f -> iidMapJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitRBMapJsonApiDescriptor(
          RBMapJsonApiDescriptor rbMapJsonApiDescriptor) {
        return visitorMatchInfo(3, rbMapJsonApiDescriptor,
            (MatcherGenerator<RBMapJsonApiDescriptor>) f -> rbMapJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitCollectionJsonApiDescriptor(
          CollectionJsonApiDescriptor collectionJsonApiDescriptor) {
        return visitorMatchInfo(4, collectionJsonApiDescriptor,
            (MatcherGenerator<CollectionJsonApiDescriptor>) f -> collectionJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitJavaGenericJsonApiDescriptor(
          JavaGenericJsonApiDescriptor javaGenericJsonApiDescriptor) {
        return visitorMatchInfo(5, javaGenericJsonApiDescriptor,
            (MatcherGenerator<JavaGenericJsonApiDescriptor>) f -> javaGenericJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitPseudoEnumJsonApiDescriptor(
          PseudoEnumJsonApiDescriptor pseudoEnumJsonApiDescriptor) {
        return visitorMatchInfo(6, pseudoEnumJsonApiDescriptor,
            (MatcherGenerator<PseudoEnumJsonApiDescriptor>) f -> pseudoEnumJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitJavaEnumJsonApiDescriptor(
          JavaEnumJsonApiDescriptor javaEnumJsonApiDescriptor) {
        return visitorMatchInfo(7, javaEnumJsonApiDescriptor,
            (MatcherGenerator<JavaEnumJsonApiDescriptor>) f -> javaEnumJsonApiDescriptorMatcher(f));
      }
    }));
  }

}
