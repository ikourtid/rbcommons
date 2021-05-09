package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.math.optimization.highlevel.FunctionDescriptor.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptorTest.waterSlideFunctionDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.alwaysMatchingMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class FunctionDescriptorTest {

  // There are no parameters in the QuadraticFunctionDescriptor, so there's nothing to compare.
  public static TypeSafeMatcher<QuadraticFunctionDescriptor> quadraticFunctionDescriptorMatcher(
      QuadraticFunctionDescriptor quadraticFunctionDescriptor) {
    return alwaysMatchingMatcher();
  }

  // We cannot compare two arbitrary math functions, so this will always match.
  // This is why we don't want to use this object, unless we're dealing with some test code.
  public static TypeSafeMatcher<ArbitraryFunctionDescriptor> arbitraryFunctionDescriptorMatcher(
      ArbitraryFunctionDescriptor expected) {
    return alwaysMatchingMatcher();
  }

  public static TypeSafeMatcher<FunctionDescriptor> functionDescriptorMatcher(FunctionDescriptor expected) {
    return generalVisitorMatcher(expected, ofd -> ofd.visit(new Visitor<VisitorMatchInfo<FunctionDescriptor>>() {

      @Override
      public VisitorMatchInfo<FunctionDescriptor> visitWaterSlideFunctionDescriptor(
          WaterSlideFunctionDescriptor waterSlideFunctionDescriptor) {
        return visitorMatchInfo(1, waterSlideFunctionDescriptor,
            (MatcherGenerator<WaterSlideFunctionDescriptor>) f -> waterSlideFunctionDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<FunctionDescriptor> visitQuadraticFunctionDescriptor(QuadraticFunctionDescriptor quadraticFunctionDescriptor) {
        return visitorMatchInfo(2, quadraticFunctionDescriptor,
            (MatcherGenerator<QuadraticFunctionDescriptor>) f -> quadraticFunctionDescriptorMatcher(f));
      }

      @Override
      public VisitorMatchInfo<FunctionDescriptor> visitArbitraryFunctionDescriptor(
          ArbitraryFunctionDescriptor arbitraryFunctionDescriptor) {
        return visitorMatchInfo(3, arbitraryFunctionDescriptor,
            (MatcherGenerator<ArbitraryFunctionDescriptor>) f -> arbitraryFunctionDescriptorMatcher(f));
      }

    }));
  }

}
