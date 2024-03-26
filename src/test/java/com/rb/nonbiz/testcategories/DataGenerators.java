package com.rb.nonbiz.testcategories;

import com.rb.ExcludeSlowTestsSuiteRBCommons;

/**
 * Just an empty category marker interface to denote classes whose job is to create data (usually files that become
 * input to the backtests, such as various protobuf files), but don't otherwise add business logic or run backtests.
 *
 * <p> Because these typically take longer than most unit tests, we typically don't include them in the
 * 'run all unit tests' test suites such as {@link ExcludeSlowTestsSuiteRBCommons}. </p>
 */
public interface DataGenerators {

}
