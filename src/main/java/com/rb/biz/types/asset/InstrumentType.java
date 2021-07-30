package com.rb.biz.types.asset;

import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;

import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

/**
 * Tells us what kind of instrument we are dealing with (ETF, stock, mutual fund - as of Aug 2019).
 *
 * <p> The {@link InstrumentId} itself does not (and should not) record this information.
 *
 * <p> These could all have been enums, but using classes and visitors forces the code to handle all cases,
 * especially in case some get added later.
 *
 * <p> The current possibilities are:
 * <ul>
 *   <li> {@link EtfInstrumentType EtfInstrumentType}
 *   <li> {@link StockInstrumentType StockInstrumentType}
 *   <li> {@link MutualFundInstrumentType MutualFundInstrumentType}
 *   <li> {@link StructuredProductInstrumentType StructuredProductInstrumentType}</li>
 * </ul>
 */
public abstract class InstrumentType implements HasHumanReadableLabel {

  public interface Visitor<T> {

    T visitEtf(EtfInstrumentType etfInstrumentType);
    T visitStock(StockInstrumentType stockInstrumentType);
    T visitMutualFund(MutualFundInstrumentType mutualFundInstrumentType);
    T visitStructuredProduct(StructuredProductInstrumentType structuredProductInstrumentType);

  }

  public abstract <T> T visit(Visitor<T> visitor);

  /**
   * An ETF.
   *
   * <p> Technically this should be ETP, because it includes ETNs, but unfortunately we already use the term 'ETF'
   * instead of 'ETP' in various places (e.g. BasicSingleDaySingleEtfInfo), so we'll stick with this. </p>
   *
   * @see InstrumentType
   */
  public static class EtfInstrumentType extends InstrumentType {

    public static EtfInstrumentType etfInstrumentType() {
      return new EtfInstrumentType();
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitEtf(this);
    }

    @Override
    public HumanReadableLabel getHumanReadableLabel() {
      return label("ETF");
    }

  }

  /**
   * A stock.
   *
   * @see InstrumentType
   */
  public static class StockInstrumentType extends InstrumentType {

    public static StockInstrumentType stockInstrumentType() {
      return new StockInstrumentType();
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitStock(this);
    }

    @Override
    public HumanReadableLabel getHumanReadableLabel() {
      return label("stock");
    }

  }

  /**
   * A mutual fund.
   *
   * @see InstrumentType
   */
  public static class MutualFundInstrumentType extends InstrumentType {

    public static MutualFundInstrumentType mutualFundInstrumentType() {
      return new MutualFundInstrumentType();
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitMutualFund(this);
    }

    @Override
    public HumanReadableLabel getHumanReadableLabel() {
      return label("mutual_fund");
    }

  }


  /**
   * A structured product.
   *
   * @see InstrumentType
   */
  public static class StructuredProductInstrumentType extends InstrumentType {

    public static StructuredProductInstrumentType structuredProductInstrumentType() {
      return new StructuredProductInstrumentType();
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitStructuredProduct(this);
    }

    @Override
    public HumanReadableLabel getHumanReadableLabel() {
      return label("structured_product");
    }

  }


  @Override
  public String toString() {
    return getHumanReadableLabel().toString();
  }

}
