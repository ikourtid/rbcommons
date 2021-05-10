package com.rb.nonbiz.reflection;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.nonbiz.reflection.ClassWithDepth.classWithDepth;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.uniqueClassesWithDepth;
import static junit.framework.TestCase.assertEquals;

public class UniqueClassesWithDepthMultilineStringFormatterTest extends RBTest<ClassesWithDepthMultilineStringFormatter> {

  @Test
  public void simpleTest() {
    assertEquals(
        "com.rb.biz.types.Money\n" +
        "....com.rb.nonbiz.types.UnitFraction\n" +
        "..com.rb.nonbiz.math.eigen.EigenDimensionIndex\n",
        makeTestObject().format(
            uniqueClassesWithDepth(ImmutableList.of(
                classWithDepth(Money.class, 0),
                classWithDepth(UnitFraction.class, 2),
                classWithDepth(EigenDimensionIndex.class, 1))),
            ".."));
  }

  @Override
  protected ClassesWithDepthMultilineStringFormatter makeTestObject() {
    return new ClassesWithDepthMultilineStringFormatter();
  }

}