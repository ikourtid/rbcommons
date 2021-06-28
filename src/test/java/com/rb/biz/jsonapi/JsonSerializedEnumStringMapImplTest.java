package com.rb.biz.jsonapi;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;

import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImpl.jsonSerializedEnumStringMap;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImpl.jsonSerializedEnumStringMapImpl;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum.VALUE1;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum.VALUE2;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum.VALUE3;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SingletonSerializationTestEnum.SINGLE_VALUE;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class JsonSerializedEnumStringMapImplTest
    extends RBTestMatcher<JsonSerializedEnumStringMap<SerializationTestEnum>> {

  protected enum SerializationTestEnum {

    VALUE1,
    VALUE2,
    VALUE3

  }


  protected enum EmptySerializationTestEnum {}

  protected enum SingletonSerializationTestEnum {

    SINGLE_VALUE

  }


  @Test
  public void emptyOrSingletonEnumMap_throws() {
    assertIllegalArgumentException( () -> jsonSerializedEnumStringMapImpl(
        EmptySerializationTestEnum.class,
        HashBiMap.create(ImmutableMap.of())));
    assertIllegalArgumentException( () -> jsonSerializedEnumStringMapImpl(
        SingletonSerializationTestEnum.class,
        HashBiMap.create(ImmutableMap.of(SINGLE_VALUE, "dummy"))));
  }

  @Test
  public void notAllEnumConstantsHaveValues_throws() {
    assertIllegalArgumentException( () -> jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "v1",
        VALUE3, "v3"));
  }

  @Override
  public JsonSerializedEnumStringMap<SerializationTestEnum> makeTrivialObject() {
    return jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "v1",
        VALUE2, "v2",
        VALUE3, "v3");
  }

  @Override
  public JsonSerializedEnumStringMap<SerializationTestEnum> makeNontrivialObject() {
    return jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "val1",
        VALUE2, "val2",
        VALUE3, "val3");
  }

  @Override
  public JsonSerializedEnumStringMap<SerializationTestEnum> makeMatchingNontrivialObject() {
    return jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "val1",
        VALUE2, "val2",
        VALUE3, "val3");
  }

  @Override
  protected boolean willMatch(JsonSerializedEnumStringMap<SerializationTestEnum> expected,
                              JsonSerializedEnumStringMap<SerializationTestEnum> actual) {
    return jsonSerializedEnumStringMapMatcher(expected).matches(actual);
  }

  public static <E extends Enum<E>> TypeSafeMatcher<JsonSerializedEnumStringMap<E>>
  jsonSerializedEnumStringMapMatcher(JsonSerializedEnumStringMap<E> expected) {
    return makeMatcher(expected, actual ->
        expected.getEnumClass().equals(actual.getEnumClass())
            && Arrays.stream(expected.getEnumClass().getEnumConstants())
            .allMatch(enumConstant ->
                expected.getSerializationStringOrThrow(enumConstant).equals(
                    actual.getSerializationStringOrThrow(enumConstant))));
  }

}
