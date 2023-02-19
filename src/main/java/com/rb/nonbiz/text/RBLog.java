package com.rb.nonbiz.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static com.rb.nonbiz.text.SmartFormatter.smartFormatWithTimePrepended;

/**
 * Every class FooBarBaz that wants to log should have this, at the top:
 * private static final RBLog log = rbLog(FooBarBaz.class);
 * ... and then just make a bunch of calls like {@code
 * log.info("xyz");
 * log.info( () -> someComplicatedCalculation());
 * }
 *
 * There are overloads that take {@code Supplier<String>}. Those save us from the overhead of building
 * a string if we are not going to be printing it anyway
 * for the cases where the logger is not enabled for a particular log level.
 */
public class RBLog {

  private final Logger logger;

  private RBLog(Logger logger) {
    this.logger = logger;
  }

  public static RBLog rbLog(Class<?> className) {
    return new RBLog(LogManager.getLogger(className));
  }

  /**
   * Use this for cases when you want to have {@code >} 1 logger per class,
   * and you want to distinguish between them.
   */
  public static RBLog rbLog(Class<?> className, String suffix) {
    return new RBLog(LogManager.getLogger(Strings.format("%s.%s", className.getName(), suffix)));
  }

  public void error(String template, Object... args) {
    logger.error( () -> formatWithTime(template, args));
  }

  public void error(Supplier<String> messageSupplier) {
    if (logger.isErrorEnabled()) {
      error(messageSupplier.get());
    }
  }

  public void warn(String template, Object... args) {
    logger.warn( () -> formatWithTime(template, args));
  }

  public void warn(Supplier<String> messageSupplier) {
    if (logger.isWarnEnabled()) {
      warn(messageSupplier.get());
    }
  }

  public void info(String template, Object... args) {
    logger.info( () -> formatWithTime(template, args));
  }

  public void info(Supplier<String> messageSupplier) {
    if (logger.isInfoEnabled()) {
      info(messageSupplier.get());
    }
  }

  public void debug(String template, Object... args) {
    logger.debug( () -> formatWithTime(template, args));
  }

  public void debug(Supplier<String> stringSupplier) {
    if (logger.isDebugEnabled()) {
      debug(stringSupplier.get());
    }
  }

  public void trace(String template, Object... args) {
    logger.trace( () -> formatWithTime(template, args));
  }

  public void trace(Supplier<String> stringSupplier) {
    if (logger.isTraceEnabled()) {
      trace(stringSupplier.get());
    }
  }

  /**
   * Use {@code info(Supplier<String>)} instead of this, when possible.
   */
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  /**
   * Use {@code debug(Supplier<String>)} instead of this, when possible.
   */
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  /**
   * Use {@code trace(Supplier<String>)} instead of this, when possible.
   */
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  private String formatWithTime(String template, Object... args) {
    return smartFormatWithTimePrepended(template, args);
  }

}
