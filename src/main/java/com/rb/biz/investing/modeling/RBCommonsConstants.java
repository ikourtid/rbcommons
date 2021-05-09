package com.rb.biz.investing.modeling;

import com.rb.nonbiz.text.Strings;

import java.math.MathContext;
import java.time.LocalDate;

public class RBCommonsConstants {

  public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;

  public double getDefaultWeightForMinAndMaxArtificialTerms() {
    return 1e-6;
  }

  public int getNumSamplesForGlobalObjectiveCalibration() {
    return 252; // ~= 1 year
  }

  /**
   * asOf is the instrument data info that was available on that date.
   * In practice, for now (Jan 2017), we'll always use the same file.
   */
  public String getInstrumentMasterFileName(LocalDate asOf) {
    return getFullFilename("ssd/rb/src/main/md/instrument_master/im.txt");
  }

  public String getFullFilename(String fromHome) {
    return Strings.format("%s/%s", System.getenv("HOME"), fromHome);
  }

}
