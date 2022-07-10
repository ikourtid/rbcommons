package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import org.junit.Test;

import static com.rb.nonbiz.collections.LinkedHashMaps.toSortedLinkedHashMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static org.junit.Assert.assertEquals;

public class LinkedHashMapsTest {

  @Test
  public void convertsRbMapsInOrder() {
    assertEquals(
        // The way we construct the map below, it is extremely unlikely that we will end up with the
        // ordering 0123456789 just by chance.
        "0123456789",
        Joiner.on("").join(
            toSortedLinkedHashMap(
                rbMapOf(
                    "0", DUMMY_POSITIVE_INTEGER,
                    "2", DUMMY_POSITIVE_INTEGER,
                    "4", DUMMY_POSITIVE_INTEGER,
                    "6", DUMMY_POSITIVE_INTEGER,
                    "8", DUMMY_POSITIVE_INTEGER,
                    "1", DUMMY_POSITIVE_INTEGER,
                    "3", DUMMY_POSITIVE_INTEGER,
                    "5", DUMMY_POSITIVE_INTEGER,
                    "7", DUMMY_POSITIVE_INTEGER,
                    "9", DUMMY_POSITIVE_INTEGER),
                String::compareTo)
                .keySet()));
  }

}
