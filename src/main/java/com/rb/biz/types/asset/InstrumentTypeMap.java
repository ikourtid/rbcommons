package com.rb.biz.types.asset;

import com.rb.biz.types.asset.InstrumentType.EtfInstrumentType;
import com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType;
import com.rb.biz.types.asset.InstrumentType.StockInstrumentType;
import com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType;
import com.rb.biz.types.asset.InstrumentType.Visitor;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.IidSets;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rb.biz.types.asset.InstrumentType.EtfInstrumentType.etfInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType.mutualFundInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StockInstrumentType.stockInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType.structuredProductInstrumentType;

/**
 * A special case of a map of {@link InstrumentType} to some value.
 *
 * <p> Unlike an {@link RBMap}, this forces you to specify values for all keys, in the sense that when you use it,
 * if your requested instrument is not in this map, it will always throw an exception.
 * There is no equivalent of {@link RBMap#getOptional(Object)}. </p>
 *
 * @see InstrumentType
 */
public class InstrumentTypeMap<T> {

  private final T valueForEtfs;
  private final T valueForStocks;
  private final T valueForMutualFunds;
  private final T valueForStructuredProducts;
  // We rarely do this, but instantiating a visitor every time we want to access this map may hurt performance,
  // so let's just save this at construction time.
  private final Visitor<T> visitor;

  private InstrumentTypeMap(
      T valueForEtfs, T valueForStocks, T valueForMutualFunds, T valueForStructuredProducts, Visitor<T> visitor) {
    this.valueForEtfs = valueForEtfs;
    this.valueForStocks = valueForStocks;
    this.valueForMutualFunds = valueForMutualFunds;
    this.valueForStructuredProducts = valueForStructuredProducts;
    this.visitor = visitor;
  }

  public static <T> InstrumentTypeMap<T> instrumentTypeMapWithSharedDefaults(T sharedDefaultValueForAllInstrumentTypes) {
    return InstrumentTypeMapBuilder.<T>instrumentTypeMapBuilder()
        .setValueForEtfs(              sharedDefaultValueForAllInstrumentTypes)
        .setValueForStocks(            sharedDefaultValueForAllInstrumentTypes)
        .setValueForMutualFunds(       sharedDefaultValueForAllInstrumentTypes)
        .setValueForStructuredProducts(sharedDefaultValueForAllInstrumentTypes)
        .build();
  }

  /**
   * A common use case is when we store an RBMap under each instrument type,
   * in which case we want to ensure each key appears under only one instrument type.
   *
   * <p> This cannot be part of the builder below, because the generic class T is not guaranteed to have
   * this notion of being able to extract a set of keys out of it. For all we know, T could be just a string.
   * So this method has to be separate and invoked when applicable. </p>
   */
  public static <K, T> void checkInstrumentTypeMapHasNoDuplicates(
      InstrumentTypeMap<T> instrumentTypeMap,
      Function<T, RBSet<K>> keysExtractor) {
    RBPreconditions.checkArgument(
        RBSets.noSharedItems(
            keysExtractor.apply(instrumentTypeMap.valueForEtfs),
            keysExtractor.apply(instrumentTypeMap.valueForStocks),
            keysExtractor.apply(instrumentTypeMap.valueForMutualFunds),
            keysExtractor.apply(instrumentTypeMap.valueForStructuredProducts)),
        "The keys (possibly InstrumentIds) under each InstrumentType must be unique: %s",
        instrumentTypeMap);
  }

  /**
   * A common use case is when we store an {@link IidMap} under each instrument type,
   * in which case we want to ensure each instrument appears under only one instrument type.
   *
   * <p> This cannot be part of the builder below, because the generic class T is not guaranteed to have
   * this notion of being able to extract a set of keys out of it. For all we know, T could be just a string.
   * So this method has to be separate and invoked when applicable. </p>
   */
  public static <T> void checkInstrumentTypeMapHasNoDuplicateInstruments(
      InstrumentTypeMap<T> instrumentTypeMap,
      Function<T, IidSet> iidSetExtractor) {
    RBPreconditions.checkArgument(
        IidSets.noSharedIids(
            iidSetExtractor.apply(instrumentTypeMap.valueForEtfs),
            iidSetExtractor.apply(instrumentTypeMap.valueForStocks),
            iidSetExtractor.apply(instrumentTypeMap.valueForMutualFunds),
            iidSetExtractor.apply(instrumentTypeMap.valueForStructuredProducts)),
        "The InstrumentIds under each InstrumentType must be unique: %s",
        instrumentTypeMap);
  }

    public T get(InstrumentType instrumentType) {
    return instrumentType.visit(visitor);
  }

  public T getValueForEtfs() {
    return valueForEtfs;
  }

  public T getValueForStocks() {
    return valueForStocks;
  }

  public T getValueForMutualFunds() {
    return valueForMutualFunds;
  }

  public T getValueForStructuredProducts() {
    return valueForStructuredProducts;
  }

  // The order is not important here, but exposing as #stream() will allow us to do things such as allMatch etc.
  public Stream<T> stream() {
    return Stream.of(valueForEtfs, valueForStocks, valueForMutualFunds, valueForStructuredProducts);
  }

  /**
   * Assuming this predicate matches exactly one of the (Apr 2024 currently 4) instrument types,
   * returns the appropriate InstrumentType.
   * If not exactly one, then it throws.
   */
  public InstrumentType getInstrumentTypeWhenUnique(Predicate<T> predicate) {
    boolean inEtfs               = predicate.test(valueForEtfs);
    boolean inStocks             = predicate.test(valueForStocks);
    boolean inMutualFunds        = predicate.test(valueForMutualFunds);
    boolean inStructuredProducts = predicate.test(valueForStructuredProducts);
    RBPreconditions.checkArgument(
        Stream.of(inEtfs, inStocks, inMutualFunds, inStructuredProducts)
            .filter(v -> v) // only keep the 'true' boolean values...
            .count() == 1,  // ... so that we can count them.
        "Predicate passed in must be true for exactly one category of {ETFs, stocks, mutual funds, structured products}; %s",
        this.toString());
    // This style of relying on a default is not great, but here we already checked that one of 4 booleans is true,
    // so when we hit the last line with the default case, we know we're dealing with a mutual fund.
    return inEtfs     ? etfInstrumentType() :
        inStocks      ? stockInstrumentType() :
        inMutualFunds ? mutualFundInstrumentType() :
                        structuredProductInstrumentType();
  }

  @Override
  public String toString() {
    return Strings.format("[ITM etf= %s ; stock= %s ; mutual fund= %s ; structured prod= %s ITM]",
        valueForEtfs, valueForStocks, valueForMutualFunds, valueForStructuredProducts);
  }


  /**
   * An {@link RBBuilder} for a {@link InstrumentTypeMap}.
   */
  public static class InstrumentTypeMapBuilder<T> implements RBBuilder<InstrumentTypeMap<T>> {

    private T valueForEtfs;
    private T valueForStocks;
    private T valueForMutualFunds;
    private T valueForStructuredProducts;

    private InstrumentTypeMapBuilder() {}

    public static <T> InstrumentTypeMapBuilder<T> instrumentTypeMapBuilder() {
      return new InstrumentTypeMapBuilder<>();
    }

    public InstrumentTypeMapBuilder<T> setValueForEtfs(T valueForEtfs) {
      this.valueForEtfs = checkNotAlreadySet(this.valueForEtfs, valueForEtfs);
      return this;
    }

    public InstrumentTypeMapBuilder<T> setValueForStocks(T valueForStocks) {
      this.valueForStocks = checkNotAlreadySet(this.valueForStocks, valueForStocks);
      return this;
    }

    public InstrumentTypeMapBuilder<T> setValueForMutualFunds(T valueForMutualFunds) {
      this.valueForMutualFunds = checkNotAlreadySet(this.valueForMutualFunds, valueForMutualFunds);
      return this;
    }

    public InstrumentTypeMapBuilder<T> setValueForStructuredProducts(T valueForStructuredProducts) {
      this.valueForStructuredProducts = checkNotAlreadySet(this.valueForStructuredProducts, valueForStructuredProducts);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(valueForEtfs);
      RBPreconditions.checkNotNull(valueForStocks);
      RBPreconditions.checkNotNull(valueForMutualFunds);
      RBPreconditions.checkNotNull(valueForStructuredProducts);
    }

    @Override
    public InstrumentTypeMap<T> buildWithoutPreconditions() {
      return new InstrumentTypeMap<>(
          valueForEtfs,
          valueForStocks,
          valueForMutualFunds,
          valueForStructuredProducts,
          new Visitor<T>() {
            @Override
            public T visitEtf(EtfInstrumentType etfInstrumentType) {
              return valueForEtfs;
            }

            @Override
            public T visitStock(StockInstrumentType stockInstrumentType) {
              return valueForStocks;
            }

            @Override
            public T visitMutualFund(MutualFundInstrumentType mutualFundInstrumentType) {
              return valueForMutualFunds;
            }

            @Override
            public T visitStructuredProduct(StructuredProductInstrumentType structuredProductInstrumentType) {
              return valueForStructuredProducts;
            }
          });
    }
  }

}
