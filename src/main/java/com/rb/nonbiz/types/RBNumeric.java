package com.rb.nonbiz.types;

/**
 * This is a common base class for {@link ImpreciseValue} and {@link PreciseValue} which allows us to write methods
 * that work both on them. Moreover, because RBNumeric extends Number, it means that we can *also* write methods that
 * can work on all 3 of PreciseValue, ImpreciseValue, and Double - although those may look a bit hacky because
 * technically they should also support long, int, etc., which also extend Number.
 */
public abstract class RBNumeric<T> extends Number implements Comparable<T> {

}
