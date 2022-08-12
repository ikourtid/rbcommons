package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBMapWithDefault;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.IidMapJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaGenericJsonApiDescriptor;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.IidMapJsonApiDescriptor.iidMapJsonApiDescriptor;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaGenericJsonApiDescriptor.javaGenericJsonApiDescriptor;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor.simpleClassJsonApiDescriptor;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptorTest.dataClassJsonApiDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class JavaGenericJsonApiDescriptorTest extends RBTestMatcher<JavaGenericJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,
        PreciseValue.class,
        ImpreciseValue.class,

        Strings.class,
        InstrumentId.class,
        Symbol.class,

        RBSet.class,
        IidSet.class,
        IidMap.class,
        RBMap.class).forEach(badOuterClass -> {
          DataClassJsonApiDescriptor dummy = simpleClassJsonApiDescriptor(String.class);
          assertIllegalArgumentException( () -> javaGenericJsonApiDescriptor(badOuterClass, dummy));
          assertIllegalArgumentException( () -> javaGenericJsonApiDescriptor(badOuterClass, dummy, dummy));
        }
    );
  }

  @Override
  public JavaGenericJsonApiDescriptor makeTrivialObject() {
    return javaGenericJsonApiDescriptor(RBMapWithDefault.class, simpleClassJsonApiDescriptor(Money.class));
  }

  @Override
  public JavaGenericJsonApiDescriptor makeNontrivialObject() {
    return javaGenericJsonApiDescriptor(ClosedRange.class,
        iidMapJsonApiDescriptor(simpleClassJsonApiDescriptor(UnitFraction.class)));
  }

  @Override
  public JavaGenericJsonApiDescriptor makeMatchingNontrivialObject() {
    return javaGenericJsonApiDescriptor(ClosedRange.class,
        iidMapJsonApiDescriptor(simpleClassJsonApiDescriptor(UnitFraction.class)));
  }

  @Override
  protected boolean willMatch(JavaGenericJsonApiDescriptor expected, JavaGenericJsonApiDescriptor actual) {
    return javaGenericJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JavaGenericJsonApiDescriptor> javaGenericJsonApiDescriptorMatcher(
      JavaGenericJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getOuterClass()),
        matchList(       v -> v.getGenericArgumentClassDescriptors(), f -> dataClassJsonApiDescriptorMatcher(f)));
  }

}
