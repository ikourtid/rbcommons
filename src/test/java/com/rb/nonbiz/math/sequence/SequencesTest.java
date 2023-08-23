package com.rb.nonbiz.math.sequence;

import org.junit.Test;

import java.util.Iterator;

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
    Iterator<String> iterator = stringSequence.iterator();
    assertEquals("_100", iterator.next());
    assertEquals("_200", iterator.next());
    assertEquals("_400", iterator.next());
  }

}
