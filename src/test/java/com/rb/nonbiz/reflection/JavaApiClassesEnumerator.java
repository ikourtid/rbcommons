package com.rb.nonbiz.reflection;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.rb.biz.investing.namedfactormodel.NamedFactor;
import com.rb.biz.investing.strategy.BaseInvestorSettings;
import com.rb.biz.investing.strategy.CompleteInvestorInputs;
import com.rb.biz.investing.strategy.Investor;
import com.rb.nonbiz.collections.MutableRBSet;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.math.eigen.FactorLoadings;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.reflection.UniqueClassesWithDepth.uniqueClassesWithDepth;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;

/**
 * This is not really a test. It's a class that has to live in test because it accesses both production and test
 * top-level classes.
 * The goal is to be able to print out classes with certain properties, e.g. with an annotation present.
 * For example, this will be helpful in identifying classes whose source code we want to include in a jar file,
 * for cases where we license the core investing engine as a library (binary).
 */
public class JavaApiClassesEnumerator {

  private static final RBSet<Class<?>> DO_NOT_SHOW_THESE_CLASSES = newRBSet(ImmutableList.of(
      BigDecimal.class,
      Boolean.class,
      Comparable.class,
      Integer.class,
      List.class,

      LocalDate.class,
      LocalDateTime.class,
      LocalTime.class,
      Long.class,
      Map.class,

      Object.class,
      Optional.class,
      OptionalInt.class,
      Ordering.class,
      Range.class,

      Set.class,
      String.class,
      TLongObjectHashMap.class,
      ToDoubleFunction.class));

  private final AllClassesThatImplementInterfacesRetriever allClassesThatImplementInterfacesRetriever =
      makeRealObject(AllClassesThatImplementInterfacesRetriever.class);
  private final AllRbTopLevelClassesRetriever allRbTopLevelClassesRetriever =
      makeRealObject(AllRbTopLevelClassesRetriever.class);
  private ClassesWithDepthMultilineStringFormatter classesWithDepthMultilineStringFormatter =
      makeRealObject(ClassesWithDepthMultilineStringFormatter.class);
  private final DataClassRecursiveEnumerator dataClassRecursiveEnumerator =
      makeRealObject(DataClassRecursiveEnumerator.class);

  @Ignore("We should only run this manually whenever we want to generate the list of files to include javadoc for")
  @Test
  public void enumerateDataClassesInApi() throws IOException {
    enumerateAll(
        // Never show these.
        DO_NOT_SHOW_THESE_CLASSES,

        // A good source of names of classes that we should show here is the top-level class CompleteInvestorInputs.
        rbSetOf(
            CompleteInvestorInputs.class,
            NamedFactor.class,
            FactorLoadings.class),

        // Show all of the 9 (currently - April 2021) subclasses that extend InvestorSettings.
        singletonRBSet(BaseInvestorSettings.class),

        // Show all verb classes that implement the Investor interface.
        singletonRBSet(Investor.class));
  }

  private void enumerateAll(
      RBSet<Class<?>> nonRowboatClassesToExclude,
      RBSet<Class<?>> classesToShowRecursively,
      RBSet<Class<?>> showSubclassesOfTheseClassesRecursively,
      RBSet<Class<?>> showImplementersOfTheseInterfacesNonRecursively) {
    MutableRBSet<Class<?>> encountered = newMutableRBSetWithExpectedSize(10_000);
    encountered.addAll(nonRowboatClassesToExclude.asSet());

    BiConsumer<Stream<Class<?>>, Function<Class<?>, UniqueClassesWithDepth>> subcasePrinter =
        (classStream, retriever) -> {
          List<ClassWithDepth> mutableClassesWithDepth = newArrayList();
          classStream.forEach(clazz -> {
            UniqueClassesWithDepth uniqueClassesWithDepth = retriever.apply(clazz);
            uniqueClassesWithDepth.getRawList().forEach(classWithDepth -> {
              encountered.add(classWithDepth.getClassObject());
              mutableClassesWithDepth.add(classWithDepth);
            });
          });
          System.out.println(classesWithDepthMultilineStringFormatter.format(
              uniqueClassesWithDepth(mutableClassesWithDepth),
              "  "));
        };

    System.out.format("# Showing recursively all subclasses of %s\n",
        Joiner.on(" , ").join(classesToShowRecursively));
    subcasePrinter.accept(
        classesToShowRecursively.stream(),
        clazz -> dataClassRecursiveEnumerator.enumerateRecursively(clazz, newRBSet(encountered)));

    AllRbTopLevelClasses allRbTopLevelClasses = allRbTopLevelClassesRetriever.retrieve();

    System.out.format("# Showing recursively all subclasses of %s\n",
        Joiner.on(" , ").join(showSubclassesOfTheseClassesRecursively));
    // This is not an efficient search, but this method is not in production, and it will barely get called in test,
    // so it's OK.
    subcasePrinter.accept(
        allRbTopLevelClasses.getRawSet()
            .stream()
            .filter(clazz -> showSubclassesOfTheseClassesRecursively
                .stream()
                .anyMatch(superClass -> superClass.isAssignableFrom(clazz))),
        clazz -> dataClassRecursiveEnumerator.enumerateRecursively(clazz, newRBSet(encountered)));

    System.out.format("# also, showing NON-recursively all implementers of %s\n",
        Joiner.on(" , ").join(showImplementersOfTheseInterfacesNonRecursively));
    subcasePrinter.accept(
        allClassesThatImplementInterfacesRetriever
            .retrieve(allRbTopLevelClasses, showImplementersOfTheseInterfacesNonRecursively)
            .stream(),
        clazz -> dataClassRecursiveEnumerator.enumerateNonRecursively(clazz, newRBSet(encountered)));
  }

}
