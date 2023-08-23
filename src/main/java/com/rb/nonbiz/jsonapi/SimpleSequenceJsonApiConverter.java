package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.math.sequence.ArithmeticProgression;
import com.rb.nonbiz.math.sequence.ConstantSequence;
import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.math.sequence.Sequence;
import com.rb.nonbiz.math.sequence.SimpleSequence;
import com.rb.nonbiz.math.sequence.SimpleSequence.Visitor;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonStringOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithSubclassesDocumentation.JsonApiClassWithSubclassesDocumentationBuilder.jsonApiClassWithSubclassesDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiSubclassInfo.JsonApiSubclassInfoBuilder.jsonApiSubclassInfoBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link SimpleSequence} back and forth to JSON for our public API.
 */
public class SimpleSequenceJsonApiConverter implements HasJsonApiDocumentation {

  @Inject ArithmeticProgressionJsonApiConverter arithmeticProgressionJsonApiConverter;
  @Inject ConstantSequenceJsonApiConverter constantSequenceJsonApiConverter;
  @Inject GeometricProgressionJsonApiConverter geometricProgressionJsonApiConverter;

  // Typically, JSON API converter classes use a JsonValidator. However, because this represents
  // one of multiple subclasses, we delegate the JSON validation to the JSON API converters of the subclasses.
  // Also, it would be difficult to specify the optional properties, since each subclass has its own.

  public <T> JsonObject toJsonObject(SimpleSequence<T> simpleSequence, Function<T, JsonElement> itemSerializer) {
    // Unfortunately there's no good way to do this without instanceof. This is because ArithmeticProgression
    // and GeometricProgression are both specific to Double, whereas ConstantSequence can apply to any data type,
    // not just double.
    return simpleSequence.visit(new Visitor<T, JsonObject>() {
      @Override
      public JsonObject visitConstantSequence(ConstantSequence<T> constantSequence) {
        return constantSequenceJsonApiConverter.toJsonObject(constantSequence, itemSerializer);
      }

      @Override
      public JsonObject visitArithmeticProgression(ArithmeticProgression<T> arithmeticProgression) {
        return arithmeticProgressionJsonApiConverter.toJsonObject(arithmeticProgression, itemSerializer);
      }

      @Override
      public JsonObject visitGeometricProgression(GeometricProgression<T> geometricProgression) {
        return geometricProgressionJsonApiConverter.toJsonObject(geometricProgression, itemSerializer);
      }
    });
  }

  public <T> SimpleSequence<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> itemDeserializer,
      BiFunction<T, Double, T> nextItemGeneratorIfArithmetic,
      BiFunction<T, PositiveMultiplier, T> nextItemGeneratorIfGeometric) {
    String typeDiscriminatorValue = getJsonStringOrThrow(jsonObject, "type");
    boolean isConstant   = typeDiscriminatorValue.equals("constantSequence");
    boolean isArithmetic = typeDiscriminatorValue.equals("arithmeticProgression");
    boolean isGeometric  = typeDiscriminatorValue.equals("geometricProgression");

    RBPreconditions.checkArgument(
        isConstant ^ isArithmetic ^ isGeometric,
        "Exactly one of 'constantSequence', 'arithmeticProgression', 'geometricProgression' must be specified: JSON was %s",
        jsonObject);
    return isGeometric ? geometricProgressionJsonApiConverter .fromJsonObject(jsonObject, itemDeserializer, nextItemGeneratorIfGeometric)
        : isArithmetic ? arithmeticProgressionJsonApiConverter.fromJsonObject(jsonObject, itemDeserializer, nextItemGeneratorIfArithmetic)
        :                constantSequenceJsonApiConverter     .fromJsonObject(jsonObject, itemDeserializer);
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassWithSubclassesDocumentationBuilder()
        .setClassBeingDocumented(Sequence.class)
        .setSingleLineSummary(documentation(
            "A sequence (in the math sense) of numbers, or objects with numbers in them."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "Can be an arithmetic progression, geometric progression, or just a constant sequence. <p />",

            "Note that this is more general than a single number; it could be any object with a number in it. ",
            "If so, then any subsequent items in the sequence will be identical to the initial value, except for ",
            "the number in the object. <p />")))
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
