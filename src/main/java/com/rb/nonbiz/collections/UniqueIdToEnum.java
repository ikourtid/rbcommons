package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.HasUniqueId;
import com.rb.nonbiz.text.RBSetOfHasUniqueId;

import java.util.Arrays;

import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;

public class UniqueIdToEnum {

  public static <T extends HasUniqueId<T>> RBSetOfHasUniqueId<T> makeUniqueIdToEnumMap(T[] enumValues) {
    return rbSetOfHasUniqueId(
        rbMapFromStream(Arrays.stream(enumValues), v -> v.getUniqueId()));
  }

}
