package com.rb.nonbiz.reflection;

/**
 * The code here is to help us generate a list of Java files from the rb module
 * for supporting clients who get a binary license of rb.jar (i.e. not the source code).
 *
 * Such clients will need to access the system's functionality, although not the 'innards'.
 * Here's what they'll need. + means 'include', - means 'exclude'
 *
 * SOURCE CODE
 *   PROD/MAIN
 *     Most of com.rb.nonbiz (e.g. RBMap, RBOptionals, etc.)
 *   TEST
 *     + Test-only functionality (e.g. RBCollectionMatchers)
 *     - Individual tests such as OrdersTest. If they want ordersMatcher, it will still be accessible via the jar,
 *       and there's probably no need for javadoc.
 *
 * JAVADOC ONLY
 *   PROD/MAIN
 *     + for all classes that implement Investor
 *     + for all data classes that are mentioned in CompleteInvestorInputs (recursively)
 *   TEST
 *     n/a ; our test code (or even the test support files such as OptionalMatchers) does not have javadoc.
 */