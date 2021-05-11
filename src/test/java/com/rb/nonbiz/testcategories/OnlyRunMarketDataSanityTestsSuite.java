package com.rb.nonbiz.testcategories;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Categories.IncludeCategory(RealMarketDataSanityCheckTests.class)
@Suite.SuiteClasses( { AllTests.class })
public class OnlyRunMarketDataSanityTestsSuite {

}
