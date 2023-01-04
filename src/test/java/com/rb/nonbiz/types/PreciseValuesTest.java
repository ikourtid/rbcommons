package com.rb.nonbiz.types;

import com.rb.biz.types.Money;
import com.rb.nonbiz.types.PreciseValues.BigDecimalsEpsilonComparisonVisitor;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.SellQuantity.sellQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BUY_QUANTITY;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_MONEY;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_SELL_QUANTITY;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.PreciseValues.epsilonComparePreciseValues;
import static com.rb.nonbiz.types.PreciseValues.epsilonComparePreciseValuesAsDoubles;
import static com.rb.nonbiz.types.RBDoublesTest.comparisonSignVisitor;
import static org.junit.Assert.assertEquals;

public class PreciseValuesTest {

  public static BigDecimalsEpsilonComparisonVisitor<String> bigDecimalsComparisonSignVisitor() {
    return new BigDecimalsEpsilonComparisonVisitor<String>() {
      @Override
      public String visitRightIsGreater(BigDecimal rightMinusLeft) {
        return "<";
      }

      @Override
      public String visitAlmostEqual() {
        return "==";
      }

      @Override
      public String visitLeftIsGreater(BigDecimal rightMinusLeft) {
        return ">";
      }
    };
  }

  @Test
  public void testEpsilonCompareAsDoubles_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    Money one = money(1);
    Money ten = money(10);
    Money slightlyMoreThan10 = money(10 + 1e-9);

    assertEquals("<",  epsilonComparePreciseValuesAsDoubles(one,                ten,                epsilon(1e-8), visitor));
    assertEquals(">",  epsilonComparePreciseValuesAsDoubles(ten,                one,                epsilon(1e-8), visitor));
    assertEquals("==", epsilonComparePreciseValuesAsDoubles(ten,                ten,                epsilon(1e-8), visitor));
    assertEquals("==", epsilonComparePreciseValuesAsDoubles(slightlyMoreThan10, ten,                epsilon(1e-8), visitor));
    assertEquals("==", epsilonComparePreciseValuesAsDoubles(ten,                slightlyMoreThan10, epsilon(1e-8), visitor));
    assertEquals(">",  epsilonComparePreciseValuesAsDoubles(slightlyMoreThan10, ten,                epsilon(1e-10), visitor));
    assertEquals("<",  epsilonComparePreciseValuesAsDoubles(ten,                slightlyMoreThan10, epsilon(1e-10), visitor));
  }

  @Test
  public void testEpsilonCompare_generalCase() {
    BigDecimalsEpsilonComparisonVisitor<String> visitor = bigDecimalsComparisonSignVisitor();
    assertEquals("<",  epsilonComparePreciseValues(buyQuantity(77),        sellQuantity(88),        epsilon(1e-8), visitor));
    assertEquals(">",  epsilonComparePreciseValues(buyQuantity(88),        sellQuantity(77),        epsilon(1e-8), visitor));
    assertEquals("==", epsilonComparePreciseValues(buyQuantity(88),        sellQuantity(88),        epsilon(1e-8), visitor));
    assertEquals("==", epsilonComparePreciseValues(buyQuantity(88 + 1e-9), sellQuantity(88),        epsilon(1e-8), visitor));
    assertEquals("==", epsilonComparePreciseValues(buyQuantity(88),        sellQuantity(88 + 1e-9), epsilon(1e-8), visitor));
    assertEquals(">",  epsilonComparePreciseValues(buyQuantity(88 + 1e-9), sellQuantity(88),        epsilon(1e-10), visitor));
    assertEquals("<",  epsilonComparePreciseValues(buyQuantity(88),        sellQuantity(88 + 1e-9), epsilon(1e-10), visitor));
  }

}
