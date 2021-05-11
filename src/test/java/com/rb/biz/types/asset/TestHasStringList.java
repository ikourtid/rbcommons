package com.rb.biz.types.asset;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class TestHasStringList implements HasList<String> {

  private final List<String> listOfStrings;

  private TestHasStringList(List<String> listOfStrings) {
    this.listOfStrings = listOfStrings;
  }

  public static TestHasStringList emptyTestHasStringList() {
    return new TestHasStringList(emptyList());
  }

  public static TestHasStringList singletonTestHasStringList(String onlyItem) {
    return new TestHasStringList(singletonList(onlyItem));
  }

  public static TestHasStringList testHasStringListOf(String first, String second, String... rest) {
    return new TestHasStringList(concatenateFirstSecondAndRest(first, second, rest));
  }

  @Override
  public List<String> getList() {
    return listOfStrings;
  }

}
