package com.rb.nonbiz.testcategories;

import com.rb.ExcludeSlowTestsSuiteRBCommons;

/**
 * Just an empty category marker interface to denote classes whose job is merely to print information to the console,
 *   // but not otherwise add business logic, run backtests, etc.
 *
 * <p> Because these typically take longer than most unit tests, we typically don't include them in the
 * 'run all unit tests' test suites such as {@link ExcludeSlowTestsSuiteRBCommons}. </p>
 */
public interface DataPrinters {

}
