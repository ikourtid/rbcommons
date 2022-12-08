package com.rb.nonbiz.types;

import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import java.util.OptionalInt;
import java.util.OptionalLong;

import static com.rb.nonbiz.types.RBIntegers.maxAllowingOptionalInt;
import static com.rb.nonbiz.types.RBIntegers.minAllowingOptionalInt;
import static com.rb.nonbiz.types.RBLongs.maxAllowingOptionalLong;
import static com.rb.nonbiz.types.RBLongs.minAllowingOptionalLong;
import static org.junit.Assert.assertEquals;

public class RBLongsTest {

  @Test
  public void testMaxAllowingOptionalInt() {
    TriConsumer<OptionalLong, Long, Long> asserter = (optionalValue1, value2, expectedResult) -> {
      assertEquals(expectedResult.longValue(), maxAllowingOptionalLong(optionalValue1, value2));
      assertEquals(expectedResult.longValue(), maxAllowingOptionalLong(value2, optionalValue1));
    };

    asserter.accept(OptionalLong.of(22), 11L, 22L);
    asserter.accept(OptionalLong.of(22), 33L, 33L);
    asserter.accept(OptionalLong.empty(), 33L, 33L);
  }

  @Test
  public void testMinAllowingOptionalInt() {
    TriConsumer<OptionalLong, Long, Long> asserter = (optionalValue1, value2, expectedResult) -> {
      assertEquals(expectedResult.longValue(), minAllowingOptionalLong(optionalValue1, value2));
      assertEquals(expectedResult.longValue(), minAllowingOptionalLong(value2, optionalValue1));
    };

    asserter.accept(OptionalLong.of(22), 11L, 11L);
    asserter.accept(OptionalLong.of(22), 33L, 22L);
    asserter.accept(OptionalLong.empty(), 33L, 33L);
  }

}
