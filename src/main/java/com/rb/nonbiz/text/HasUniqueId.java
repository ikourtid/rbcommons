package com.rb.nonbiz.text;

/**
 * Implemented by an items with a {@link UniqueId}.
 *
 * @see UniqueId
 * @see HasHumanReadableLabel
 */
public interface HasUniqueId<T> {

  UniqueId<T> getUniqueId();

}
