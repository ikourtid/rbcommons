package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.types.HasLongRepresentation;

/**
 * An investable is something that
 * a) can be a key to an eigendecomposition
 * b) can be something we can buy. E.g. we can buy shares in VTI, or we can buy shares in the US stocks
 *    instrument class (or, more generally, asset class).
 *
 * The current examples (Jan 2017) are InstrumentId, InstrumentClass, AssetClass.
 */
public interface Investable extends HasLongRepresentation {

}
