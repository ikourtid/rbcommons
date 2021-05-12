package com.rb;

import com.rb.nonbiz.testcategories.AllTests;
import com.rb.nonbiz.testcategories.Backtests;
import com.rb.nonbiz.testcategories.DataGenerators;
import com.rb.nonbiz.testcategories.DataPrinters;
import com.rb.nonbiz.testcategories.EigenAnalyzers;
import com.rb.nonbiz.testcategories.RealMarketDataSanityCheckTests;
import com.rb.nonbiz.testcategories.ResearchBacktests;
import com.rb.nonbiz.testcategories.SlowTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * Useful for running all RBCommons tests except for the slow ones,
 * so we can quickly-ish (~2 secs as of May 2021) see if we broke any tests.
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory( {
    Backtests.class,
    DataGenerators.class,
    DataPrinters.class,
    EigenAnalyzers.class,
    RealMarketDataSanityCheckTests.class,
    SlowTests.class,
    ResearchBacktests.class
})
@Suite.SuiteClasses( { AllTests.class })
public class ExcludeSlowTestsSuiteRBCommons {

}

