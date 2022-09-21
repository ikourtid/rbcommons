package com.rb.nonbiz.reflection;

import com.google.common.base.Joiner;
import com.rb.nonbiz.collections.RBStreams;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * A generalization of {@link Class} that also gives the class information for the classes 'inside the angle brackets'
 * in the case of generics.
 *
 * <p> Java supports {@link Class} objects as part of its reflection API.
 * However, because generics were not part of the original Java specification and got added later,
 * Java uses "type erasure" (look it up) and doesn't have any knowledge at runtime about the generic class arguments.
 * That is, I can call {@link Class#getClass()} on a runtime object of type {@code Foo<X, Y>} and get Foo.class,
 * but there's no way to get X.class or Y.class. This class allows it.
 * </p>
 */
public class RBClass<T> {

  private final Class<T> outerClass;
  private final List<RBClass<?>> innerClassRBClasses;

  private RBClass(
      Class<T> outerClass,
      List<RBClass<?>> innerClassRBClasses) {
    this.outerClass = outerClass;
    this.innerClassRBClasses = innerClassRBClasses;
  }

  public static <T> RBClass<T> rbClass(
      Class<T> outerClass,
      RBClass<?> first,
      RBClass<?> ... rest) {
    return new RBClass<>(outerClass, concatenateFirstAndRest(first, rest));
  }

  /**
   * Constructs a {@link RBClass} for a generic class, where the arguments inside the
   * angle brackets are non-generic classes.
   *
   * <p> 'Shallow generic' is not a real term, but hopefully it's intuitive. </p>
   */
  public static <T> RBClass<T> shallowGenericRbClass(
      Class<T> outerClass,
      Class<?> first,
      Class<?> ... rest) {
    return new RBClass<>(outerClass, RBStreams.concatenateFirstAndRest(first, rest)
        .map(v -> nonGenericRbClass(v))
        .collect(Collectors.toList()));
  }

  /**
   * Represents e.g. a {@code UniqueId<Partition<AssetClass>>} or some other situation where the class inside the
   * {@link UniqueId} is also generic (in addition to {@link UniqueId}, which is generic).
   */
  // Unfortunately, we ned this convoluted code to cast to a Class<UniqueId<T>>.
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> RBClass<UniqueId<T>> uniqueIdRbClass(RBClass<T> classOfUniqueId) {
    return new RBClass<UniqueId<T>>(
        (Class) UniqueId.class,
        singletonList(classOfUniqueId));
  }

  /**
   * Represents e.g. a {@code UniqueId<NamedFactor>} or some other situation where the class inside the
   * {@link UniqueId} is not itself generic (even though obviously {@link UniqueId} is).
   */
  public static <T> RBClass<UniqueId<T>> uniqueIdRbClass(Class<T> classOfUniqueId) {
    return uniqueIdRbClass(nonGenericRbClass(classOfUniqueId));
  }

    public static <T> RBClass<T> nonGenericRbClass(
      Class<T> outerClass) {
    return new RBClass<>(outerClass, emptyList());
  }

  public Class<?> getOuterClass() {
    return outerClass;
  }

  public List<RBClass<?>> getInnerClassRBClasses() {
    return innerClassRBClasses;
  }

  public Stream<Class<?>> getAllClassesAsStream() {
    return Stream.concat(
        Stream.of(outerClass),
        innerClassRBClasses
            .stream()
            .flatMap(v -> v.getAllClassesAsStream()));
  }

  // We do this trick to avoid nesting our usual toString 'tags' (here, "CDAG").
  public String toStringWithoutTags() {
    return !isGeneric()
        ? outerClass.getSimpleName()
        : Strings.format("%s < %s >", outerClass.getSimpleName(), Joiner.on(" , ").join(
            innerClassRBClasses.stream().map(v -> v.toStringWithoutTags()).iterator()));
  }

  public boolean isGeneric() {
    return !innerClassRBClasses.isEmpty();
  }

  @Override
  public String toString() {
    // We use Class#getSimpleName because our code avoids having classes with the same name but in different packages,
    // so there's rarely any ambiguity.
    // The following will result in strings such as:
    // "[CDAG RBMap < String , IidMap < UnitFraction > > CDAG]"
    // ... with spaces between the corner brackets, whereas typically generic classes in Java code are written as
    // RBMap<String, IidMap<UnitFraction>>
    // However, we have a convention to use spaces in generated strings around any item that is variable, i.e. not
    // part of the fixed text in the first string argument of String#format.
    return !isGeneric()
        ? Strings.format("[CDAG %s CDAG]", outerClass.getSimpleName())
        : Strings.format("[CDAG %s < %s > CDAG]", outerClass.getSimpleName(), Joiner.on(" , ").join(
            innerClassRBClasses.stream().map(v -> v.toStringWithoutTags()).iterator()));
  }

  // IDE-generated. We don't normally implement equals & hashCode, but RBClass can reasonably be used as a key
  // in a map, so it should have non-trivial implementation for those methods.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RBClass<?> rbClass = (RBClass<?>) o;
    return outerClass.equals(rbClass.outerClass) && innerClassRBClasses.equals(rbClass.innerClassRBClasses);
  }

  // IDE-generated; see #equals above.
  @Override
  public int hashCode() {
    return Objects.hash(outerClass, innerClassRBClasses);
  }

}
