package com.rb.nonbiz.json;

import com.rb.nonbiz.json.DataClassJsonApiDescriptor.CollectionJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.IidMapJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.RBMapJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.UniqueIdJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.Visitor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.YearlyTimeSeriesJsonApiDescriptor;
import com.rb.nonbiz.testmatchers.RBMatchers;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.CollectionJsonApiDescriptorTest.collectionJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.IidMapJsonApiDescriptorTest.iidMapJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.RBMapJsonApiDescriptorTest.rbMapJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.SimpleClassJsonApiDescriptorTest.simpleClassJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.UniqueIdJsonApiDescriptorTest.uniqueIdJsonApiDescriptorMatcher;
import static com.rb.nonbiz.json.YearlyTimeSeriesJsonApiDescriptorTest.yearlyTimeSeriesJsonApiDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class DataClassJsonApiDescriptorTest extends RBTestMatcher<DataClassJsonApiDescriptor> {

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
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitUniqueIdJsonApiDescriptor(
          UniqueIdJsonApiDescriptor uniqueIdJsonApiDescriptor) {
        return visitorMatchInfo(2, uniqueIdJsonApiDescriptor,
            (MatcherGenerator<UniqueIdJsonApiDescriptor>) f -> uniqueIdJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitIidMapJsonApiDescriptor(
          IidMapJsonApiDescriptor iidMapJsonApiDescriptor) {
        return visitorMatchInfo(3, iidMapJsonApiDescriptor,
            (MatcherGenerator<IidMapJsonApiDescriptor>) f -> iidMapJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitRBMapJsonApiDescriptor(
          RBMapJsonApiDescriptor rbMapJsonApiDescriptor) {
        return visitorMatchInfo(4, rbMapJsonApiDescriptor,
            (MatcherGenerator<RBMapJsonApiDescriptor>) f -> rbMapJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitCollectionJsonApiDescriptor(
          CollectionJsonApiDescriptor collectionJsonApiDescriptor) {
        return visitorMatchInfo(5, collectionJsonApiDescriptor,
            (MatcherGenerator<CollectionJsonApiDescriptor>) f -> collectionJsonApiDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<DataClassJsonApiDescriptor> visitYearlyTimeSeriesJsonApiDescriptor(
          YearlyTimeSeriesJsonApiDescriptor yearlyTimeSeriesJsonApiDescriptor) {
        return visitorMatchInfo(6, yearlyTimeSeriesJsonApiDescriptor,
            (MatcherGenerator<YearlyTimeSeriesJsonApiDescriptor>) f -> yearlyTimeSeriesJsonApiDescriptorMatcher(f));
      }
    }));
  }

}
