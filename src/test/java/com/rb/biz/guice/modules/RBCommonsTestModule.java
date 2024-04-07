package com.rb.biz.guice.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.rb.biz.guice.RBClock;
import com.rb.biz.guice.RBSimpleTestClock;
import com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.SmartFormatter;
import com.rb.nonbiz.text.SmartFormatterHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RBCommonsTestModule implements Module {

  public static RBCommonsTestModule rbCommonsIntegrationTestModule() {
    return new RBCommonsTestModule();
  }

  @Override
  public void configure(Binder binder) {
    binder.requestStaticInjection(RBLog.class);
    // ExcludeSlowTestsSuiteRBCommons still gives us 4 failing tests if we run it by itself
    // (instead of ExcludeSlowTestsSuite), but without the following two lines, there would be way more failures.
    binder.requestStaticInjection(SmartFormatter.class);
    binder.requestStaticInjection(SmartFormatterHelper.class);

    // That is, by default, this binds to an empty HardCodedInstrumentMaster
    // (since HardCodedInstrumentMaster's no-arg constructor constructs an empty one).
    binder
        .bind(InstrumentMaster.class)
        .to(HardCodedInstrumentMaster.class);

    binder
        .bind(RBClock.class)
        .to(RBSimpleTestClock.class)
        .asEagerSingleton();
  }

  @Provides
  private LocalDateTime getLocalDateTime() {
    throw new IllegalArgumentException("You should be injecting an RBClock instead of a LocalDateTime");
  }

  @Provides
  private LocalDate getLocalDate() {
    throw new IllegalArgumentException("You should be injecting an RBClock instead of a LocalDate");
  }

}
