package com.rb.nonbiz.text;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBSet;

import java.util.Collection;

/**
 * This class is to help exception messages automatically print information about the {@link Symbol}
 * corresponding to an {@link InstrumentId}, without the caller needing to do anything special.
 *
 * <p> Unfortunately, because of Guice dependency injection, we need some trickery to get this to work.
 * We had to create a separate class {@link SmartFormatterHelper}. Its methods are package-private, and its
 * class name is unorthodox (it ends in 'Helper'), so it should be clear that callers should not use that.
 * Instead, {@link SmartFormatter} should be the entry point for such conversions. </p>
 */
public class SmartFormatter {

  @Inject static SmartFormatterHelper smartFormatterHelper;

  public static String smartFormatWithDatePrepended(String template, Object... args) {
    return smartFormatterHelper.formatWithDatePrepended(template, args);
  }

  public static String smartFormat(String template, Object... args) {
    return smartFormatterHelper.format(template, args);
  }

  /**
   * This is useful for collections of {@link PrintsInstruments}, such as {@link RBSet}.
   * {@link SmartFormatterHelper} knows how to automatically convert objects that implement {@link PrintsInstruments}
   * (the simplest example being {@link InstrumentId}), but it will not know how to do that with <em> collections </em>
   * of such objects. This method takes care of that, at the expense of printing all raw collections the same way.
   * That is, a list and an {@link RBSet} will look the same; the output will not show the type.
   */
  public static <T> String smartFormatCollection(Collection<T> collection) {
    return Joiner.on(" , ").join(collection
        .stream()
        .map(v -> smartFormatterHelper.formatSingleObject(v))
        .iterator());
  }

}
