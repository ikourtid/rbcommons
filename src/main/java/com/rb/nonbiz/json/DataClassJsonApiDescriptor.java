package com.rb.nonbiz.json;

import com.rb.nonbiz.text.Strings;

/**
 * This is helpful in the JSON API documentation (OpenAPI / Swagger). It gives us type information for a property of a
 * JSON object in the JSON API serialization.
 */
public abstract class DataClassJsonApiDescriptor {

  public interface Visitor<T> {

    T visitSimpleClassJsonApiDescriptor(SimpleClassJsonApiDescriptor simpleClassJsonApiDescriptor);
    T visitUniqueIdJsonApiDescriptor(UniqueIdJsonApiDescriptor uniqueIdJsonApiDescriptor);
    T visitIidMapJsonApiDescriptor(IidMapJsonApiDescriptor iidMapJsonApiDescriptor);
    T visitRBMapJsonApiDescriptor(RBMapJsonApiDescriptor rbMapJsonApiDescriptor);
    T visitCollectionJsonApiDescriptor(CollectionJsonApiDescriptor collectionJsonApiDescriptor);
    T visitYearlyTimeSeriesJsonApiDescriptor(YearlyTimeSeriesJsonApiDescriptor yearlyTimeSeriesJsonApiDescriptor);

  }

  public abstract <T> T visit(Visitor<T> visitor);


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the simplest case
   * where it is a single JSON API data class (e.g. not some collection).
   */
  public static class SimpleClassJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> clazz;

    private SimpleClassJsonApiDescriptor(Class<?> clazz) {
      this.clazz = clazz;
    }

    public static SimpleClassJsonApiDescriptor simpleClassJsonApiDescriptor(Class<?> clazz) {
      return new SimpleClassJsonApiDescriptor(clazz);
    }

    /**
     * This cannot be called getClass because it's an existing method in java.lang.Object.
     */
    public Class<?> getClassBeingDescribed() {
      return clazz;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitSimpleClassJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[SCJAD %s SCJAD]", clazz);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the simplest case
   * where it is a single JSON API data class (e.g. not some collection).
   */
  public static class UniqueIdJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> clazz;

    private UniqueIdJsonApiDescriptor(Class<?> clazz) {
      this.clazz = clazz;
    }

    public static UniqueIdJsonApiDescriptor uniqueIdJsonApiDescriptor(Class<?> clazz) {
      return new UniqueIdJsonApiDescriptor(clazz);
    }

    /**
     * This cannot be called getClass because it's an existing method in java.lang.Object.
     */
    public Class<?> getClassOfId() {
      return clazz;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitUniqueIdJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[UIJAD %s UIJAD]", clazz);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an IidMap of some Java data class.
   */
  public static class IidMapJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> iidMapValueClass;

    private IidMapJsonApiDescriptor(Class<?> iidMapValueClass) {
      this.iidMapValueClass = iidMapValueClass;
    }

    public static IidMapJsonApiDescriptor iidMapJsonApiDescriptor(Class<?> iidMapValueClass) {
      return new IidMapJsonApiDescriptor(iidMapValueClass);
    }

    public Class<?> getIidMapValueClass() {
      return iidMapValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitIidMapJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[IMJAD %s IMJAD]", iidMapValueClass);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an RBMap of some Java data class.
   */
  public static class RBMapJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> rbMapKeyClass;
    private final Class<?> rbMapValueClass;

    private RBMapJsonApiDescriptor(Class<?> rbMapKeyClass, Class<?> rbMapValueClass) {
      this.rbMapKeyClass = rbMapKeyClass;
      this.rbMapValueClass = rbMapValueClass;
    }

    public static RBMapJsonApiDescriptor rbMapJsonApiDescriptor(Class<?> rbMapKeyClass, Class<?> rbMapValueClass) {
      return new RBMapJsonApiDescriptor(rbMapKeyClass, rbMapValueClass);
    }

    public Class<?> getRbMapKeyClass() {
      return rbMapKeyClass;
    }

    public Class<?> getRBMapValueClass() {
      return rbMapValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitRBMapJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[RMJAD %s -> %s RMJAD]", rbMapKeyClass, rbMapValueClass);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an collection (Set, List, or array) of some Java data class.
   */
  public static class CollectionJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> collectionValueClass;

    private CollectionJsonApiDescriptor(Class<?> collectionValueClass) {
      this.collectionValueClass = collectionValueClass;
    }

    public static CollectionJsonApiDescriptor collectionJsonApiDescriptor(Class<?> arrayValueClass) {
      return new CollectionJsonApiDescriptor(arrayValueClass);
    }

    public Class<?> getCollectionValueClass() {
      return collectionValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitCollectionJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[CJAD %s CJAD]", collectionValueClass);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of a YearlyTimeSeries of some Java data class.
   */
  public static class YearlyTimeSeriesJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> yearlyTimeSeriesValueClass;

    private YearlyTimeSeriesJsonApiDescriptor(Class<?> yearlyTimeSeriesValueClass) {
      this.yearlyTimeSeriesValueClass = yearlyTimeSeriesValueClass;
    }

    public static YearlyTimeSeriesJsonApiDescriptor yearlyTimeSeriesJsonApiDescriptor(Class<?> arrayValueClass) {
      return new YearlyTimeSeriesJsonApiDescriptor(arrayValueClass);
    }

    public Class<?> getYearlyTimeSeriesValueClass() {
      return yearlyTimeSeriesValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitYearlyTimeSeriesJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[CJAD %s CJAD]", yearlyTimeSeriesValueClass);
    }

  }


}
