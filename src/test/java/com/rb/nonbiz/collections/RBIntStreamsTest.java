package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.RBIntStreams.reduceInGroups;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;

public class RBIntStreamsTest {

  @Test
  public void testReduceInGroups_groupSizeMustBePositive() {
    assertIllegalArgumentException( () -> reduceUsingBi(closedRange(123, 123), 0));
    assertIllegalArgumentException( () -> reduceUsingBi(closedRange(123, 124), 0));
    assertIllegalArgumentException( () -> reduceUsingBi(closedRange(123, 123), -1));
    assertIllegalArgumentException( () -> reduceUsingBi(closedRange(123, 124), -1));
    assertIllegalArgumentException( () -> reduceUsingBi(closedRange(123, 123), -99));
    assertIllegalArgumentException( () -> reduceUsingBi(closedRange(123, 124), -99));

    assertIllegalArgumentException( () -> reduceUsingTri(closedRange(123, 123), 0));
    assertIllegalArgumentException( () -> reduceUsingTri(closedRange(123, 124), 0));
    assertIllegalArgumentException( () -> reduceUsingTri(closedRange(123, 123), -1));
    assertIllegalArgumentException( () -> reduceUsingTri(closedRange(123, 124), -1));
    assertIllegalArgumentException( () -> reduceUsingTri(closedRange(123, 123), -99));
    assertIllegalArgumentException( () -> reduceUsingTri(closedRange(123, 124), -99));
  }

  @Test
  public void testReduceInGroups_biFunctionOverload() {
    // 'Group' of 1
    assertEquals(singletonList(   "0: 0"),                             reduceUsingBi(closedRange(0, 0), 1));
    assertEquals(ImmutableList.of("0: 0", "1: 1"),                     reduceUsingBi(closedRange(0, 1), 1));
    assertEquals(ImmutableList.of("0: 0", "1: 1", "2: 2"),             reduceUsingBi(closedRange(0, 2), 1));

    assertEquals(singletonList(   "0: 90"),                            reduceUsingBi(closedRange(90, 90), 1));
    assertEquals(ImmutableList.of("0: 90", "1: 91"),                   reduceUsingBi(closedRange(90, 91), 1));
    assertEquals(ImmutableList.of("0: 90", "1: 91", "2: 92"),          reduceUsingBi(closedRange(90, 92), 1));

    // Groups of 2
    assertEquals(singletonList(   "0: 0"),                             reduceUsingBi(closedRange(0, 0), 2));
    assertEquals(singletonList(   "0: 0_1"),                           reduceUsingBi(closedRange(0, 1), 2));
    assertEquals(ImmutableList.of("0: 0_1", "1: 2"),                   reduceUsingBi(closedRange(0, 2), 2));
    assertEquals(ImmutableList.of("0: 0_1", "1: 2_3"),                 reduceUsingBi(closedRange(0, 3), 2));
    assertEquals(ImmutableList.of("0: 0_1", "1: 2_3", "2: 4"),         reduceUsingBi(closedRange(0, 4), 2));

    assertEquals(singletonList(   "0: 90"),                            reduceUsingBi(closedRange(90, 90), 2));
    assertEquals(singletonList(   "0: 90_91"),                         reduceUsingBi(closedRange(90, 91), 2));
    assertEquals(ImmutableList.of("0: 90_91", "1: 92"),                reduceUsingBi(closedRange(90, 92), 2));
    assertEquals(ImmutableList.of("0: 90_91", "1: 92_93"),             reduceUsingBi(closedRange(90, 93), 2));
    assertEquals(ImmutableList.of("0: 90_91", "1: 92_93", "2: 94"),    reduceUsingBi(closedRange(90, 94), 2));

    // Groups of 4
    assertEquals(singletonList(   "0: 0"),                             reduceUsingBi(closedRange(0, 0), 4));
    assertEquals(singletonList(   "0: 0_1"),                           reduceUsingBi(closedRange(0, 1), 4));
    assertEquals(singletonList(   "0: 0_1_2"),                         reduceUsingBi(closedRange(0, 2), 4));
    assertEquals(singletonList(   "0: 0_1_2_3"),                       reduceUsingBi(closedRange(0, 3), 4));
    assertEquals(ImmutableList.of("0: 0_1_2_3", "1: 4"),               reduceUsingBi(closedRange(0, 4), 4));
    assertEquals(ImmutableList.of("0: 0_1_2_3", "1: 4_5"),             reduceUsingBi(closedRange(0, 5), 4));
    assertEquals(ImmutableList.of("0: 0_1_2_3", "1: 4_5_6"),           reduceUsingBi(closedRange(0, 6), 4));
    assertEquals(ImmutableList.of("0: 0_1_2_3", "1: 4_5_6_7"),         reduceUsingBi(closedRange(0, 7), 4));
    assertEquals(ImmutableList.of("0: 0_1_2_3", "1: 4_5_6_7", "2: 8"), reduceUsingBi(closedRange(0, 8), 4));

    assertEquals(singletonList(   "0: 90"),                                     reduceUsingBi(closedRange(90, 90), 4));
    assertEquals(singletonList(   "0: 90_91"),                                  reduceUsingBi(closedRange(90, 91), 4));
    assertEquals(singletonList(   "0: 90_91_92"),                               reduceUsingBi(closedRange(90, 92), 4));
    assertEquals(singletonList(   "0: 90_91_92_93"),                            reduceUsingBi(closedRange(90, 93), 4));
    assertEquals(ImmutableList.of("0: 90_91_92_93", "1: 94"),                   reduceUsingBi(closedRange(90, 94), 4));
    assertEquals(ImmutableList.of("0: 90_91_92_93", "1: 94_95"),                reduceUsingBi(closedRange(90, 95), 4));
    assertEquals(ImmutableList.of("0: 90_91_92_93", "1: 94_95_96"),             reduceUsingBi(closedRange(90, 96), 4));
    assertEquals(ImmutableList.of("0: 90_91_92_93", "1: 94_95_96_97"),          reduceUsingBi(closedRange(90, 97), 4));
    assertEquals(ImmutableList.of("0: 90_91_92_93", "1: 94_95_96_97", "2: 98"), reduceUsingBi(closedRange(90, 98), 4));
  }

  @Test
  public void testReduceInGroups_triFunctionOverload() {
    // 'Group' of 1
    assertEquals(singletonList(   "0: 0/0"),                           reduceUsingTri(closedRange(0, 0), 1));
    assertEquals(ImmutableList.of("0: 0/0", "1: 1/1"),                 reduceUsingTri(closedRange(0, 1), 1));
    assertEquals(ImmutableList.of("0: 0/0", "1: 1/1", "2: 2/2"),       reduceUsingTri(closedRange(0, 2), 1));

    assertEquals(singletonList(   "0: 90/90"),                         reduceUsingTri(closedRange(90, 90), 1));
    assertEquals(ImmutableList.of("0: 90/90", "1: 91/91"),             reduceUsingTri(closedRange(90, 91), 1));
    assertEquals(ImmutableList.of("0: 90/90", "1: 91/91", "2: 92/92"), reduceUsingTri(closedRange(90, 92), 1));

    // Groups of 2
    assertEquals(singletonList(   "0: 0/0"),                           reduceUsingTri(closedRange(0, 0), 2));
    assertEquals(singletonList(   "0: 0/1"),                           reduceUsingTri(closedRange(0, 1), 2));
    assertEquals(ImmutableList.of("0: 0/1", "1: 2/2"),                 reduceUsingTri(closedRange(0, 2), 2));
    assertEquals(ImmutableList.of("0: 0/1", "1: 2/3"),                 reduceUsingTri(closedRange(0, 3), 2));
    assertEquals(ImmutableList.of("0: 0/1", "1: 2/3", "2: 4/4"),       reduceUsingTri(closedRange(0, 4), 2));

    assertEquals(singletonList(   "0: 90/90"),                         reduceUsingTri(closedRange(90, 90), 2));
    assertEquals(singletonList(   "0: 90/91"),                         reduceUsingTri(closedRange(90, 91), 2));
    assertEquals(ImmutableList.of("0: 90/91", "1: 92/92"),             reduceUsingTri(closedRange(90, 92), 2));
    assertEquals(ImmutableList.of("0: 90/91", "1: 92/93"),             reduceUsingTri(closedRange(90, 93), 2));
    assertEquals(ImmutableList.of("0: 90/91", "1: 92/93", "2: 94/94"), reduceUsingTri(closedRange(90, 94), 2));

    // Groups of 4
    assertEquals(singletonList(   "0: 0/0"),                           reduceUsingTri(closedRange(0, 0), 4));
    assertEquals(singletonList(   "0: 0/1"),                           reduceUsingTri(closedRange(0, 1), 4));
    assertEquals(singletonList(   "0: 0/2"),                           reduceUsingTri(closedRange(0, 2), 4));
    assertEquals(singletonList(   "0: 0/3"),                           reduceUsingTri(closedRange(0, 3), 4));
    assertEquals(ImmutableList.of("0: 0/3", "1: 4/4"),                 reduceUsingTri(closedRange(0, 4), 4));
    assertEquals(ImmutableList.of("0: 0/3", "1: 4/5"),                 reduceUsingTri(closedRange(0, 5), 4));
    assertEquals(ImmutableList.of("0: 0/3", "1: 4/6"),                 reduceUsingTri(closedRange(0, 6), 4));
    assertEquals(ImmutableList.of("0: 0/3", "1: 4/7"),                 reduceUsingTri(closedRange(0, 7), 4));
    assertEquals(ImmutableList.of("0: 0/3", "1: 4/7", "2: 8/8"),       reduceUsingTri(closedRange(0, 8), 4));

    assertEquals(singletonList(   "0: 90/90"),                         reduceUsingTri(closedRange(90, 90), 4));
    assertEquals(singletonList(   "0: 90/91"),                         reduceUsingTri(closedRange(90, 91), 4));
    assertEquals(singletonList(   "0: 90/92"),                         reduceUsingTri(closedRange(90, 92), 4));
    assertEquals(singletonList(   "0: 90/93"),                         reduceUsingTri(closedRange(90, 93), 4));
    assertEquals(ImmutableList.of("0: 90/93", "1: 94/94"),             reduceUsingTri(closedRange(90, 94), 4));
    assertEquals(ImmutableList.of("0: 90/93", "1: 94/95"),             reduceUsingTri(closedRange(90, 95), 4));
    assertEquals(ImmutableList.of("0: 90/93", "1: 94/96"),             reduceUsingTri(closedRange(90, 96), 4));
    assertEquals(ImmutableList.of("0: 90/93", "1: 94/97"),             reduceUsingTri(closedRange(90, 97), 4));
    assertEquals(ImmutableList.of("0: 90/93", "1: 94/97", "2: 98/98"), reduceUsingTri(closedRange(90, 98), 4));
  }

  private List<String> reduceUsingBi(ClosedRange<Integer> range, int groupSize) {
    return reduceInGroups(
        range,
        groupSize,
        (groupNo, indices) -> Strings.format("%s: %s", groupNo, Joiner.on('_').join(indices.iterator())))
        .collect(Collectors.toList());
  }

  private List<String> reduceUsingTri(ClosedRange<Integer> range, int groupSize) {
    return reduceInGroups(
        range,
        groupSize,
        (groupNo, startIndexInclusive, endIndexInclusive) -> Strings.format("%s: %s/%s", groupNo, startIndexInclusive, endIndexInclusive))
        .collect(Collectors.toList());
  }

}
