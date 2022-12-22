package com.rb.nonbiz.testmatchers;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.testutils.Epsilons;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBColtMatchers {

  public static TypeSafeMatcher<DoubleMatrix2D> matrixMatcher(DoubleMatrix2D expected, Epsilons e) {
    return matrixMatcher(expected, e.get(DoubleMatrix2D.class));
  }

  public static TypeSafeMatcher<DoubleMatrix2D> matrixMatcher(DoubleMatrix2D expected, double epsilon) {
    return new TypeSafeMatcher<DoubleMatrix2D>() {
      @Override
      protected boolean matchesSafely(DoubleMatrix2D actual) {
        if (expected.rows() != actual.rows()) {
          return false;
        }
        if (expected.columns() != actual.columns()) {
          return false;
        }
        for (int i = 0; i < expected.rows(); i++) {
          for (int j = 0; j < expected.columns(); j++) {
            if (Math.abs(expected.getQuick(i, j) - actual.getQuick(i, j)) > epsilon) {
              return false;
            }
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("DoubleMatrix2D");
      }
    };
  }

  public static TypeSafeMatcher<DoubleMatrix1D> matrix1dMatcher(DoubleMatrix1D expected, Epsilons e) {
    return matrix1dMatcher(expected, e.get(DoubleMatrix1D.class));
  }

  public static TypeSafeMatcher<DoubleMatrix1D> matrix1dMatcher(DoubleMatrix1D expected, double epsilon) {
    return makeMatcher(expected, actual ->
        doubleArrayMatcher(expected.toArray(), epsilon).matches(actual.toArray()));
  }

}
