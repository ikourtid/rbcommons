package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.Pair;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBLists.listConcatenation;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_POSITIVE_INTEGER;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RBPreconditionsTest {

  @Test
  public void testCheckAllSame() {
    Function<Pair<Integer, String>, String> fieldExtractor = pair -> pair.getRight();
    assertIllegalArgumentException( () -> RBSimilarityPreconditions.checkAllSame(emptyList(), fieldExtractor));
    assertEquals("a", RBSimilarityPreconditions.checkAllSame(singletonList(pair(1, "a")), fieldExtractor));
    assertEquals("a", RBSimilarityPreconditions.checkAllSame(ImmutableList.of(pair(1, "a"), pair(2, "a")), fieldExtractor));
    assertEquals("a", RBSimilarityPreconditions.checkAllSame(ImmutableList.of(pair(1, "a"), pair(2, "a")).iterator(), fieldExtractor));
    assertIllegalArgumentException( () -> RBSimilarityPreconditions.checkAllSame(ImmutableList.of(pair(1, "a"), pair(2, "b")), fieldExtractor));
  }

  @Test
  public void testCheckAllSameUsingPredicate() {
    class PairWithoutEquals {
      final int x;
      final int y;

      PairWithoutEquals(int x, int y) {
        this.x = x;
        this.y = y;
      }
    };

    PairWithoutEquals pair1 = new PairWithoutEquals(100, 200);
    PairWithoutEquals pair2 = new PairWithoutEquals(100, 200);
    assertNotEquals(pair1, pair2); // because we didn't implement hashcode/equals, so the comparison uses pointer equality
    {
      Consumer<List<PairWithoutEquals>> checker = list -> RBSimilarityPreconditions.checkAllSameUsingPredicate(
          list.iterator(),
          identity(),
          (v1, v2) -> (v1.x == v2.x && v1.y == v2.y),
          "");
      // These are same using a value comparison (i.e. not pointer comparison)
      checker.accept(ImmutableList.of(pair1, pair2));
      checker.accept(ImmutableList.of(pair1, pair2, pair1));

      checker.accept(singletonList(pair1)); // Passes, since there's nothing that's unequal
      assertIllegalArgumentException( () -> checker.accept(emptyList())); // fails intentionally
    }
    {
      Consumer<List<PairWithoutEquals>> checker = list -> RBSimilarityPreconditions.checkAllSameUsingPredicate(
          list.iterator(), identity(), Object::equals, "");
      // These are NOT same using a pointer comparison (i.e. not value comparison)
      assertIllegalArgumentException( () -> checker.accept(ImmutableList.of(pair1, pair2)));
      assertIllegalArgumentException( () -> checker.accept(ImmutableList.of(pair1, pair2, pair1)));

      checker.accept(singletonList(pair1)); // Passes, since there's nothing that's unequal
      assertIllegalArgumentException( () -> checker.accept(emptyList())); // fails intentionally
    }
  }

  @Test
  public void testCheckConsecutive_usingIterables() {
    BiPredicate<Integer, Integer> isThreeX = (v1, v2) -> (v1 * 3 == v2);
    RBOrderingPreconditions.checkConsecutive(emptyList(), isThreeX);
    RBOrderingPreconditions.checkConsecutive(singletonList(DUMMY_POSITIVE_INTEGER), isThreeX);
    RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 30), isThreeX);
    RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 30, 90, 270, 810), isThreeX);
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 31), isThreeX));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 30, 91), isThreeX));
  }

  @Test
  public void testCheckConsecutive_usingIterators() {
    BiPredicate<Integer, Integer> isThreeX = (v1, v2) -> (v1 * 3 == v2);
    RBOrderingPreconditions.checkConsecutive(emptyIterator(), isThreeX);
    RBOrderingPreconditions.checkConsecutive(singletonList(DUMMY_POSITIVE_INTEGER).iterator(), isThreeX);
    RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 30).iterator(), isThreeX);
    RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 30, 90, 270, 810).iterator(), isThreeX);
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 31).iterator(), isThreeX));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkConsecutive(ImmutableList.of(10, 30, 91).iterator(), isThreeX));
  }

  @Test
  public void testCheckIncreasing_usingIterables() {
    RBOrderingPreconditions.checkIncreasing(Collections.<Integer>emptyList());
    RBOrderingPreconditions.checkIncreasing(singletonList(10));
    RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11));
    RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11, 12));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 10)));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 9)));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11, 11)));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11, 10)));
  }

  @Test
  public void testCheckIncreasing_usingIterators() {
    RBOrderingPreconditions.checkIncreasing(Collections.<Double>emptyIterator());
    RBOrderingPreconditions.checkIncreasing(singletonList(10).iterator());
    RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11).iterator());
    RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11, 12).iterator());
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 10).iterator()));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 9).iterator()));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11, 11).iterator()));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkIncreasing(ImmutableList.of(10, 11, 10).iterator()));
  }

  @Test
  public void testCheckExactlyOneOptionalIsNonEmpty() {
    assertIllegalArgumentException( () -> RBPreconditions.checkExactlyOneOptionalIsNonEmpty(Optional.empty(), Optional.empty()));
    assertIllegalArgumentException( () -> RBPreconditions.checkExactlyOneOptionalIsNonEmpty(Optional.of(1), Optional.of(2)));
    assertIllegalArgumentException( () -> RBPreconditions.checkExactlyOneOptionalIsNonEmpty(Optional.of(1), Optional.of(1)));
    RBPreconditions.checkExactlyOneOptionalIsNonEmpty(Optional.empty(), Optional.of(1));
    RBPreconditions.checkExactlyOneOptionalIsNonEmpty(Optional.of(1), Optional.empty());
  }

  @Test
  public void testCheckConsecutiveClosedDoubleRanges() {
    RBOrderingPreconditions.checkConsecutiveClosedDoubleRanges(emptyIterator(), 1e-8);
    RBOrderingPreconditions.checkConsecutiveClosedDoubleRanges(singleton(closedRange(1.1, 2.2)).iterator(), 1e-8);
    RBOrderingPreconditions.checkConsecutiveClosedDoubleRanges(
        ImmutableList.of(closedRange(1.1, 2.2), closedRange(2.2, 4.4)).iterator(), 1e-8);
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkConsecutiveClosedDoubleRanges(
        ImmutableList.of(closedRange(1.1, 2.2), closedRange(2.19, 4.4)).iterator(), 1e-8));
    assertIllegalArgumentException( () -> RBOrderingPreconditions.checkConsecutiveClosedDoubleRanges(
        ImmutableList.of(closedRange(1.1, 2.2), closedRange(2.21, 4.4)).iterator(), 1e-8));
  }

  @Test
  public void testCheckNoHolesInPredicateBeingTrue() {
    Predicate<Integer> isEven = v -> (v % 2 == 0);
    // What fails is F* T+ F+ T+ F*; these enumerate some reasonable sample counts of F (or T) for each of the 5
    // components of this regex.
    List<Integer> starSizes = ImmutableList.of(0, 1, 2);
    List<Integer> plusSizes = ImmutableList.of(1, 2);

    for (int sizeOfFirstF : starSizes) {
      for (int sizeOfSecondT : plusSizes) {
        for (int sizeOfThirdF : plusSizes) {
          for (int sizeOfFourthT : plusSizes) {
            for (int sizeOfFifthF : starSizes) {
              List<Integer> list = listConcatenation(
                  // 101 is not even, so it will be 'F'
                  Collections.nCopies(sizeOfFirstF,  101),
                  Collections.nCopies(sizeOfSecondT, 100),
                  Collections.nCopies(sizeOfThirdF,  101),
                  Collections.nCopies(sizeOfFourthT, 100),
                  Collections.nCopies(sizeOfFifthF,  101));
              assertIllegalArgumentException( () ->
                  RBPreconditions.checkNoHolesInPredicateBeingTrue(list.iterator(), isEven, DUMMY_STRING));
            }
          }
        }
      }
    }
    // However, F* T* F* (which intuitively seems like complement of the above, although I can't prove it)
    // must not throw.
    for (int sizeOfFirstF : starSizes) {
      for (int sizeOfSecondT : starSizes) {
        for (int sizeOfThirdF : starSizes) {
          List<Integer> list = listConcatenation(
              // 101 is not even, so it will be 'F'
              Collections.nCopies(sizeOfFirstF,  101),
              Collections.nCopies(sizeOfSecondT, 100),
              Collections.nCopies(sizeOfThirdF,  101));
          // i.e. this will not throw
          RBPreconditions.checkNoHolesInPredicateBeingTrue(list.iterator(), isEven, DUMMY_STRING);
        }
      }
    }
  }

  @Test
  public void testCheckAllAreDifferentStrictSubclassesOf() {
    class Super {};
    class Sub1 extends Super {};
    class Sub2 extends Super {};
    class SubSub extends Sub1 {};
    Consumer<List<? extends Super>> runner = list ->
        RBPreconditions.checkAllAreDifferentStrictSubclassesOf(
            Super.class, list, DUMMY_STRING, DUMMY_STRING);

    runner.accept(emptyList()); // does not throw
    runner.accept(singletonList(new Sub1())); // does not throw

    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Sub1(), new Sub1()))); // same
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Sub1(), new Sub2(), new Sub1()))); // same
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Sub2(), new Sub1(), new Sub1()))); // same

    // does not throw
    runner.accept(singletonList(new Sub1())); // too few
    runner.accept(ImmutableList.of(new Sub2(), new Sub1())); // all different
    runner.accept(ImmutableList.of(new Sub2(), new Sub1(), new SubSub())); // doesn't have to be all direct subclasses

    assertIllegalArgumentException( () -> runner.accept(singletonList(new Super())));
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Super(), new Sub1()))); // Super is not a strict subclass of Super
  }

  @Test
  public void testCheckAllAreStrictSubclassesOf() {
    class Super {};
    class Sub1 extends Super {};
    class Sub2 extends Super {};
    class SubSub extends Sub1 {};

    Consumer<List<? extends Super>> runner = list ->
        RBPreconditions.checkAllAreStrictSubclassesOf(
            Super.class, list, DUMMY_STRING, DUMMY_STRING);

    runner.accept(emptyList());
    runner.accept(singletonList(new Sub1()));
    runner.accept(ImmutableList.of(new Sub1(), new Sub2()));
    runner.accept(ImmutableList.of(new Sub1(), new Sub2(), new SubSub())); // it doesn't have to be only direct subclasses
    // Ideally, we would prohibit this, but the semantics of checkAllAreStrictSubclassesOf intentionally doesn't do that;
    // use checkAllAreDifferentStrictSubclassesOf if you want that behavior.
    runner.accept(ImmutableList.of(new Sub1(), new Sub1()));
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Super())));
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Super(), new Sub1())));
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Super(), new Sub1(), new Sub2())));
    assertIllegalArgumentException( () -> runner.accept(ImmutableList.of(new Super(), new Sub1(), new Sub1())));
  }

}
