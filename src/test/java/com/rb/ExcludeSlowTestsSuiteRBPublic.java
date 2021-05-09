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
 * Useful for running all RBPublic tests except for the slow ones,
 * so we can quickly-ish (~20 secs as of Dec 2019) see if we broke any tests.
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
public class ExcludeSlowTestsSuiteRBPublic {

}

