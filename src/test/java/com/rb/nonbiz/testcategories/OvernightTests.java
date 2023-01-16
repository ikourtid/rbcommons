package com.rb.nonbiz.testcategories;

/**
 * This is just a category marker, so it is intentionally empty.
 *
 * <p> It is meant to separate the time-consuming tests from regular tests,
 * so that we can only run those ones 'overnight'. Of course, it doesn't have to be overnight, but the point is that
 * these are slow enough to take multiple hours and don't make sense to run in the typical intraday runs of
 * unit tests (currently - Jan 2023 - named ExcludeSlowTestsSuite*). </p>
 *
 * <p> This is currently mostly backtests, but can also include other slow tests, even ones that are a few
 * seconds long, which is still too long to include in an ESTS run. </p>
 */
public interface OvernightTests {

}
