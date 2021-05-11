package com.rb.biz.guice.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.rb.biz.guice.RBClock;
import com.rb.biz.guice.RBClockModifier;
import com.rb.nonbiz.text.RBLog;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RBCommonsTestModule implements Module {

  public static RBCommonsTestModule rbCommonsIntegrationTestModule() {
    return new RBCommonsTestModule();
  }

  @Override
  public void configure(Binder binder) {
    binder.requestStaticInjection(RBLog.class);

    binder
        .bind(RBClock.class)
        .asEagerSingleton();

    binder
        .bind(RBClockModifier.class)
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
