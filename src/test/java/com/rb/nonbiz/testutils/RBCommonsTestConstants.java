package com.rb.nonbiz.testutils;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.rb.biz.types.Money;
import com.rb.biz.types.OnesBasedReturn;
import com.rb.biz.types.Price;
import com.rb.biz.types.SignedMoney;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.trading.BuyQuantity;
import com.rb.biz.types.trading.NonNegativeQuantity;
import com.rb.biz.types.trading.PositiveQuantity;
import com.rb.biz.types.trading.SellQuantity;
import com.rb.biz.types.trading.SignedQuantity;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.Correlation;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.types.RationalUnitFraction;
import com.rb.nonbiz.types.SplitMultiplier;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.trading.BuyQuantity.buyQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.biz.types.trading.SellQuantity.sellQuantity;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.types.Correlation.correlation;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.RationalUnitFraction.rationalUnitFraction;
import static com.rb.nonbiz.types.SplitMultiplier.splitMultiplier;

/**
 * This class doesn't have any real functionality; it's just a repository for a bunch of
 * dummy values that are convenient to use in tests in order to denote that a value doesn't matter.
 *
 * RBTest and RBIntegrationTest derive from RBTestConstants, but this is purely for convenience reasons.
 * Autocompletion in the IDE can be much smarter, and it keeps us from having to static import these constants.
 */
public abstract class RBCommonsTestConstants<T> {

  // These few items are not alphabetical, so we won't have forward references in the code
  public static final String DUMMY_STRING = "abc123";

  public static final BigDecimal DUMMY_BIG_DECIMAL = BigDecimal.valueOf(0.12219889);
  public static final boolean DUMMY_BOOLEAN = false;
  public static final BuyQuantity DUMMY_BUY_QUANTITY = buyQuantity(222);
  public static final Correlation DUMMY_CORRELATION = correlation(-0.112233);
  public static final double DUMMY_DOUBLE = 987.65;
  public static final double DUMMY_POSITIVE_DOUBLE = 12.34;
  public static final double DUMMY_NEGATIVE_DOUBLE = -23.45;
  public static final DoubleMatrix1D DUMMY_DOUBLE_MATRIX_1D = new DenseDoubleMatrix1D(new double[] { DUMMY_DOUBLE, DUMMY_DOUBLE });
  public static final HumanReadableLabel DUMMY_LABEL = label(DUMMY_STRING);
  public static final int DUMMY_POSITIVE_INTEGER = 111;
  public static final Long DUMMY_LONG = 7878L;
  public static final LocalDate DUMMY_DATE = LocalDate.of(1974, 4, 4);
  public static final LocalDateTime DUMMY_TIME = LocalDateTime.of(1974, 4, 4, 11, 59, 59, 123_456_789);
  public static final LocalDate DUMMY_MARKET_DATE = LocalDate.of(2006, 4, 5);  // 4/5/06
  public static final LocalDateTime DUMMY_MARKET_TIME = LocalDateTime.of(2006, 4, 5, 13, 2, 3, 123_456_789); // 1:23 pm 4/5/06
  public static final Money DUMMY_MONEY = money(543.21);
  public static final NonNegativeQuantity DUMMY_NON_NEGATIVE_QUANTITY = nonNegativeQuantity(222);
  public static final OnesBasedReturn DUMMY_ONES_BASED_RETURN = onesBasedReturn(1.2345);
  public static final OnesBasedReturn DUMMY_ONES_BASED_GAIN = onesBasedReturn(1.2233);
  public static final PositiveQuantity DUMMY_POSITIVE_QUANTITY = positiveQuantity(111);
  public static final Price DUMMY_PRICE = price(123.45);
  public static final SignedQuantity DUMMY_SIGNED_QUANTITY = signedQuantity(-7.89);
  public static final PositiveMultiplier DUMMY_POSITIVE_MULTIPLIER = positiveMultiplier(1.234);
  public static final SplitMultiplier DUMMY_SPLIT_MULTIPLIER = splitMultiplier(BigDecimal.valueOf(3.456));
  public static final RationalUnitFraction DUMMY_RATIONAL_FRACTION = rationalUnitFraction(123, 456);
  public static final SellQuantity DUMMY_SELL_QUANTITY = sellQuantity(333);
  public static final SignedMoney DUMMY_SIGNED_MONEY = signedMoney(-11.11);
  public static final SignedMoney DUMMY_POSITIVE_SIGNED_MONEY = signedMoney(22.22);
  public static final Symbol DUMMY_SYMBOL = symbol("POS");
  public static final UniqueId DUMMY_UNIQUE_STRING_ID = uniqueId("foo");
  public static double ZERO_SEED = 0;
  public static double EPSILON_SEED = 1e-9;
  public static double DUMMY_SEED = 1e-7;
  public static double DUMMY_EPSILON = 0.012345;

  // The following are consecutive market days used in many tests.
  public static final LocalDate DAY0 = LocalDate.of(2010, 1, 29);
  public static final LocalDate DAY1 = LocalDate.of(2010, 2, 1);
  public static final LocalDate DAY2 = LocalDate.of(2010, 2, 2);
  public static final LocalDate DAY3 = LocalDate.of(2010, 2, 3);
  public static final LocalDate DAY4 = LocalDate.of(2010, 2, 4);
  public static final LocalDate DAY5 = LocalDate.of(2010, 2, 5);
  public static final LocalDate DAY6 = LocalDate.of(2010, 2, 8);
  public static final LocalDate DAY7 = LocalDate.of(2010, 2, 9);
  public static final LocalDate DAY8 = LocalDate.of(2010, 2, 10);

  // This doesn't test anything, but it's convenient for diagnosing problems where one or more of the
  // predefined constants above cannot be defined.
  @Test
  public void emptyTest() {}

}
