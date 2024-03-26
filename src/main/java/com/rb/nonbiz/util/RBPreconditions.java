package com.rb.nonbiz.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBLists;
import com.rb.nonbiz.collections.RBMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBIterators.getFirstNonUniqueIteratorItem;
import static com.rb.nonbiz.collections.RBOptionals.findOnlyPresentOptional;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;

/**
 * Various static precondition methods (i.e. methods that are expected to throw an exception if their arguments are
 * not deemed valid) that do not fall under the other categories in the files called RB*Preconditions.
 */
public class RBPreconditions {

  private static final Pattern PATTERN_WITH_NON_CONTIGUOUS_TRUE = Pattern.compile("F*T+F+T+F*");

  /**
   * This is handy because it saves you from having to mention 'value' twice at the point where you're calling this,
   * plus it's a bit more explicit about what it checks.
   * This is for the case where you want an arbitrary error message; use the next overload for a default message.
   */
  public static <T extends Comparable<? super T>> void checkInRange(T value, Range<T> range, String format, Object...args) {
    RBPreconditions.checkArgument(
        range.contains(value),
        format,
        args);
  }

  /**
   * This is handy because it saves you from having to mention 'value' twice at the point where you're calling this,
   * plus it's a bit more explicit about what it checks.
   * Also, this saves you from having to create an error message; you get a reasonable default one.
   */
  public static <T extends Comparable<? super T>> void checkInRange(T value, Range<T> range) {
    RBPreconditions.checkArgument(
        range.contains(value),
        "value %s must be contained in range %s , but was not",
        value, range);
  }

  public static void checkValidArrayElement(int index, int arrayLength) {
    checkInRange(index, Range.closedOpen(0, arrayLength));
  }

  public static <T> void checkAllAreStrictSubclassesOf(
      Class<T> superclass, Iterable<? extends T> iterable, String format, Object...args) {
    iterable.forEach(subclassInstance -> RBPreconditions.checkArgument(
        superclass.isAssignableFrom(subclassInstance.getClass()) && !superclass.equals(subclassInstance.getClass()),
        format,
        args));
  }

  public static <T> void checkAllAreDifferentStrictSubclassesOf(
      Class<T> superclass, Iterable<? extends T> iterable, String format, Object...args) {
    checkAllAreStrictSubclassesOf(superclass, iterable, format, args);
    checkUnique(
        Iterators.transform(iterable.iterator(), v -> v.getClass()),
        format,
        args);
  }

  /**
   * Throws if neither or both optionals passed in are empty.
   * Otherwise, it returns the value inside the (only) non-empty optional.
   *
   * It just calls findOnlyPresentOptional, but it's good to have it as a precondition directly,
   * so it's easy to locate in the IDE's autocomplete when we're looking for an appropriate precondition.
   */
  public static <T> T checkExactlyOneOptionalIsNonEmpty(Optional<T> opt1, Optional<T> opt2) {
    return findOnlyPresentOptional(opt1, opt2);
  }

  public static void checkAtLeastOneOptionalIsPresent(Optional<?> opt1, Optional<?> opt2, Optional<?>...rest) {
    int numPresent = 0;
    if (opt1.isPresent()) {
      numPresent++;
    }
    if (opt2.isPresent()) {
      numPresent++;
    }
    for (Optional<?> opt : rest) {
      if (opt.isPresent()) {
        numPresent++;
      }
    }
    RBPreconditions.checkArgument(numPresent >= 1);
  }

  public static void checkAllOptionalsArePresentOrAllAreEmpty(Optional<?> opt1, Optional<?> opt2, Optional<?>...rest) {
    boolean firstIsPresent = opt1.isPresent();
    RBPreconditions.checkArgument(
        opt2.isPresent() == firstIsPresent,
        "All optionals must either be present or absent: %s %s %s",
        opt1, opt2, rest);
    for (Optional<?> opt : rest) {
      RBPreconditions.checkArgument(
          opt.isPresent() == firstIsPresent,
          "All optionals must either be present or absent: %s %s %s",
          opt1, opt2, rest);
    }
  }

  public static <T> void checkIsNonEmpty(
      Collection<T> collection, String format, Object...args) {
    checkArgument(!collection.isEmpty(), format, args);
  }

  public static <T> void checkIsNonEmpty(
      IidMap<T> iidMap, String format, Object...args) {
    checkArgument(!iidMap.isEmpty(), format, args);
  }

  @SafeVarargs
  public static <T> void checkUnique(T first, T second, T ... rest) {
    checkUnique(concatenateFirstSecondAndRest(first, second, rest));
  }

  public static <T> void checkUnique(List<T> list) {
    checkUnique(list, "Items in list must be unique but were not: %s", list);
  }

  public static <T> void checkUnique(Iterator<T> iterator) {
    checkUnique(iterator, "Items in list must be unique but were not");
  }

  public static <T> void checkUnique(Stream<T> stream) {
    checkUnique(stream, "Items in stream must be unique but were not");
  }

  public static <T> void checkUnique(List<T> list, String format, Object...args) {
    RBPreconditions.checkArgument(RBLists.listItemsAreUnique(list), format, args);
  }

  public static <T> void checkUnique(Iterator<T> iterator, String format, Object...args) {
    Optional<T> firstNonUniqueIteratorItem = getFirstNonUniqueIteratorItem(iterator);
    if (firstNonUniqueIteratorItem.isPresent()) {
      throw new IllegalArgumentException(
          smartFormat(
              "non-unique item of %s : %s",
              firstNonUniqueIteratorItem.get(),
              smartFormat(format, args)));
    }
  }

  public static <T> void checkUnique(Stream<T> stream, String format, Object...args) {
    checkUnique(stream.iterator(), format, args);
  }

  /**
   * Throws if a predicate ever has 'holes' in the entries where it's true.
   * In regex terms, this should throw if F* T+ F+ T+ F*
   * (the middle F+ being the 'hole').
   * This is useful for cases where we want to make sure that e.g. for whatever the start and end dates
   * of a stock index's lifetime is, we have information for a contiguous region.
   */
  public static <T> void checkNoHolesInPredicateBeingTrue(
      Iterator<T> iterator, Predicate<T> predicate, String format, Object...args) {
    // We could have a faster implementation that short-circuits the calculation if it encounters a problem,
    // but using regexes is much clearer - plus, the only time it would have better performance would be
    // if this is precondition is meant to fail, by which point we have bigger problems.
    StringBuilder sb = new StringBuilder();
    iterator.forEachRemaining(v -> sb.append(predicate.test(v) ? "T" : "F"));
    RBPreconditions.checkArgument(
        !PATTERN_WITH_NON_CONTIGUOUS_TRUE.matcher(sb.toString()).matches(),
        format, args);
  }

  public static void checkNonEmptyString(String s) {
    RBPreconditions.checkNotNull(s);
    RBPreconditions.checkArgument(!s.isEmpty());
  }

  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  public static void checkArgument(boolean expression, String format, Object ... args) {
    if (!expression) {
      throw new IllegalArgumentException(smartFormat(format, args));
    }
  }

  // copied from guava Preconditions
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    } else {
      return reference;
    }
  }

  public static <T> T checkNotNull(T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    } else {
      return reference;
    }
  }

  // copied from guava Preconditions
  public static <T> T checkNotNull(T reference, String errorMessageTemplate, Object... errorMessageArgs) {
    if (reference == null) {
      throw new NullPointerException(
          smartFormat(errorMessageTemplate, errorMessageArgs));
    } else {
      return reference;
    }
  }

  /**
   * This is a bit weird. It's just to keep semantics clean. Instead of having code that says
   * T doesNotThrow = someExpression();
   * ... and having a warning for an unused 'doesNotThrow' variable,
   * the semantics would be a bit cleaner if we say
   * checkDoesNotThrow(someExpression())
   *
   * Now, normally this would be done with a lambda. However, in this case, we want checkDoesNotThrow to throw
   * (just like any precondition) if and only iff the construction of the underlying expression throws.
   * So for a slight performance improvement, we won't pass in a lambda here. This means that if there's an exception,
   * it will be thrown BEFORE we get to checkDoesNotThrow. But that's fine; the end result will be the same.
   */
  public static <T> void checkDoesNotThrow(T expression) {
    ; // intentionally does nothing; see method comment.
  }

  /**
   * Throws if the {@link Runnable} does not throw an exception of the specified type.
   * Does nothing otherwise.
   */
  public static <E extends Exception> void checkThrowsThisException(
      Runnable runnable, Class<E> exceptionClass, String format, Object ... errorMessageArgs) {
    try {
      runnable.run();
    } catch (Exception e) {
      // The exception type thrown (e) should be the same as or a sub-class of the exception type we expect
      // Previous version was opposite
      boolean isCorrectExceptionType = exceptionClass.isAssignableFrom(e.getClass());// isAssignableFrom means 'is superclass of'

      if (!isCorrectExceptionType) {
        String originalMessage = smartFormat(format, errorMessageArgs);
        throw new IllegalArgumentException(smartFormat(
            "Expected an exception of type %s , but got one of type %s ; original error message is: %s",
            exceptionClass, e.getClass(), originalMessage));
      }
      // OK, all good; we got an exception, and it was of a proper type.
      return;
    }
    // If we got to this point, then it means that the runnable that was passed in
    // didn't throw an exception as expected (whether of the type we expected, or another exception type),
    // so that's wrong. Let's throw our own exception to indicate that.
    throw new IllegalArgumentException(smartFormat(format, errorMessageArgs));
  }

  /**
   * Throws if the {@link Runnable} throws an exception, but returns a cleaner error message.
   */
  public static <E extends Exception> void checkDoesNotThrowException(
      Runnable runnable, String format, Object ... errorMessageArgs) {
    try {
      runnable.run();
    } catch (Exception e) {
      throw new IllegalArgumentException(smartFormat(format, errorMessageArgs), e);
    }
  }

  /**
   * Throws if the {@link Runnable} does not throw an exception of the specified type.
   * Does nothing otherwise.
   */
  public static void checkThrows(Runnable runnable, String format, Object ... errorMessageArgs) {
    checkThrowsThisException(runnable, Exception.class, format, errorMessageArgs);
  }

}
