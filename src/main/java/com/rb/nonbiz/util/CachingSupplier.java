package com.rb.nonbiz.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rb.nonbiz.text.RBLog;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.rb.nonbiz.text.RBLog.rbLog;

/**
 * This is a special verb class, because it is stateful.
 *
 * If you want to create a specialized caching supplier (e.g. one to load and cache the eigendecompositions),
 * remember to annotate your caching supplier class (not this) as
 * {@literal @Singleton}
 * ... otherwise, you would be reloading your eigendecomposition files into a different cache each time!
 *
 * If your cache is effectively '2-D' (e.g. you want to cache a unique eigendecomposition for each combination of
 * date and RiskModelDescriptor), no problem: just make sure the CachingSupplierKey is a pair of those two values,
 * and that it implements hashCode and equals.
 */
public class CachingSupplier<K extends CachingSupplierKey<V>, V> {

  private static final RBLog log = rbLog(CachingSupplier.class);

  private final Cache<K, V> cache;
  private final Lock masterLock;
  private final Map<K, Lock> itemLocks;

  public CachingSupplier(int initialCapacity) {
    this.cache = CacheBuilder.newBuilder()
        .initialCapacity(initialCapacity)
        .build();
    this.itemLocks = newHashMapWithExpectedSize(initialCapacity);
    this.masterLock = new ReentrantLock();
  }

  // Ideally, your subclass should have a more specific API than passing this 'cache key' that just calls this.
  // For example, it could be
  // public Eigendecomposition<InstrumentId> getEigendecomposition(UniqueId<RiskModelDescriptor> id, LocalDate date)
  public V getFromCache(K uniqueCacheKey, Supplier<V> cacheValueLoader) {
    // There's probably a better way to do this using the java 8 concurrency library or something else,
    // but this will do the trick.
    // We do this little trick so that we don't have too coarse locking. First, we acquire (after possibly creating)
    // a per-eigendecomposition lock. This way, only up to 1 thread will be fetching the eigendecomposition from disk.
    Lock itemLock;

    // First, lock the entire cache object (using masterLock), so no other thread could try to do mess with the
    // 'itemLocks' map. Retrieve the lock for this specific cache key (we have one per key); if not there, add a lock.
    // Since this is a fast operation, it's OK to use masterLock to lock this ENTIRE cache object,
    // as we will not be causing other threads to wait.
    try {
      masterLock.lock();
      itemLock = itemLocks.get(uniqueCacheKey);
      if (itemLock == null) {
        itemLock = new ReentrantLock();
        itemLocks.put(uniqueCacheKey, itemLock);
      }
    } finally {
      // make sure we don't leave masterLock locked,
      // whether things were normal & we gave a value to itemLock, OR if there's some exception.
      masterLock.unlock();
    }

    try {
      itemLock.lock();
      V item = cache.getIfPresent(uniqueCacheKey);
      if (item != null) {
        // the item is already there; just return it. The 'finally' clause will make sure itemLock gets unlocked.
        return item;
      }
      // OK, we need to load the item. Typically this will be a long-ish operation. This is the reason why we
      // have a single master lock and per-item locks: if e.g. we only need to retrieve an eigendecomposition for a given day,
      // and we end up having to load it from disk (which takes non-trivial time),
      // we don't want to lock any other threads that are trying to retrieve eigendecompositions for OTHER days
      // (which may already have been loaded & be in the cache), since it may take us a long time to finish.
      log.debug("Loading item into cache for key %s in thread %s",
          uniqueCacheKey, Thread.currentThread().getId());
      item = cacheValueLoader.get();
      try {
        // OK, now that we already loaded 'item', we can put it in the cache.
        // Since this is a fast operation, it's OK to use masterLock to lock this ENTIRE cache object,
        // as we will not be causing other threads to wait.
        masterLock.lock();
        cache.put(uniqueCacheKey, item);
      } finally {
        // make sure we don't leave masterLock locked,
        // whether things were normal & we gave a value to itemLock, OR if there's some exception.
        masterLock.unlock();
      }
      return item;
    } finally {
      itemLock.unlock();
    }
  }

}
