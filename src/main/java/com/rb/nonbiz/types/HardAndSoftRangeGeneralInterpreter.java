package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.google.inject.Inject;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;

/**
 * This is a more general case of the {@link HardAndSoftRangeInterpreter}, except that it also handles the
 * situation where there is no 'current point'. See {@link HardAndSoftRangeInterpreter} for (much) more info.
 */
public class HardAndSoftRangeGeneralInterpreter {

  @Inject HardAndSoftRangeInterpreter hardAndSoftRangeInterpreter;

  public <T extends RBNumeric<? super T>> Range<T> getRangeToUse(
      Optional<T> currentPoint, HardAndSoftRange<T> hardAndSoftRange) {
    return transformOptional(
        currentPoint,
        v -> hardAndSoftRangeInterpreter.getRangeToUse(v, hardAndSoftRange))
        // If there is no current point, it makes sense to use the tighter / most restrictive range of the two.
        .orElse(hardAndSoftRange.getSoftRange());
  }

}
