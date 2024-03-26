package com.rb.nonbiz.testcategories;

/**
 * This is just a category marker, so it is intentionally empty.
 *
 * <p> It is meant to separate those client demos that are 'slow' (like > 1 sec) from regular tests,
 * so that they will not as part of the various ExcludeSlowTestsSuite flavors of groups of tests, which we run on
 * development laptops many times intraday. We don't want those tests - currently (Jan 2023) over 20,000 of them -
 * to take more than a few seconds and interfere with our development. </p>
 */
public interface SlowClientDemos extends OvernightTests {

}
