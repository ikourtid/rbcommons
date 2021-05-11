package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class RBUtilitiesTest {

  @Test
  public void makeComparator() {
    class StringAndInt {

      String string;
      Integer number;

      public StringAndInt(String string, Integer number) {
        this.string = string;
        this.number = number;
      }

    }

    StringAndInt a1 = new StringAndInt("a", 1);
    StringAndInt a2 = new StringAndInt("a", 2);
    StringAndInt b1 = new StringAndInt("b", 1);
    StringAndInt b2 = new StringAndInt("b", 2);

    Comparator<StringAndInt> comparator = RBUtilities.<StringAndInt>makeComparator(
        stringAndInt -> stringAndInt.string,
        stringAndInt -> stringAndInt.number);
    List<StringAndInt> expectedSorting = ImmutableList.of(a1, a2, b1, b2);
    for (int i = 0; i < expectedSorting.size(); i++) {
      for (int j = 0; j < expectedSorting.size(); j++) {
        StringAndInt itemI = expectedSorting.get(i);
        StringAndInt itemJ = expectedSorting.get(j);
        if (i < j) {
          assertTrue(comparator.compare(itemI, itemJ) < 0);
          assertTrue(comparator.compare(itemJ, itemI) > 0);
        } else if (i > j) {
          assertTrue(comparator.compare(itemI, itemJ) > 0);
          assertTrue(comparator.compare(itemJ, itemI) < 0);
        } else {
          assertTrue(comparator.compare(itemI, itemJ) == 0);
          assertTrue(comparator.compare(itemJ, itemI) == 0);
        }
      }
    }
  }

}