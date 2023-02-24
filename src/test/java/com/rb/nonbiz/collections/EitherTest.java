package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.collections.Either.Visitor;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EitherTest {

  @Test
  public void implementsEquals() {
    assertEquals(Either.left("a"),  Either.left("a"));
    assertEquals(Either.right("a"), Either.right("a"));
  }

  /**
   * An Either object does not have getters for its left and right components;
   * instead, the caller is expected to use a visitor.
   *
   * However, tests benefit from brevity, so we will have methods that assume the value on the left
   * (or right) is the one that's present, and throw otherwise.
   */
  public static <L, R> L getLeftFromEitherOrThrow(Either<L, R> either) {
    return either.visit(new Visitor<L, R, L>() {
      @Override
      public L visitLeft(L left) {
        return left;
      }

      @Override
      public L visitRight(R right) {
        throw new IllegalArgumentException(smartFormat(
            "Expected an Either.left, but there is a right value of %s", right));
      }
    });
  }

  /**
   * An Either object does not have getters for its left and right components;
   * instead, the caller is expected to use a visitor.
   *
   * However, tests benefit from brevity, so we will have methods that assume the value on the left
   * (or right) is the one that's present, and throw otherwise.
   */
  public static <L, R> R getRightFromEitherOrThrow(Either<L, R> either) {
    return either.visit(new Visitor<L, R, R>() {
      @Override
      public R visitLeft(L left) {
        throw new IllegalArgumentException(smartFormat(
            "Expected an Either.right, but there is a left value of %s", left));
      }

      @Override
      public R visitRight(R right) {
        return right;
      }
    });
  }

  @Test
  public void hasLeft_visitorHitsLeft() {
    Either<String, Double> either = Either.left("abc");
    assertEquals(
        ImmutableList.of(1, 2),
        either.visit(new Either.Visitor<String, Double, List<Integer>>() {
          @Override
          public List<Integer> visitLeft(String left) {
            return ImmutableList.of(1, 2);
          }

          @Override
          public List<Integer> visitRight(Double right) {
            return ImmutableList.of(3, 4);
          }
        }));
  }

  @Test
  public void hasRight_visitorHitsRight() {
    Either<String, Set<Integer>> either = Either.right(ImmutableSet.of(10, 20));
    int result = either.visit(new Either.Visitor<String, Set<Integer>, Integer>() {
      @Override
      public Integer visitLeft(String left) {
        return 987;
      }

      @Override
      public Integer visitRight(Set<Integer> right) {
        return 123;
      }
    });
    assertEquals(123, result);
  }

  @Test
  public void sameTypesEverywhere() {
    Either.Visitor<String, String, String> visitor = new Either.Visitor<String, String, String>() {
      @Override
      public String visitLeft(String left) {
        return "xxx_" + left;
      }

      @Override
      public String visitRight(String right) {
        return "yyy_" + right;
      }
    };
    Either<String, String> leftOnly = Either.left("l");
    Either<String, String> rightOnly = Either.right("r");
    assertEquals("xxx_l", leftOnly.visit(visitor));
    assertEquals("yyy_r", rightOnly.visit(visitor));
  }

  @Test
  public void testEquals() {
    assertEquals(Either.left(123), Either.left(123));
    assertEquals(Either.left("abc"), Either.left("abc"));
    assertEquals(Either.right(123), Either.right(123));
    assertEquals(Either.right("abc"), Either.right("abc"));
    assertNotEquals(Either.left("abc"), Either.left(123));
    assertNotEquals(Either.left("abc"), Either.right("abc"));
  }
  
}
