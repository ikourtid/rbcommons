package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.OnesBasedReturn;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D;
import com.rb.nonbiz.collections.ImmutableIndexableArray2D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality.ACTUAL;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality.BACK_FILLED;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality.GAP_FILLED;

/**
 * 'in matrix form' means we can actually use this to pass it to the correlations and eigendecomposition
 * calculations, so it includes gap-filled and back-filled returns (it has to, since it's a rectangular matrix).
 *
 * This also stores information about what returns were gap-filled, back-filled, or actual.
 */
public class ReturnsInMatrixForm {

  public enum ReturnsQuality {

    ACTUAL,
    GAP_FILLED, // usually when there's a trading halt. This implies real returns exist on both sides
    BACK_FILLED // means a stock return is for a time before the start of the trading history for a stock

  }

  private final ImmutableDoubleIndexableArray2D<LocalDate, InstrumentId> returnsMatrix;
  private final ImmutableIndexableArray2D<LocalDate, InstrumentId, ReturnsQuality> qualityMatrix;

  private ReturnsInMatrixForm(
      ImmutableDoubleIndexableArray2D<LocalDate, InstrumentId> returnsMatrix,
      ImmutableIndexableArray2D<LocalDate, InstrumentId, ReturnsQuality> qualityMatrix) {
    this.returnsMatrix = returnsMatrix;
    this.qualityMatrix = qualityMatrix;
  }

  public static ReturnsInMatrixForm returnsInMatrixForm(
      ImmutableDoubleIndexableArray2D<LocalDate, InstrumentId> returnsMatrix,
      ImmutableIndexableArray2D<LocalDate, InstrumentId, ReturnsQuality> qualityMatrix) {
    ArrayIndexMapping<LocalDate> rowMapping = returnsMatrix.getRowMapping();
    ArrayIndexMapping<InstrumentId> columnMapping = returnsMatrix.getColumnMapping();
    RBPreconditions.checkArgument(
        rowMapping.getAllKeys().equals(qualityMatrix.getRowMapping().getAllKeys()),
        "Returns and quality-of-returns must refer to the same dates but they don't: %s vs %s",
        rowMapping, qualityMatrix.getRowMapping());
    RBPreconditions.checkArgument(
        columnMapping.getAllKeys().equals(
            qualityMatrix.getColumnMapping().getAllKeys()),
        "Returns and quality-of-returns must refer to the same instruments but they don't: %s vs %s",
        columnMapping, qualityMatrix.getColumnMapping());
    RBPreconditions.checkArgument(
        returnsMatrix.getNumRows() > 0 && returnsMatrix.getNumColumns() > 0,
        "Returns and quality-of-returns can't be empty");
    for (int instrumentIndex = 0; instrumentIndex < columnMapping.size(); instrumentIndex++) {
      InstrumentId instrumentId = columnMapping.getKey(instrumentIndex);
      LocalDate earliestDate = rowMapping.getKey(0);
      ReturnsQuality previous = qualityMatrix.get(earliestDate, instrumentId);
      for (int dateIndex = 1; dateIndex < rowMapping.size(); dateIndex++) {
        LocalDate date = rowMapping.getKey(dateIndex);
        ReturnsQuality current = qualityMatrix.get(date, instrumentId);
        switch (previous) {
          case ACTUAL:
            RBPreconditions.checkArgument(
                current == GAP_FILLED || current == ACTUAL,
                "From ACTUAL, data can only go to ACTUAL or to GAP_FILLED");
            break;
          case GAP_FILLED:
            RBPreconditions.checkArgument(
                current == GAP_FILLED || current == ACTUAL,
                "From GAP_FILLED, data can only go to ACTUAL or to GAP_FILLED");
            break;
          case BACK_FILLED:
            RBPreconditions.checkArgument(
                current == BACK_FILLED || current == ACTUAL,
                "From BACK_FILLED, data can only go to BACK_FILLED or to ACTUAL");
            break;
          default:
            throw new IllegalArgumentException(Strings.format("Unhandled case of %s", previous));
        }
        previous = current;
      }
    }

    for (int instrumentIndex = 0; instrumentIndex < columnMapping.size(); instrumentIndex++) {
      boolean atLeastOneActual = false;
      for (int dateIndex = 0; dateIndex < rowMapping.size(); dateIndex++) {
        OnesBasedReturn throwsIfBad = onesBasedReturn(returnsMatrix.getByIndex(dateIndex, instrumentIndex));
        if (qualityMatrix.getByIndex(dateIndex, instrumentIndex) == ACTUAL) {
          atLeastOneActual = true;
        }
      }
      RBPreconditions.checkArgument(
          atLeastOneActual,
          "Instrument %s has no actual returns; it's all back-filled and gap-filled",
          columnMapping.getKey(instrumentIndex));
    }
    return new ReturnsInMatrixForm(returnsMatrix, qualityMatrix);
  }

  public ImmutableDoubleIndexableArray2D<LocalDate, InstrumentId> getReturnsMatrix() {
    return returnsMatrix;
  }

  public ImmutableIndexableArray2D<LocalDate, InstrumentId, ReturnsQuality> getQualityMatrix() {
    return qualityMatrix;
  }

}
