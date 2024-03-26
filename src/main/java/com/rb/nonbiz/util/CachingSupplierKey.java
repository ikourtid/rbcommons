package com.rb.nonbiz.util;

import com.rb.biz.types.asset.InstrumentId;
import org.apache.commons.math3.linear.EigenDecomposition;

/**
 * Represents a key for a cache of generic type V.
 *
 * <p> For example, V could be {@link EigenDecomposition} of {@link InstrumentId}.
 */
public interface CachingSupplierKey<V> {

}
