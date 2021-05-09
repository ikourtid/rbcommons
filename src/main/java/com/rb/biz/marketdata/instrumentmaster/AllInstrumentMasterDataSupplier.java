package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDate;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a special verb class, because it is stateful.
 */
@Singleton
public class AllInstrumentMasterDataSupplier {

  private final Cache<LocalDate, AllIndexedInstrumentMasterData> cache;
  private final Lock masterLock;

  public AllInstrumentMasterDataSupplier() {
    this.cache = CacheBuilder.newBuilder()
        .initialCapacity(250 * 20) // 20 years's worth of entries
        .build();
    this.masterLock = new ReentrantLock();
  }

  @Inject AllIndexedInstrumentMasterDataLoader allIndexedInstrumentMasterDataLoader;

  public AllIndexedInstrumentMasterData getAllMasterDataAsOf(LocalDate asOfDate) {
    // Issue #344
    LocalDate singleCacheKey = LocalDate.of(1974, 4, 4);
    try {
      masterLock.lock();
      AllIndexedInstrumentMasterData data = cache.getIfPresent(singleCacheKey);
      if (data != null) {
        return data;
      }
      data = allIndexedInstrumentMasterDataLoader.load(asOfDate);
      cache.put(singleCacheKey, data);
      return data;
    } finally {
      masterLock.unlock();
    }
  }

}
