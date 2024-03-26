package com.rb.nonbiz.types;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.HasUniqueId;

/**
 * Implemented by classes who have a unique numeric long ID, such as {@link InstrumentId}.
 *
 * <p> Somewhat parallel to {@link HasUniqueId}, except for the case where the ID is a long. </p>
 */
public interface HasLongRepresentation extends Comparable<HasLongRepresentation> {

  long asLong();

  @Override
  default int compareTo(HasLongRepresentation other) {
    return Long.compare(asLong(), other.asLong());
  }

}
