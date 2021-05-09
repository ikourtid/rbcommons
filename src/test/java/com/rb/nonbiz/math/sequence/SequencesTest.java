package com.rb.nonbiz.math.sequence;

import org.junit.Test;

import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;
import static com.rb.nonbiz.math.sequence.Sequences.transformedSequence;
import static org.junit.Assert.assertEquals;

public class SequencesTest {

  @Test
  public void testTransformedSequence() {
    Sequence<String> stringSequence = transformedSequence(
        geometricProgressionBuilder()
            .setInitialValue(100)
            .setCommonRatio(2)
            .build(),
        v -> "_" + v.intValue());
    assertEquals("_100", stringSequence.get(0));
    assertEquals("_200", stringSequence.get(1));
    assertEquals("_400", stringSequence.get(2));
    assertEquals("_102400", stringSequence.get(10)); // 100 * (2 ^ 10)
  }

}
