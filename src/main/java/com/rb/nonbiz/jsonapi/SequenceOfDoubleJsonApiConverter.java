package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.math.sequence.ArithmeticProgression;
import com.rb.nonbiz.math.sequence.ConstantSequence;
import com.rb.nonbiz.math.sequence.DoubleSequence;
import com.rb.nonbiz.math.sequence.DoubleSequence.Visitor;
import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.math.sequence.Sequence;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonStringOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentation.JsonApiClassWithSubclassesDocumentationBuilder.jsonApiClassWithSubclassesDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiSubclassInfo.JsonApiSubclassInfoBuilder.jsonApiSubclassInfoBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link Sequence} of {@link Double} back and forth to JSON for our public API.
 */
public class SequenceOfDoubleJsonApiConverter implements HasJsonApiDocumentation {

  @Inject ArithmeticProgressionJsonApiConverter arithmeticProgressionJsonApiConverter;
  @Inject ConstantSequenceJsonApiConverter constantSequenceJsonApiConverter;
  @Inject GeometricProgressionJsonApiConverter geometricProgressionJsonApiConverter;

  // Typically, JSON API converter classes use a JsonValidator. However, because this represents
  // one of multiple subclasses, we delegate the JSON validation to the JSON API converters of the subclasses.
  // Also, it would be difficult to specify the optional properties, since each subclass has its own.

  public JsonObject toJsonObject(Sequence<Double> sequence) {
    // Unfortunately there's no good way to do this without instanceof. This is because ArithmeticProgression
    // and GeometricProgression are both specific to Double, whereas ConstantSequence can apply to any data type,
    // not just double.
    if (sequence instanceof DoubleSequence) {
      DoubleSequence doubleSequence = (DoubleSequence) sequence;
      return doubleSequence.visit(new Visitor<JsonObject>() {
        @Override
        public JsonObject visitArithmeticProgression(ArithmeticProgression arithmeticProgression) {
          return arithmeticProgressionJsonApiConverter.toJsonObject(arithmeticProgression);
        }

        @Override
        public JsonObject visitGeometricProgression(GeometricProgression geometricProgression) {
          return geometricProgressionJsonApiConverter.toJsonObject(geometricProgression);
        }
      });
    }
    if (sequence instanceof ConstantSequence) {
      return constantSequenceJsonApiConverter.toJsonObject(
          (ConstantSequence<Double>) sequence,
          v -> jsonDouble(v));
    }
    throw new IllegalArgumentException(Strings.format(
        "The only Sequence objects that can be converted to JSON are ConstantSequence, ArithmeticProgression, and " +
            "GeometricProgression; got a %s",
        sequence.getClass().getCanonicalName()));
  }

  public Sequence<Double> fromJsonObject(JsonObject jsonObject) {
    String typeDiscriminatorValue = getJsonStringOrThrow(jsonObject, "type");
    boolean isConstant   = typeDiscriminatorValue.equals("constantSequence");
    boolean isArithmetic = typeDiscriminatorValue.equals("arithmeticProgression");
    boolean isGeometric  = typeDiscriminatorValue.equals("geometricProgression");

    RBPreconditions.checkArgument(
        isConstant ^ isArithmetic ^ isGeometric,
        "Exactly one of 'constantSequence', 'arithmeticProgression', 'geometricProgression' must be specified: JSON was %s",
        jsonObject);
    return isGeometric ? geometricProgressionJsonApiConverter.fromJsonObject(jsonObject)
        : isArithmetic ? arithmeticProgressionJsonApiConverter.fromJsonObject(jsonObject)
        :                constantSequenceJsonApiConverter.fromJsonObject(jsonObject, v -> v.getAsDouble());
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassWithSubclassesDocumentationBuilder()
        .setClassBeingDocumented(Sequence.class)
        .setSingleLineSummary(documentation(
            "A sequence (in the math sense) of Double (floating point numbers)."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "Can be an arithmetic progression, geometric progression, or just a constant sequence.")))
        .setJsonApiInfoOnMultipleSubclasses(
            jsonApiSubclassInfoBuilder()
                .setClassOfSubclass(ConstantSequence.class)
                .setDiscriminatorPropertyValue("constantSequence")
                .setJsonApiConverterForTraversing(constantSequenceJsonApiConverter)
                .build(),
            jsonApiSubclassInfoBuilder()
                .setClassOfSubclass(ArithmeticProgression.class)
                .setDiscriminatorPropertyValue("arithmeticProgression")
                .setJsonApiConverterForTraversing(arithmeticProgressionJsonApiConverter)
                .build(),
            jsonApiSubclassInfoBuilder()
                .setClassOfSubclass(GeometricProgression.class)
                .setDiscriminatorPropertyValue("geometricProgression")
                .setJsonApiConverterForTraversing(geometricProgressionJsonApiConverter)
                .build())
        .setDiscriminatorProperty("type")
        .setNontrivialSampleJson(jsonObject(
            "type",             jsonString("arithmeticProgression"),
            "initialValue",     jsonDouble(10_000),
            "commonDifference", jsonDouble(500)))
        .build();
  }

}
