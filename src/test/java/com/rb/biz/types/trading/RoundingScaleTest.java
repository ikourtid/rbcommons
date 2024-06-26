package com.rb.biz.types.trading;

import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.rb.biz.types.trading.RoundingScale.INTEGER_ROUNDING_SCALE;
import static com.rb.biz.types.trading.RoundingScale.roundingScale;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class RoundingScaleTest extends RBTestMatcher<RoundingScale> {

  @Test
  public void roundingScale_tooSmall_orTooLarge_throws() {
    Function<Integer, RoundingScale> maker = scale -> roundingScale(scale);

    assertIllegalArgumentException( () -> maker.apply(-20));
    assertIllegalArgumentException( () -> maker.apply(-11));
    assertIllegalArgumentException( () -> maker.apply( 11));
    assertIllegalArgumentException( () -> maker.apply( 20));

    RoundingScale doesNotThrow;
    doesNotThrow = maker.apply(-10);
    doesNotThrow = maker.apply(-9);
    doesNotThrow = maker.apply(-1);
    doesNotThrow = maker.apply( 0);
    doesNotThrow = maker.apply( 1);
    doesNotThrow = maker.apply( 9);
    doesNotThrow = maker.apply(10);
  }

  @Test
  public void testValues() {
    assertEquals(BigDecimal.valueOf(1_000), roundingScale(-3).getAsValue());
    assertEquals(BigDecimal.valueOf(  100), roundingScale(-2).getAsValue());
    assertEquals(BigDecimal.valueOf(   10), roundingScale(-1).getAsValue());
    assertEquals(BigDecimal.valueOf(    1), roundingScale( 0).getAsValue());
    assertEquals(BigDecimal.valueOf(    1), INTEGER_ROUNDING_SCALE.getAsValue());
    assertEquals(BigDecimal.valueOf(  0.1), roundingScale( 1).getAsValue());
    assertEquals(BigDecimal.valueOf( 0.01), roundingScale( 2).getAsValue());
    assertEquals(BigDecimal.valueOf(0.001), roundingScale( 3).getAsValue());
    assertEquals(0.001, roundingScale( 3).getPowerOf10AsDouble(), 1e-8);
  }

  @Test
  public void testGetPowerOf10AsDouble() {
    // use max delta = 0; should match exactly
    assertEquals(1_000.0,   roundingScale(-3).getPowerOf10AsDouble(), 0);
    assertEquals(  100.0,   roundingScale(-2).getPowerOf10AsDouble(), 0);
    assertEquals(   10.0,   roundingScale(-1).getPowerOf10AsDouble(), 0);
    assertEquals(    1.0,   roundingScale( 0).getPowerOf10AsDouble(), 0);
    assertEquals(    1.0,   INTEGER_ROUNDING_SCALE.getPowerOf10AsDouble(), 0);
    assertEquals(    0.1,   roundingScale( 1).getPowerOf10AsDouble(), 0);
    assertEquals(    0.01,  roundingScale( 2).getPowerOf10AsDouble(), 0);
    assertEquals(    0.001, roundingScale( 3).getPowerOf10AsDouble(), 0);
  }

  @Test
  public void testGetPowerOf10InverseAsDouble() {
    // use max delta = 0; should match exactly
    assertEquals(    0.001, roundingScale(-3).getPowerOf10InverseAsDouble(), 0);
    assertEquals(    0.01,  roundingScale(-2).getPowerOf10InverseAsDouble(), 0);
    assertEquals(    0.1,   roundingScale(-1).getPowerOf10InverseAsDouble(), 0);
    assertEquals(    1.0,   roundingScale( 0).getPowerOf10InverseAsDouble(), 0);
    assertEquals(    1.0,   INTEGER_ROUNDING_SCALE.getPowerOf10InverseAsDouble(), 0);
    assertEquals(   10.0,   roundingScale( 1).getPowerOf10InverseAsDouble(), 0);
    assertEquals(  100.0,   roundingScale( 2).getPowerOf10InverseAsDouble(), 0);
    assertEquals(1_000.0,   roundingScale( 3).getPowerOf10InverseAsDouble(), 0);
  }

  @Test
  public void testCompareTo() {
    assertEquals(-1, roundingScale(-1).compareTo(roundingScale(2)));
    assertEquals(-1, roundingScale( 0).compareTo(roundingScale(2)));
    assertEquals(-1, roundingScale( 1).compareTo(roundingScale(2)));
    assertEquals( 0, roundingScale( 2).compareTo(roundingScale(2)));
    assertEquals( 1, roundingScale( 3).compareTo(roundingScale(2)));
    assertEquals( 1, roundingScale( 4).compareTo(roundingScale(2)));
    assertEquals( 1, roundingScale( 5).compareTo(roundingScale(2)));
  }

  @Test
  public void testRoundSimplistically() {
    TriConsumer<Double, RoundingScale, Double> asserter = (expectedRoundedResult, roundingScale, unrounded) ->
        assertEquals(expectedRoundedResult, roundingScale.roundSimplistically(unrounded), 1e-8);

    asserter.accept(990.0,      roundingScale(-1), 987.654321);
    asserter.accept(988.0,      roundingScale(0),  987.654321);
    asserter.accept(987.7,      roundingScale(1),  987.654321);
    asserter.accept(987.65,     roundingScale(2),  987.654321);
    asserter.accept(987.654,    roundingScale(3),  987.654321);
    asserter.accept(987.654321, roundingScale(6),  987.654321);
    asserter.accept(987.654321, roundingScale(10), 987.654321);

    asserter.accept(-990.0,      roundingScale(-1), -987.654321);
    asserter.accept(-988.0,      roundingScale(0),  -987.654321);
    asserter.accept(-987.7,      roundingScale(1),  -987.654321);
    asserter.accept(-987.65,     roundingScale(2),  -987.654321);
    asserter.accept(-987.654,    roundingScale(3),  -987.654321);
    asserter.accept(-987.654321, roundingScale(6),  -987.654321);
    asserter.accept(-987.654321, roundingScale(10), -987.654321);

    // roundSimplistically works for cases in which multiplying by 10, rounding, and dividing by 10 would not:
    // (5.55 * 10.0) / 10.0 computes as 5.5, but we want it to compute 5.6.
    // Instead, we use two multiplications: (5.55 * 10.0) * 0.1, which computes to 5.6
    asserter.accept(5.6,   roundingScale(1), 5.55);
    asserter.accept(5.6,   roundingScale(1), 5.551);

    asserter.accept(56.0,  roundingScale( 0), 55.5);
    asserter.accept(56.0,  roundingScale( 0), 55.51);

    asserter.accept(560.0, roundingScale(-1), 555.0);
    asserter.accept(560.0, roundingScale(-1), 555.1);

    // -5.5 rounds simplistically 'up' to -5
    asserter.accept(-5.0,  roundingScale(0), -5.5);
    asserter.accept(-6.0,  roundingScale(0), -5.51);

    // -5.55 simplistically rounds 'up' to -5.55.
    asserter.accept(-5.5,  roundingScale(1), -5.55);
    asserter.accept(-5.6,  roundingScale(1), -5.551);
  }

  @Test
  public void testFloorSimplistically() {
    TriConsumer<Double, RoundingScale, Double> asserter = (expectedRoundedResult, roundingScale, unrounded) ->
        assertEquals(expectedRoundedResult, roundingScale.floorSimplistically(unrounded), 1e-8);

    asserter.accept(980.0,      roundingScale(-1), 987.654321);
    asserter.accept(987.0,      roundingScale(0),  987.654321);
    asserter.accept(987.6,      roundingScale(1),  987.654321);
    asserter.accept(987.65,     roundingScale(2),  987.654321);
    asserter.accept(987.654,    roundingScale(3),  987.654321);
    asserter.accept(987.654321, roundingScale(6),  987.654321);
    asserter.accept(987.654321, roundingScale(10), 987.654321);

    asserter.accept(-990.0,      roundingScale(-1), -987.654321);
    asserter.accept(-988.0,      roundingScale(0),  -987.654321);
    asserter.accept(-987.7,      roundingScale(1),  -987.654321);
    asserter.accept(-987.66,     roundingScale(2),  -987.654321);
    asserter.accept(-987.655,    roundingScale(3),  -987.654321);
    asserter.accept(-987.654321, roundingScale(6),  -987.654321);
    asserter.accept(-987.654321, roundingScale(10), -987.654321);

    asserter.accept(5.5,   roundingScale(1), 5.55);
    asserter.accept(5.5,   roundingScale(1), 5.551);

    asserter.accept(55.0,  roundingScale( 0), 55.5);
    asserter.accept(55.0,  roundingScale( 0), 55.51);

    asserter.accept(550.0, roundingScale(-1), 555.0);
    asserter.accept(550.0, roundingScale(-1), 555.1);

    asserter.accept(-6.0,  roundingScale(0), -5.5);
    asserter.accept(-6.0,  roundingScale(0), -5.51);

    asserter.accept(-5.6,  roundingScale(1), -5.55);
    asserter.accept(-5.6,  roundingScale(1), -5.551);
  }

  @Test
  public void testCeilingSimplistically() {
    TriConsumer<Double, RoundingScale, Double> asserter = (expectedRoundedResult, roundingScale, unrounded) ->
        assertEquals(expectedRoundedResult, roundingScale.ceilingSimplistically(unrounded), 1e-8);

    asserter.accept(990.0,      roundingScale(-1), 987.654321);
    asserter.accept(988.0,      roundingScale(0),  987.654321);
    asserter.accept(987.7,      roundingScale(1),  987.654321);
    asserter.accept(987.66,     roundingScale(2),  987.654321);
    asserter.accept(987.655,    roundingScale(3),  987.654321);
    asserter.accept(987.654321, roundingScale(6),  987.654321);
    asserter.accept(987.654321, roundingScale(10), 987.654321);

    asserter.accept(-980.0,      roundingScale(-1), -987.654321);
    asserter.accept(-987.0,      roundingScale(0),  -987.654321);
    asserter.accept(-987.6,      roundingScale(1),  -987.654321);
    asserter.accept(-987.65,     roundingScale(2),  -987.654321);
    asserter.accept(-987.654,    roundingScale(3),  -987.654321);
    asserter.accept(-987.654321, roundingScale(6),  -987.654321);
    asserter.accept(-987.654321, roundingScale(10), -987.654321);

    asserter.accept(5.6,   roundingScale(1), 5.55);
    asserter.accept(5.6,   roundingScale(1), 5.551);

    asserter.accept(56.0,  roundingScale( 0), 55.5);
    asserter.accept(56.0,  roundingScale( 0), 55.51);

    asserter.accept(560.0, roundingScale(-1), 555.0);
    asserter.accept(560.0, roundingScale(-1), 555.1);

    asserter.accept(-5.0,  roundingScale(0), -5.5);
    asserter.accept(-5.0,  roundingScale(0), -5.51);

    asserter.accept(-5.5,  roundingScale(1), -5.55);
    asserter.accept(-5.5,  roundingScale(1), -5.551);
  }

  @Override
  public RoundingScale makeTrivialObject() {
    return INTEGER_ROUNDING_SCALE;
  }

  @Override
  public RoundingScale makeNontrivialObject() {
    return roundingScale(8);
  }

  @Override
  public RoundingScale makeMatchingNontrivialObject() {
    return roundingScale(8);
  }

  @Override
  protected boolean willMatch(RoundingScale expected, RoundingScale actual) {
    return roundingScaleMatcher(expected).matches(actual);
  }

  public static <T extends RoundingScale> TypeSafeMatcher<T> roundingScaleMatcher(T expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getRawInt()));
  }

}
