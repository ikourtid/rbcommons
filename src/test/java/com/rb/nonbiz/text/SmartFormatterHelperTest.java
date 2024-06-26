package com.rb.nonbiz.text;

import com.rb.biz.guice.RBSimpleTestClock;
import com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.rb.biz.guice.RBSimpleTestClock.rbSimpleTestClock;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SmartFormatterHelperTest extends RBTest<SmartFormatterHelper> {

  private final InstrumentId I_11 = instrumentId(11);
  private final InstrumentId I_22 = instrumentId(22);

  private final HardCodedInstrumentMaster INSTRUMENT_MASTER = hardCodedInstrumentMaster(
      I_11, "I.11",
      I_22, "I.22");
  private final RBSimpleTestClock RB_CLOCK = rbSimpleTestClock(LocalDateTime.of(1974, 4, 4, 4, 4, 4));

  @Test
  public void automaticallyConvertsSimpleInstruments_hasMessage() {
    assertThat(
        makeTestObject().format("Message with %s and %s", I_11, I_22),
        stringMatcher("Message with I.11 (iid 11 ) and I.22 (iid 22 )"));

    assertThat(
        makeTestObject().formatSingleObject(I_11),
        stringMatcher("I.11 (iid 11 )"));
  }

  @Test
  public void testFormatWithTimePrepended() {
    assertThat(
        makeTestObject().formatWithDatePrepended("Message with %s and %s", I_11, I_22),
        stringMatcher("1974-04-04 Message with I.11 (iid 11 ) and I.22 (iid 22 )"));
  }

  @Test
  public void mustHaveBothInstrumentMasterAndRbClock_otherwiseDoesNotConvert() {
    SmartFormatterHelper hasNeither = new SmartFormatterHelper();
    SmartFormatterHelper onlyHasInstrumentMaster = new SmartFormatterHelper();
    onlyHasInstrumentMaster.instrumentMaster = INSTRUMENT_MASTER;
    SmartFormatterHelper onlyHasClock = new SmartFormatterHelper();
    onlyHasClock.rbClock = RB_CLOCK;

    rbSetOf(
        hasNeither,
        onlyHasInstrumentMaster,
        onlyHasClock)
        .forEach(smartFormatterHelper -> {
          assertThat(
              smartFormatterHelper.format("Message with %s and %s", I_11, I_22),
              stringMatcher("Message with iid 11 and iid 22"));

          assertThat(
              smartFormatterHelper.formatSingleObject(I_11),
              stringMatcher("iid 11"));
        });
  }

  @Test
  public void testStackOverflowPreventionMechanism() {
    SmartFormatterHelper smartFormatterHelper = makeTestObject();
    assertEquals(0, smartFormatterHelper.unsafeTestOnlyGetStackDepth());

    // These should increment and then decrement this 'stack depth' counter, all inside this call:
    assertThat(
        smartFormatterHelper.formatWithDatePrepended("Message with %s and %s", I_11, I_22),
        stringMatcher("1974-04-04 Message with I.11 (iid 11 ) and I.22 (iid 22 )"));
    assertEquals(0, smartFormatterHelper.unsafeTestOnlyGetStackDepth());

    assertThat(
        smartFormatterHelper.format("Message with %s and %s", I_11, I_22),
        stringMatcher("Message with I.11 (iid 11 ) and I.22 (iid 22 )"));
    assertEquals(0, smartFormatterHelper.unsafeTestOnlyGetStackDepth());

    assertThat(
        smartFormatterHelper.formatSingleObject(I_11),
        stringMatcher("I.11 (iid 11 )"));
    assertEquals(0, smartFormatterHelper.unsafeTestOnlyGetStackDepth());


    boolean threwException = false;
    try {
      smartFormatterHelper.formatSingleObject(
          (PrintsInstruments) (instrumentMaster, date) ->
              // 33 is not in the InstrumentMaster, so this will throw an exception, and the creation of the exception
              // text will itself will call smartFormatter.
              smartFormatterHelper.formatSingleObject(instrumentId(33)));
    } catch (Exception intentionallyIgnored) {
      // do nothing here
      threwException = true;
    }
    assertTrue(threwException);
    // So the exception above will leave this counter in a non-zero state...
    assertEquals(2, smartFormatterHelper.unsafeTestOnlyGetStackDepth());

    // ... but then, if we reset it, it will go back to zero.
    smartFormatterHelper.reset();
    assertEquals(0, smartFormatterHelper.unsafeTestOnlyGetStackDepth());
  }

  @Override
  protected SmartFormatterHelper makeTestObject() {
    SmartFormatterHelper testObject = new SmartFormatterHelper();
    testObject.instrumentMaster = INSTRUMENT_MASTER;
    testObject.rbClock = RB_CLOCK;
    return testObject;
  }

}
