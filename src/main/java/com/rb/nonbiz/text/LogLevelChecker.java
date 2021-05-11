package com.rb.nonbiz.text;

/**
 * This does very trivial work, but having a separate verb class is useful for testing code
 * that looks at the log level to affect the logic (i.e. not just to determine whether to log or not).
 *
 * We will not unit-test this class because it's hard to simulate log levels in a test,
 * but at least this class is tiny and simple, so it doesn't need testing really.
 */
public class LogLevelChecker {

  public boolean isDebugEnabled(RBLog log) {
    return log.isDebugEnabled();
  }

}
