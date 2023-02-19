package com.rb.biz.guice.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.rb.biz.guice.RBClock;
import com.rb.biz.guice.RBClockModifier;
import com.rb.biz.guice.RBSimpleTestClock;
import com.rb.biz.guice.RBThreadLocalClock;
import com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.SmartFormatterHelper;
import com.rb.nonbiz.util.RBMapPreconditions;
import com.rb.nonbiz.util.RBOrderingPreconditions;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;
import com.rb.nonbiz.util.RBSubsetPreconditions;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RBCommonsTestModule implements Module {

  public static RBCommonsTestModule rbCommonsIntegrationTestModule() {
    return new RBCommonsTestModule();
  }

  @Override
  public void configure(Binder binder) {
    binder.requestStaticInjection(RBLog.class);
    binder.requestStaticInjection(SmartFormatterHelper.class);

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
