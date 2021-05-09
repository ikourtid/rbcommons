package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.util.MultidimensionalCounter;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.CoordinatesTest.coordinatesMatcher;
import static com.rb.nonbiz.collections.RBMultidimensionalCounters.asWeFixDimension;
import static com.rb.nonbiz.collections.RBMultidimensionalCounters.asWeFixDimensionIterator;
import static com.rb.nonbiz.collections.RBMultidimensionalCounters.asWeVaryDimension;
import static com.rb.nonbiz.collections.RBMultidimensionalCounters.asWeVaryDimensionIterator;
import static com.rb.nonbiz.collections.RBMultidimensionalCounters.multidimensionalCounterCoordinatesAreValid;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class RBMultidimensionalCountersTest {

  @Test
  public void asWeVaryDimensionIterator_oneDimension_SmallestCase() {
    MultidimensionalCounter counter = new MultidimensionalCounter(4);
    assertThat(
        asWeVaryDimensionIterator(counter, 0),
        iteratorMatcher(
            ImmutableList.<Iterator<Coordinates>>of(
                ImmutableList.of(coordinates(0)).iterator(),
                ImmutableList.of(coordinates(1)).iterator(),
                ImmutableList.of(coordinates(2)).iterator(),
                ImmutableList.of(coordinates(3)).iterator())
                .iterator(),
            f -> iteratorMatcher(f, f2 -> coordinatesMatcher(f2))));
  }

  @Test
  public void asWeFixDimensionIterator_oneDimension_SmallestCase() {
    MultidimensionalCounter counter = new MultidimensionalCounter(4);
    assertThat(
        asWeFixDimensionIterator(counter, 0),
        iteratorMatcher(
            ImmutableList.<Iterator<Coordinates>>of(
                ImmutableList.of(coordinates(0), coordinates(1), coordinates(2), coordinates(3)).iterator())
                .iterator(),
            f -> iteratorMatcher(f, f2 -> coordinatesMatcher(f2))));
  }

  @Test
  public void asWeVaryDimensionIterator_generalCase() {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);

    assertAsWeVaryAsIterator(0, ImmutableList.<Iterator<Coordinates>>of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(1, 0, 0),
            coordinates(2, 0, 0)).iterator(),
        ImmutableList.of(
            coordinates(0, 0, 1),
            coordinates(1, 0, 1),
            coordinates(2, 0, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 1, 0),
            coordinates(1, 1, 0),
            coordinates(2, 1, 0)).iterator(),
        ImmutableList.of(
            coordinates(0, 1, 1),
            coordinates(1, 1, 1),
            coordinates(2, 1, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 2, 0),
            coordinates(1, 2, 0),
            coordinates(2, 2, 0)).iterator(),
        ImmutableList.of(
            coordinates(0, 2, 1),
            coordinates(1, 2, 1),
            coordinates(2, 2, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 3, 0),
            coordinates(1, 3, 0),
            coordinates(2, 3, 0)).iterator(),
        ImmutableList.of(
            coordinates(0, 3, 1),
            coordinates(1, 3, 1),
            coordinates(2, 3, 1)).iterator())
        .iterator());

    assertAsWeVaryAsIterator(1, ImmutableList.<Iterator<Coordinates>>of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 1, 0),
            coordinates(0, 2, 0),
            coordinates(0, 3, 0)).iterator(),
        ImmutableList.of(
            coordinates(0, 0, 1),
            coordinates(0, 1, 1),
            coordinates(0, 2, 1),
            coordinates(0, 3, 1)).iterator(),
        ImmutableList.of(
            coordinates(1, 0, 0),
            coordinates(1, 1, 0),
            coordinates(1, 2, 0),
            coordinates(1, 3, 0)).iterator(),
        ImmutableList.of(
            coordinates(1, 0, 1),
            coordinates(1, 1, 1),
            coordinates(1, 2, 1),
            coordinates(1, 3, 1)).iterator(),
        ImmutableList.of(
            coordinates(2, 0, 0),
            coordinates(2, 1, 0),
            coordinates(2, 2, 0),
            coordinates(2, 3, 0)).iterator(),
        ImmutableList.of(
            coordinates(2, 0, 1),
            coordinates(2, 1, 1),
            coordinates(2, 2, 1),
            coordinates(2, 3, 1)).iterator())
        .iterator());

    assertAsWeVaryAsIterator(2, ImmutableList.<Iterator<Coordinates>>of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 0, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 1, 0),
            coordinates(0, 1, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 2, 0),
            coordinates(0, 2, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 3, 0),
            coordinates(0, 3, 1)).iterator(),
        ImmutableList.of(
            coordinates(1, 0, 0),
            coordinates(1, 0, 1)).iterator(),
        ImmutableList.of(
            coordinates(1, 1, 0),
            coordinates(1, 1, 1)).iterator(),
        ImmutableList.of(
            coordinates(1, 2, 0),
            coordinates(1, 2, 1)).iterator(),
        ImmutableList.of(
            coordinates(1, 3, 0),
            coordinates(1, 3, 1)).iterator(),
        ImmutableList.of(
            coordinates(2, 0, 0),
            coordinates(2, 0, 1)).iterator(),
        ImmutableList.of(
            coordinates(2, 1, 0),
            coordinates(2, 1, 1)).iterator(),
        ImmutableList.of(
            coordinates(2, 2, 0),
            coordinates(2, 2, 1)).iterator(),
        ImmutableList.of(
            coordinates(2, 3, 0),
            coordinates(2, 3, 1)).iterator())
        .iterator());

    assertIllegalArgumentException( () -> asWeVaryDimensionIterator(counter, -1));
    assertIllegalArgumentException( () -> asWeVaryDimensionIterator(counter, 3));
  }

  @Test
  public void asWeVaryDimension_generalCase() {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);

    assertAsWeVaryAsList(0, ImmutableList.of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(1, 0, 0),
            coordinates(2, 0, 0)),
        ImmutableList.of(
            coordinates(0, 0, 1),
            coordinates(1, 0, 1),
            coordinates(2, 0, 1)),
        ImmutableList.of(
            coordinates(0, 1, 0),
            coordinates(1, 1, 0),
            coordinates(2, 1, 0)),
        ImmutableList.of(
            coordinates(0, 1, 1),
            coordinates(1, 1, 1),
            coordinates(2, 1, 1)),
        ImmutableList.of(
            coordinates(0, 2, 0),
            coordinates(1, 2, 0),
            coordinates(2, 2, 0)),
        ImmutableList.of(
            coordinates(0, 2, 1),
            coordinates(1, 2, 1),
            coordinates(2, 2, 1)),
        ImmutableList.of(
            coordinates(0, 3, 0),
            coordinates(1, 3, 0),
            coordinates(2, 3, 0)),
        ImmutableList.of(
            coordinates(0, 3, 1),
            coordinates(1, 3, 1),
            coordinates(2, 3, 1))));

    assertAsWeVaryAsList(1, ImmutableList.of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 1, 0),
            coordinates(0, 2, 0),
            coordinates(0, 3, 0)),
        ImmutableList.of(
            coordinates(0, 0, 1),
            coordinates(0, 1, 1),
            coordinates(0, 2, 1),
            coordinates(0, 3, 1)),
        ImmutableList.of(
            coordinates(1, 0, 0),
            coordinates(1, 1, 0),
            coordinates(1, 2, 0),
            coordinates(1, 3, 0)),
        ImmutableList.of(
            coordinates(1, 0, 1),
            coordinates(1, 1, 1),
            coordinates(1, 2, 1),
            coordinates(1, 3, 1)),
        ImmutableList.of(
            coordinates(2, 0, 0),
            coordinates(2, 1, 0),
            coordinates(2, 2, 0),
            coordinates(2, 3, 0)),
        ImmutableList.of(
            coordinates(2, 0, 1),
            coordinates(2, 1, 1),
            coordinates(2, 2, 1),
            coordinates(2, 3, 1))));

    assertAsWeVaryAsList(2, ImmutableList.of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 0, 1)),
        ImmutableList.of(
            coordinates(0, 1, 0),
            coordinates(0, 1, 1)),
        ImmutableList.of(
            coordinates(0, 2, 0),
            coordinates(0, 2, 1)),
        ImmutableList.of(
            coordinates(0, 3, 0),
            coordinates(0, 3, 1)),
        ImmutableList.of(
            coordinates(1, 0, 0),
            coordinates(1, 0, 1)),
        ImmutableList.of(
            coordinates(1, 1, 0),
            coordinates(1, 1, 1)),
        ImmutableList.of(
            coordinates(1, 2, 0),
            coordinates(1, 2, 1)),
        ImmutableList.of(
            coordinates(1, 3, 0),
            coordinates(1, 3, 1)),
        ImmutableList.of(
            coordinates(2, 0, 0),
            coordinates(2, 0, 1)),
        ImmutableList.of(
            coordinates(2, 1, 0),
            coordinates(2, 1, 1)),
        ImmutableList.of(
            coordinates(2, 2, 0),
            coordinates(2, 2, 1)),
        ImmutableList.of(
            coordinates(2, 3, 0),
            coordinates(2, 3, 1))));

    assertIllegalArgumentException( () -> asWeVaryDimensionIterator(counter, -1));
    assertIllegalArgumentException( () -> asWeVaryDimensionIterator(counter, 3));
  }

  @Test
  public void asWeFixDimensionIterator_generalCase() {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);

    assertAsWeFixAsIterator(0, ImmutableList.<Iterator<Coordinates>>of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 0, 1),
            coordinates(0, 1, 0),
            coordinates(0, 1, 1),
            coordinates(0, 2, 0),
            coordinates(0, 2, 1),
            coordinates(0, 3, 0),
            coordinates(0, 3, 1)).iterator(),
        ImmutableList.of(
            coordinates(1, 0, 0),
            coordinates(1, 0, 1),
            coordinates(1, 1, 0),
            coordinates(1, 1, 1),
            coordinates(1, 2, 0),
            coordinates(1, 2, 1),
            coordinates(1, 3, 0),
            coordinates(1, 3, 1)).iterator(),
        ImmutableList.of(
            coordinates(2, 0, 0),
            coordinates(2, 0, 1),
            coordinates(2, 1, 0),
            coordinates(2, 1, 1),
            coordinates(2, 2, 0),
            coordinates(2, 2, 1),
            coordinates(2, 3, 0),
            coordinates(2, 3, 1)).iterator())
    .iterator());

    assertAsWeFixAsIterator(1, ImmutableList.<Iterator<Coordinates>>of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 0, 1),
            coordinates(1, 0, 0),
            coordinates(1, 0, 1),
            coordinates(2, 0, 0),
            coordinates(2, 0, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 1, 0),
            coordinates(0, 1, 1),
            coordinates(1, 1, 0),
            coordinates(1, 1, 1),
            coordinates(2, 1, 0),
            coordinates(2, 1, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 2, 0),
            coordinates(0, 2, 1),
            coordinates(1, 2, 0),
            coordinates(1, 2, 1),
            coordinates(2, 2, 0),
            coordinates(2, 2, 1)).iterator(),
        ImmutableList.of(
            coordinates(0, 3, 0),
            coordinates(0, 3, 1),
            coordinates(1, 3, 0),
            coordinates(1, 3, 1),
            coordinates(2, 3, 0),
            coordinates(2, 3, 1)).iterator())
        .iterator());

    assertAsWeFixAsIterator(2, ImmutableList.<Iterator<Coordinates>>of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 1, 0),
            coordinates(0, 2, 0),
            coordinates(0, 3, 0),
            coordinates(1, 0, 0),
            coordinates(1, 1, 0),
            coordinates(1, 2, 0),
            coordinates(1, 3, 0),
            coordinates(2, 0, 0),
            coordinates(2, 1, 0),
            coordinates(2, 2, 0),
            coordinates(2, 3, 0)).iterator(),
        ImmutableList.of(
            coordinates(0, 0, 1),
            coordinates(0, 1, 1),
            coordinates(0, 2, 1),
            coordinates(0, 3, 1),
            coordinates(1, 0, 1),
            coordinates(1, 1, 1),
            coordinates(1, 2, 1),
            coordinates(1, 3, 1),
            coordinates(2, 0, 1),
            coordinates(2, 1, 1),
            coordinates(2, 2, 1),
            coordinates(2, 3, 1)).iterator())
        .iterator());

    assertIllegalArgumentException( () -> asWeFixDimensionIterator(counter, -1));
    assertIllegalArgumentException( () -> asWeFixDimensionIterator(counter, 3));
  }

  @Test
  public void asWeFixDimension_generalCase() {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);
    
    assertAsWeFixAsList(0, ImmutableList.of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 0, 1),
            coordinates(0, 1, 0),
            coordinates(0, 1, 1),
            coordinates(0, 2, 0),
            coordinates(0, 2, 1),
            coordinates(0, 3, 0),
            coordinates(0, 3, 1)),
        ImmutableList.of(
            coordinates(1, 0, 0),
            coordinates(1, 0, 1),
            coordinates(1, 1, 0),
            coordinates(1, 1, 1),
            coordinates(1, 2, 0),
            coordinates(1, 2, 1),
            coordinates(1, 3, 0),
            coordinates(1, 3, 1)),
        ImmutableList.of(
            coordinates(2, 0, 0),
            coordinates(2, 0, 1),
            coordinates(2, 1, 0),
            coordinates(2, 1, 1),
            coordinates(2, 2, 0),
            coordinates(2, 2, 1),
            coordinates(2, 3, 0),
            coordinates(2, 3, 1))));

    assertAsWeFixAsList(1, ImmutableList.of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 0, 1),
            coordinates(1, 0, 0),
            coordinates(1, 0, 1),
            coordinates(2, 0, 0),
            coordinates(2, 0, 1)),
        ImmutableList.of(
            coordinates(0, 1, 0),
            coordinates(0, 1, 1),
            coordinates(1, 1, 0),
            coordinates(1, 1, 1),
            coordinates(2, 1, 0),
            coordinates(2, 1, 1)),
        ImmutableList.of(
            coordinates(0, 2, 0),
            coordinates(0, 2, 1),
            coordinates(1, 2, 0),
            coordinates(1, 2, 1),
            coordinates(2, 2, 0),
            coordinates(2, 2, 1)),
        ImmutableList.of(
            coordinates(0, 3, 0),
            coordinates(0, 3, 1),
            coordinates(1, 3, 0),
            coordinates(1, 3, 1),
            coordinates(2, 3, 0),
            coordinates(2, 3, 1))));

    assertAsWeFixAsList(2, ImmutableList.of(
        ImmutableList.of(
            coordinates(0, 0, 0),
            coordinates(0, 1, 0),
            coordinates(0, 2, 0),
            coordinates(0, 3, 0),
            coordinates(1, 0, 0),
            coordinates(1, 1, 0),
            coordinates(1, 2, 0),
            coordinates(1, 3, 0),
            coordinates(2, 0, 0),
            coordinates(2, 1, 0),
            coordinates(2, 2, 0),
            coordinates(2, 3, 0)),
        ImmutableList.of(
            coordinates(0, 0, 1),
            coordinates(0, 1, 1),
            coordinates(0, 2, 1),
            coordinates(0, 3, 1),
            coordinates(1, 0, 1),
            coordinates(1, 1, 1),
            coordinates(1, 2, 1),
            coordinates(1, 3, 1),
            coordinates(2, 0, 1),
            coordinates(2, 1, 1),
            coordinates(2, 2, 1),
            coordinates(2, 3, 1))));

    assertIllegalArgumentException( () -> asWeFixDimensionIterator(counter, -1));
    assertIllegalArgumentException( () -> asWeFixDimensionIterator(counter, 3));
  }

  @Test
  public void testMultidimensionalCounterCoordinatesAreValid() {
    MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 1);
    assertTrue(multidimensionalCounterCoordinatesAreValid(counter, coordinates(0, 0, 0)));
    assertTrue(multidimensionalCounterCoordinatesAreValid(counter, coordinates(0, 1, 0)));
    assertTrue(multidimensionalCounterCoordinatesAreValid(counter, coordinates(0, 2, 0)));
    assertTrue(multidimensionalCounterCoordinatesAreValid(counter, coordinates(1, 0, 0)));
    assertTrue(multidimensionalCounterCoordinatesAreValid(counter, coordinates(1, 1, 0)));
    assertTrue(multidimensionalCounterCoordinatesAreValid(counter, coordinates(1, 2, 0)));

    // 1, 2, 0 is the point most opposite from the origin of 0, 0, 0.
    // Exceeding the coordinate in any of those 3 dimensions will take us outside the 3d grid.
    assertFalse(multidimensionalCounterCoordinatesAreValid(counter, coordinates(2, 2, 0)));
    assertFalse(multidimensionalCounterCoordinatesAreValid(counter, coordinates(1, 3, 0)));
    assertFalse(multidimensionalCounterCoordinatesAreValid(counter, coordinates(1, 2, 1)));

    // too few coordinates, or too many
    assertFalse(multidimensionalCounterCoordinatesAreValid(counter, coordinates(0)));
    assertFalse(multidimensionalCounterCoordinatesAreValid(counter, coordinates(0, 0)));
    assertFalse(multidimensionalCounterCoordinatesAreValid(counter, coordinates(0, 0, 0, 0)));
  }

  private void assertAsWeFixAsIterator(int dimensionToVary, Iterator<Iterator<Coordinates>> expected) {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);
    assertThat(
        asWeFixDimensionIterator(counter, dimensionToVary),
        iteratorMatcher(
            expected,
            f -> iteratorMatcher(f, f2 -> coordinatesMatcher(f2))));
  }

  private void assertAsWeFixAsList(int dimensionToVary, List<List<Coordinates>> expected) {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);
    assertThat(
        asWeFixDimension(counter, dimensionToVary),
        orderedListMatcher(
            expected,
            f -> orderedListMatcher(f, f2 -> coordinatesMatcher(f2))));
  }

  private void assertAsWeVaryAsIterator(int dimensionToVary, Iterator<Iterator<Coordinates>> expected) {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);
    assertThat(
        asWeVaryDimensionIterator(counter, dimensionToVary),
        iteratorMatcher(
            expected,
            f -> iteratorMatcher(f, f2 -> coordinatesMatcher(f2))));
  }

  private void assertAsWeVaryAsList(int dimensionToVary, List<List<Coordinates>> expected) {
    MultidimensionalCounter counter = new MultidimensionalCounter(3, 4, 2);
    assertThat(
        asWeVaryDimension(counter, dimensionToVary),
        orderedListMatcher(
            expected,
            f -> orderedListMatcher(f, f2 -> coordinatesMatcher(f2))));
  }

}
