package com.rb.nonbiz.types;

public interface HasLongRepresentation extends Comparable<HasLongRepresentation> {

  long asLong();

  @Override
  default int compareTo(HasLongRepresentation other) {
    return Long.compare(asLong(), other.asLong());
  }

}
