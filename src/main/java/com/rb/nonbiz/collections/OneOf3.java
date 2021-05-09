package com.rb.nonbiz.collections;


import com.rb.nonbiz.text.Strings;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.optionalsEqual;

/**
 * A mini-collection that represents a triple where exactly one of the objects exists,
 * i.e. we're not allowed to have 0, 2, or 3 of them be specified.
 *
 * <p> This is a generalization of {@link Either}, but we won't rename Either to OneOf2, because it is fairly widely used
 * in the code and is therefore already established. </p>
 */
public class OneOf3<T1, T2, T3> {

  private final Optional<T1> value1;
  private final Optional<T2> value2;
  private final Optional<T3> value3;

  private OneOf3(Optional<T1> value1, Optional<T2> value2, Optional<T3> value3) {
    this.value1 = value1;
    this.value2 = value2;
    this.value3 = value3;
  }

  public static <T1, T2, T3> OneOf3<T1, T2, T3> only1stOf3(T1 value1) {
    return new OneOf3<>(Optional.of(value1), Optional.empty(), Optional.empty());
  }

  public static <T1, T2, T3> OneOf3<T1, T2, T3> only2ndOf3(T2 value2) {
    return new OneOf3<>(Optional.empty(), Optional.of(value2), Optional.empty());
  }

  public static <T1, T2, T3> OneOf3<T1, T2, T3> only3rdOf3(T3 value3) {
    return new OneOf3<>(Optional.empty(), Optional.empty(), Optional.of(value3));
  }

  // Do not use this; it's here to aid the test matcher
  Optional<T1> getValue1() {
    return value1;
  }

  // Do not use this; it's here to aid the test matcher
  Optional<T2> getValue2() {
    return value2;
  }

  // Do not use this; it's here to aid the test matcher
  Optional<T3> getValue3() {
    return value3;
  }

  public interface Visitor<T1, T2, T3, T> {

    T visitOnly1stOf3(T1 value1);
    T visitOnly2ndOf3(T2 value2);
    T visitOnly3rdOf3(T3 value3);

  }

  public <T> T visit(Visitor<T1, T2, T3, T> visitor) {
    return value1.isPresent() ? visitor.visitOnly1stOf3(value1.get())
         : value2.isPresent() ? visitor.visitOnly2ndOf3(value2.get())
                              : visitor.visitOnly3rdOf3(value3.get());
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toSimpleString() {
    return toString(false);
  }

  private String toString(boolean clarifyItIsAnEither) {
    return visit(new Visitor<T1, T2, T3, String>() {
      @Override
      public String visitOnly1stOf3(T1 value1) {
        return clarifyItIsAnEither
            ? Strings.format("only 1st of 3: %s", value1.toString())
            : value1.toString();
      }

      @Override
      public String visitOnly2ndOf3(T2 value2) {
        return clarifyItIsAnEither
            ? Strings.format("only 2nd of 3: %s", value2.toString())
            : value2.toString();
      }

      @Override
      public String visitOnly3rdOf3(T3 value3) {
        return clarifyItIsAnEither
            ? Strings.format("only 3rd of 3: %s", value3.toString())
            : value3.toString();
      }
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    OneOf3<?, ?, ?> other = (OneOf3<?, ?, ?>) o;
    if (value1.getClass() != other.value1.getClass()) {
      return false;
    }
    if (value2.getClass() != other.value2.getClass()) {
      return false;
    }
    if (value3.getClass() != other.value3.getClass()) {
      return false;
    }

    return optionalsEqual(value1, other.value1)
        && optionalsEqual(value2, other.value2)
        && optionalsEqual(value3, other.value3);
  }

  // not IDE-generated
  @Override
  public int hashCode() {
    return visit(new Visitor<T1, T2, T3, Integer>() {
      @Override
      public Integer visitOnly1stOf3(T1 value1) {
        return value1.hashCode();
      }

      @Override
      public Integer visitOnly2ndOf3(T2 value2) {
        return value2.hashCode();
      }

      @Override
      public Integer visitOnly3rdOf3(T3 value3) {
        return value3.hashCode();
      }
    });
  }

}
