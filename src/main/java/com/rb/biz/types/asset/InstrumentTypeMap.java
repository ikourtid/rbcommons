package com.rb.biz.types.asset;

import com.rb.biz.types.asset.InstrumentType.EtfInstrumentType;
import com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType;
import com.rb.biz.types.asset.InstrumentType.StockInstrumentType;
import com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType;
import com.rb.biz.types.asset.InstrumentType.Visitor;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rb.biz.types.asset.InstrumentType.EtfInstrumentType.etfInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType.mutualFundInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StockInstrumentType.stockInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType.structuredProductInstrumentType;

/**
 * <p> A special case of a map of InstrumentType {@code ->} some value. </p>
 *
 * <p> Unlike an {@code RBMap<InstrumentType, T>}, this forces you to specify values for all keys. </p>
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
   * Assuming this predicate matches exactly one of the (Sept 2019 currently 3) instrument types,
   * returns the appropriate InstrumentType.
   * If not exactly one, then it throws.
   */
  public InstrumentType getInstrumentTypeWhenUnique(Predicate<T> predicate) {
    boolean inEtfs               = predicate.test(valueForEtfs);
    boolean inStocks             = predicate.test(valueForStocks);
    boolean inMutualFunds        = predicate.test(valueForMutualFunds);
    boolean inStructuredProducts = predicate.test(valueForMutualFunds);
    RBPreconditions.checkArgument(
        Stream.of(inEtfs, inStocks, inMutualFunds, inStructuredProducts)
            .filter(v -> v)
            .count() == 1,
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
