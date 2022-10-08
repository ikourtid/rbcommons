package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.types.HasLongRepresentation;

/**
 * An 'investable' is something that
 * <ol>
 *  <li> Can be a key to an eigendecomposition. </li>
 *  <li> Can be something we can buy. E.g. we can buy shares in VTI, or we can buy shares in the US stocks
 *    instrument class (or, more generally, asset class). </li>
 * </ol>
 *
 * <p> The current examples (Jun 2022) are {@link InstrumentId}, {@link AssetId},
 * InstrumentClass, and AssetClass, and {@link CashId}. </p>
 */
public interface Investable extends HasLongRepresentation {

}
